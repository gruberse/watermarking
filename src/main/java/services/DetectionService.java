package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.util.CombinatoricsUtils;

import entities.DataLeaker;
import entities.Fragment;
import entities.Measurement;
import entities.Request;
import entities.UsabilityConstraint;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.FragmentationService;
import utilities.LogService;
import utilities.TimeService;
import utilities.WatermarkService;

public class DetectionService {

	public static void detectLeakage(String datasetName, String reportName, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold) {

		// get suspicious fragments
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> suspiciousFragments = FragmentationService.getFragments(datasetName);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments",
				timeService.getTime());

		// watermark detection
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection");
		timeService = new TimeService();
		String report = detectWatermarks(suspiciousFragments, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection", timeService.getTime());

		// write to file system
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile");
		timeService = new TimeService();
		FileService.writeFile(reportName, report);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile", timeService.getTime());
	}

	private static String detectWatermarks(List<Fragment> suspiciousFragments, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold) {
		List<DataLeaker> datasetLeakers = new LinkedList<>();

		String report = "watermark detection report";
		report = report + "\ndate:\t\t\t\t\t\t\t" + LocalDateTime.now();
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\n";

		// similarity search for each suspicious fragment
		Collections.sort(suspiciousFragments);
		for (int i = 0; i < suspiciousFragments.size(); i++) {
			Fragment suspiciousFragment = suspiciousFragments.get(i);

			LogService.log(LogService.METHOD_LEVEL, "watermarkDetection",
					"suspiciousFragment<" + suspiciousFragment.getDeviceId() + ", " + suspiciousFragment.getType()
							+ ", " + suspiciousFragment.getUnit() + ", " + suspiciousFragment.getDate() + ">");
			TimeService timeService = new TimeService();

			report = report + "\n[" + (i + 1) + "] fragment leakage";
			Collections.sort(suspiciousFragment.getMeasurements());

			report = report + "\nsuspicious fragment:\t\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			Fragment matchingFragment = new Fragment();
			BigDecimal matchingFragmentSimilarity = BigDecimal.valueOf(0.0);
			HashMap<Integer, Integer> matchingSequence = new HashMap<>();

			// retrieve usability constraints
			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());

			// fragment similarity search
			for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit(),
					suspiciousFragment.getMin().subtract(usabilityConstraint.getMaximumError()),
					suspiciousFragment.getMax().add(usabilityConstraint.getMaximumError()))) {

				HashMap<Integer, Integer> sequence = getSequence(suspiciousFragment, fragment, usabilityConstraint);

				if (sequence.size() == suspiciousFragment.getMeasurements().size()) {
					BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment, sequence);

					// set highest similarity fragment
					if (fragmentSimilarity.compareTo(matchingFragmentSimilarity) > 0) {
						matchingFragmentSimilarity = fragmentSimilarity;
						matchingFragment = fragment;
						matchingSequence = sequence;
					}
				}
			}

			timeService.stop();
			LogService.log(LogService.METHOD_LEVEL, "watermarkDetection", "suspiciousFragment", timeService.getTime());

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {

				report = report + "\nmatching original fragment:\t\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;

				LogService.log(LogService.METHOD_LEVEL, "suspiciousFragment", "suspiciousWatermark<");
				timeService = new TimeService();

				// extract (noisy) watermark
				BigDecimal[] noisyWatermark = extractWatermark(suspiciousFragment, matchingFragment, matchingSequence);

				// retrieve requests on matching fragment
				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());

				// retrieve previous and next fragment
				Fragment prevFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(),
						matchingFragment.getDate().minusDays(1));
				Fragment nextFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate().plusDays(1));

				List<DataLeaker> fragmentLeakers = new LinkedList<>();

				// watermark similarity search
				for (DataLeaker potentialLeaker : getFragmentLeakers(requests, usabilityConstraint, matchingFragment,
						prevFragment, nextFragment)) {

					BigDecimal watermarkSimilarity = getWatermarkSimilarity(noisyWatermark,
							potentialLeaker.getWatermark(), usabilityConstraint, matchingSequence);

					// add to list if above threshold
					if (watermarkSimilarity.compareTo(watermarkSimilarityThreshold) >= 0) {

						potentialLeaker.setProbability(watermarkSimilarity);
						fragmentLeakers.add(potentialLeaker);

						if (datasetLeakers.contains(potentialLeaker)) {
							DataLeaker leaker = datasetLeakers.get(datasetLeakers.indexOf(potentialLeaker));
							leaker.setProbability(leaker.getProbability().add(potentialLeaker.getProbability()));
						} else {
							datasetLeakers.add(potentialLeaker);
						}
					}
				}

				timeService.stop();
				LogService.log(LogService.METHOD_LEVEL, "suspiciousFragment", "suspiciousWatermark",
						timeService.getTime());

				if (fragmentLeakers.size() > 0) {
					Collections.sort(fragmentLeakers);
					report = report + "\nmatching watermarks: ";
					for (DataLeaker leaker : fragmentLeakers) {
						report = report + "\n\t\t\t\t\t" + leaker.toString();
					}
				} else {
					report = report + "\nno matching watermarks detected";
				}

			} else {
				report = report + "\nno matching fragment detected";
			}
		}

		// compute dataset leakage
		for (int i = 0; i < datasetLeakers.size(); i++) {
			DataLeaker leaker = datasetLeakers.get(i);
			leaker.setProbability(leaker.getProbability().divide(BigDecimal.valueOf(suspiciousFragments.size()), 5, RoundingMode.HALF_UP));
			datasetLeakers.get(i).setProbability(leaker.getProbability());
		}

		report = report + "\n\ndataset leakage";
		if (datasetLeakers.size() > 0) {
			Collections.sort(datasetLeakers);
			for (DataLeaker leaker : datasetLeakers) {
				report = report + "\nleaker:\t" + leaker;
			}
		} else {
			report = report + "\nno leakage detected";
		}

		return report;
	}

	private static HashMap<Integer, Integer> getSequence(Fragment suspiciousFragment, Fragment originalFragment,
			UsabilityConstraint usabilityConstraint) {

		HashMap<Integer, Integer> sequence = new HashMap<>();
		;

		for (int i = 0; i < suspiciousFragment.getMeasurements().size(); i++) {
			Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(i);

			for (int j = 0; j < originalFragment.getMeasurements().size(); j++) {
				Measurement originalMeasurement = originalFragment.getMeasurements().get(j);
				if ((originalMeasurement.getValue().subtract(usabilityConstraint.getMaximumError()))
						.compareTo(suspiciousMeasurement.getValue()) <= 0
						&& (originalMeasurement.getValue().add(usabilityConstraint.getMaximumError()))
								.compareTo(suspiciousMeasurement.getValue()) >= 0
						&& originalMeasurement.getTime().isEqual(suspiciousMeasurement.getTime())) {
					sequence.put(i, j);
				}
			}

			if (sequence.size() < i + 1) {
				return new HashMap<>();
			}
		}
		return sequence;
	}

	private static BigDecimal getFragmentSimilarity(Fragment suspiciousFragment, Fragment originalFragment,
			HashMap<Integer, Integer> sequence) {

		BigDecimal similarity = new BigDecimal("0.0");

		if (sequence.size() == 0) {
			return similarity;
		}

		for (Entry<Integer, Integer> entry : sequence.entrySet()) {

			BigDecimal measurementSimilarity = new BigDecimal("0.0");

			Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(entry.getKey());
			Measurement originalMeasurement = originalFragment.getMeasurements().get(entry.getValue());

			BigDecimal distance = (suspiciousMeasurement.getValue().subtract(originalMeasurement.getValue())).abs();
			if (distance.compareTo(BigDecimal.valueOf(0.0)) == 0) {
				measurementSimilarity = new BigDecimal("1.0");
			} else {
				measurementSimilarity = BigDecimal.valueOf(1)
						.subtract(distance.divide(originalMeasurement.getValue(), 4, RoundingMode.HALF_UP));
			}

			similarity = similarity.add(measurementSimilarity);
		}

		similarity = similarity.divide(BigDecimal.valueOf(sequence.size()), 4, RoundingMode.HALF_UP);

		return similarity;
	}

	private static BigDecimal[] extractWatermark(Fragment suspiciousFragment, Fragment originalFragment,
			HashMap<Integer, Integer> sequence) {

		BigDecimal[] watermark = new BigDecimal[suspiciousFragment.getMeasurements().size()];

		for (Entry<Integer, Integer> entry : sequence.entrySet()) {
			watermark[entry.getKey()] = suspiciousFragment.getMeasurements().get(entry.getKey()).getValue()
					.subtract(originalFragment.getMeasurements().get(entry.getValue()).getValue());
		}

		return watermark;
	}

	private static List<DataLeaker> getFragmentLeakers(List<Request> requests, UsabilityConstraint usabilityConstraint,
			Fragment matchingFragment, Fragment prevFragment, Fragment nextFragment) {
		List<DataLeaker> fragmentLeakers = new LinkedList<>();

		// generate single leakers
		List<DataLeaker> singleLeakers = new LinkedList<>();
		for (Request request : requests) {
			BigDecimal[] watermark = WatermarkService.generateWatermark(request, usabilityConstraint, matchingFragment,
					prevFragment, nextFragment);
			DataLeaker singleLeaker = new DataLeaker(Arrays.asList(request.getDataUser()), watermark);

			singleLeakers.add(singleLeaker);
			fragmentLeakers.add(singleLeaker);
		}

		// generate leaker combinations
		for (int i = 2; i < singleLeakers.size() + 1; i++) {

			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(singleLeakers.size(), i);
			while (iterator.hasNext()) {
				int[] dataUsersCombination = iterator.next();

				List<Integer> dataUsers = new LinkedList<>();
				BigDecimal[] watermark = new BigDecimal[matchingFragment.getMeasurements().size()];
				Arrays.fill(watermark, BigDecimal.valueOf(0));

				// collect data users and watermarks
				for (int index : dataUsersCombination) {
					dataUsers.add(index);
					DataLeaker singleLeaker = singleLeakers.get(index);
					for (int j = 0; j < watermark.length; j++) {
						watermark[j] = watermark[j].add(singleLeaker.getWatermark()[j]);
					}
				}

				// compute mean watermark
				for (int j = 0; j < watermark.length; j++) {
					watermark[j] = watermark[j].divide(BigDecimal.valueOf(dataUsers.size()), RoundingMode.HALF_UP);
				}
				
				fragmentLeakers.add(new DataLeaker(dataUsers, watermark));
			}
		}

		return fragmentLeakers;
	}

	private static BigDecimal getWatermarkSimilarity(BigDecimal[] suspiciousWatermark, BigDecimal[] originalWatermark,
			UsabilityConstraint usabilityConstraint, HashMap<Integer, Integer> sequence) {

		BigDecimal similarity = new BigDecimal("0.0");
		BigDecimal maxDistance = usabilityConstraint.getMaximumError().multiply(BigDecimal.valueOf(2.0));

		for (Entry<Integer, Integer> entry : sequence.entrySet()) {

			BigDecimal markSimilarity = new BigDecimal("0.0");
			BigDecimal suspiciousMark = suspiciousWatermark[entry.getKey()];
			BigDecimal originalMark = originalWatermark[entry.getValue()];

			BigDecimal distance = (suspiciousMark.subtract(originalMark)).abs();

			if (distance.compareTo(BigDecimal.valueOf(0)) == 0) {
				markSimilarity = new BigDecimal("1.0");
			} else if (distance.compareTo(maxDistance) > 0) {
				markSimilarity = new BigDecimal("0.0");
			} else {
				markSimilarity = BigDecimal.valueOf(1).subtract(distance.divide(maxDistance, 4, RoundingMode.HALF_UP));
			}

			similarity = similarity.add(markSimilarity);

		}

		similarity = similarity.divide(BigDecimal.valueOf(sequence.size()), 4, RoundingMode.HALF_UP);

		return similarity;
	}

}
