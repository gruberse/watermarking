package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

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
		String report = detectWatermarks(suspiciousFragments, fragmentSimilarityThreshold, watermarkSimilarityThreshold);
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

			report = report + "\n[" + (i + 1) + "] fragment leakage";
			Collections.sort(suspiciousFragment.getMeasurements());

			report = report + "\nsuspicious fragment:\t\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			Fragment matchingFragment = new Fragment();
			BigDecimal matchingFragmentSimilarity = BigDecimal.valueOf(-1.0);
			HashMap<Integer, Integer> matchingMeasurements = new HashMap<>();

			// retrieve usability constraints
			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());

			// fragment similarity search
			for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit(), suspiciousFragment.getDate())) {

				HashMap<Integer, Integer> matches = getMatchingMeasurements(suspiciousFragment, fragment);

				if (matches.size() == suspiciousFragment.getMeasurements().size()) {
					BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment,
							usabilityConstraint, matches);

					// set highest similarity fragment
					if (fragmentSimilarity.compareTo(matchingFragmentSimilarity) > 0) {
						matchingFragmentSimilarity = fragmentSimilarity;
						matchingFragment = fragment;
						matchingMeasurements = matches;
					}
				}
			}

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {

				report = report + "\nmatching original fragment:\t\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;
				
				// extract (noisy) watermark
				BigDecimal[] noisyWatermark = extractWatermark(suspiciousFragment, matchingFragment,
						matchingMeasurements);

				// retrieve requests on matching fragment
				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());

				List<DataLeaker> fragmentLeakers = new LinkedList<>();

				// watermark similarity search
				for (DataLeaker potentialLeaker : getSetOfPotentialLeakers(requests, usabilityConstraint, matchingFragment)) {

					BigDecimal watermarkSimilarity = getWatermarkSimilarity(noisyWatermark,
							potentialLeaker.getWatermark(), usabilityConstraint, matchingMeasurements);

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

				if (fragmentLeakers.size() > 0) {
					Collections.sort(fragmentLeakers);
					report = report + "\nmatching watermarks: ";
					for (DataLeaker leaker : fragmentLeakers) {
						report = report + "\n\t\t" + leaker.toString();
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
			leaker.setProbability(leaker.getProbability().divide(BigDecimal.valueOf(suspiciousFragments.size()), 4,
					RoundingMode.HALF_UP));
			datasetLeakers.get(i).setProbability(leaker.getProbability());
		}

		report = report + "\n\ndataset leakage";
		if (datasetLeakers.size() > 0) {
			Collections.sort(datasetLeakers);
			for (DataLeaker leaker : datasetLeakers) {
				report = report + "\n\t\t" + leaker;
			}
		} else {
			report = report + "\nno leakage detected";
		}

		return report;
	}

	private static HashMap<Integer, Integer> getMatchingMeasurements(Fragment suspiciousFragment,
			Fragment originalFragment) {

		HashMap<Integer, Integer> sequence = new HashMap<>();

		for (int i = 0; i < suspiciousFragment.getMeasurements().size(); i++) {
			Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(i);

			for (int j = 0; j < originalFragment.getMeasurements().size(); j++) {
				Measurement originalMeasurement = originalFragment.getMeasurements().get(j);
				if (originalMeasurement.getTime().isEqual(suspiciousMeasurement.getTime())) {
					sequence.put(i, j);
				}
			}

		}
		return sequence;
	}

	private static BigDecimal getFragmentSimilarity(Fragment suspiciousFragment, Fragment originalFragment,
			UsabilityConstraint usabilityConstraint, HashMap<Integer, Integer> matchingMeasurements) {

		BigDecimal similarity = new BigDecimal("0.0");

		for (Entry<Integer, Integer> entry : matchingMeasurements.entrySet()) {

			Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(entry.getKey());
			Measurement originalMeasurement = originalFragment.getMeasurements().get(entry.getValue());

			BigDecimal distance = (suspiciousMeasurement.getValue().subtract(originalMeasurement.getValue())).abs();
			BigDecimal relativeDistance = distance.divide(usabilityConstraint.getMaximumValue(), 4,
					RoundingMode.HALF_UP);
			BigDecimal measurementSimilarity = BigDecimal.valueOf(1).subtract(relativeDistance);

			similarity = similarity.add(measurementSimilarity);
		}

		similarity = similarity.divide(BigDecimal.valueOf(suspiciousFragment.getMeasurements().size()), 4,
				RoundingMode.HALF_UP);

		return similarity;
	}

	private static BigDecimal[] extractWatermark(Fragment suspiciousFragment, Fragment matchingFragment,
			HashMap<Integer, Integer> matchingMeasurements) {

		BigDecimal[] watermark = new BigDecimal[suspiciousFragment.getMeasurements().size()];

		for (Entry<Integer, Integer> entry : matchingMeasurements.entrySet()) {
			watermark[entry.getKey()] = suspiciousFragment.getMeasurements().get(entry.getKey()).getValue()
					.subtract(matchingFragment.getMeasurements().get(entry.getValue()).getValue());
		}

		return watermark;
	}

	private static List<DataLeaker> getSetOfPotentialLeakers(List<Request> requests, UsabilityConstraint usabilityConstraint,
			Fragment matchingFragment) {

		List<DataLeaker> setOfPotentialLeakers = new LinkedList<>();
		
		for (Request request : requests) {
			BigDecimal[] watermark = WatermarkService.generateWatermark(request, usabilityConstraint, matchingFragment);
			DataLeaker potentialLeaker = new DataLeaker(request.getDataUser(), watermark);

			setOfPotentialLeakers.add(potentialLeaker);
		}

		return setOfPotentialLeakers;
	}

	private static BigDecimal getWatermarkSimilarity(BigDecimal[] suspiciousWatermark, BigDecimal[] originalWatermark,
			UsabilityConstraint usabilityConstraint, HashMap<Integer, Integer> matchingMeasurements) {

		BigDecimal similarity = new BigDecimal("0.0");

		for (Entry<Integer, Integer> entry : matchingMeasurements.entrySet()) {

			BigDecimal suspiciousMark = suspiciousWatermark[entry.getKey()];
			BigDecimal originalMark = originalWatermark[entry.getValue()];
			
			BigDecimal distance = (suspiciousMark.subtract(originalMark)).abs();
			BigDecimal relativeDistance = distance.divide(
					usabilityConstraint.getMaximumError().multiply(BigDecimal.valueOf(2)), 4, RoundingMode.HALF_UP);
			BigDecimal markSimilarity = BigDecimal.valueOf(1).subtract(relativeDistance);

			if (markSimilarity.compareTo(BigDecimal.valueOf(0)) > 0
					&& markSimilarity.compareTo(BigDecimal.valueOf(1)) < 1) {
				similarity = similarity.add(markSimilarity);
			}
		}

		similarity = similarity.divide(BigDecimal.valueOf(matchingMeasurements.size()), 4, RoundingMode.HALF_UP);

		return similarity;
	}

}
