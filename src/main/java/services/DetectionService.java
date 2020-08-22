package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.util.CombinatoricsUtils;

import entities.Fragment;
import entities.DataUserWatermark;
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
			BigDecimal watermarkSimilarityThreshold, int numberOfColluders) {
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> suspiciousFragments = FragmentationService.getFragments(datasetName);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments",
				timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection");
		timeService = new TimeService();
		String report = detectWatermarks(suspiciousFragments, fragmentSimilarityThreshold, watermarkSimilarityThreshold,
				numberOfColluders);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection", timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile");
		timeService = new TimeService();
		FileService.writeFile(reportName, report);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile", timeService.getTime());
	}

	private static String detectWatermarks(List<Fragment> suspiciousFragments, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold, int numberOfColluders) {

		List<List<DataUserWatermark>> setOfMatchingWatermarks = new LinkedList<>();

		String report = "watermark detection report";
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\nnumber of colluders:\t\t\t" + numberOfColluders;
		report = report + "\n";

		for (int i = 0; i < suspiciousFragments.size(); i++) {
			Fragment suspiciousFragment = suspiciousFragments.get(i);

			report = report + "\n[" + (i + 1) + "] fragment leakage";
			report = report + "\nsuspicious fragment:\t\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			Fragment matchingFragment = new Fragment();
			BigDecimal matchingFragmentSimilarity = BigDecimal.valueOf(-1.0);
			HashMap<Integer, Integer> matchingMeasurements = new HashMap<>();

			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());

			for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit())) {
				HashMap<Integer, Integer> matches = getMatchingMeasurements(suspiciousFragment, fragment);

				BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment, usabilityConstraint,
						matches);

				if (fragmentSimilarity.compareTo(matchingFragmentSimilarity) > 0) {
					matchingFragmentSimilarity = fragmentSimilarity;
					matchingFragment = fragment;
					matchingMeasurements = matches;
				}
			}

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {
				report = report + "\nmatching original fragment:\t\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;

				BigDecimal[] extractedWatermark = extractWatermark(suspiciousFragment, matchingFragment,
						matchingMeasurements);

				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());

				List<DataUserWatermark> matchingWatermarks = new LinkedList<>();

				for (DataUserWatermark dataUserWatermark : getSetOfWatermarks(requests, usabilityConstraint,
						matchingFragment, numberOfColluders)) {
					BigDecimal watermarkSimilarity = getWatermarkSimilarity(extractedWatermark,
							dataUserWatermark.getWatermark(), usabilityConstraint, matchingMeasurements);

					if (watermarkSimilarity.compareTo(watermarkSimilarityThreshold) >= 0) {
						dataUserWatermark.setProbability(watermarkSimilarity);
						matchingWatermarks.add(dataUserWatermark);
					}
				}

				if (matchingWatermarks.size() > 0) {
					Collections.sort(matchingWatermarks);
					report = report + "\nmatching watermarks: ";

					for (DataUserWatermark matchingWatermark : matchingWatermarks) {
						report = report + "\n\t\t" + matchingWatermark.toString();
					}

					setOfMatchingWatermarks.add(matchingWatermarks);
				} else {
					report = report + "\nno matching watermarks detected";
				}

			} else {
				report = report + "\nno matching fragment detected";
			}
		}

		List<DataUserWatermark> datasetLeakers = getDatasetLeakers(suspiciousFragments.size(), setOfMatchingWatermarks);
		report = report + "\n\ndataset leakage";
		if (datasetLeakers.size() > 0) {
			Collections.sort(datasetLeakers);

			for (DataUserWatermark dataLeaker : datasetLeakers) {
				report = report + "\n\t\t" + dataLeaker;
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

	private static List<DataUserWatermark> getSetOfWatermarks(List<Request> requests,
			UsabilityConstraint usabilityConstraint, Fragment matchingFragment, int numberOfColluders) {
		List<DataUserWatermark> setOfWatermarks = new LinkedList<>();

		List<DataUserWatermark> singleWatermarks = new LinkedList<>();
		for (Request request : requests) {
			BigDecimal[] watermark = WatermarkService.generateWatermark(request, usabilityConstraint, matchingFragment);
			DataUserWatermark singleWatermark = new DataUserWatermark(Arrays.asList(request.getDataUser()), watermark);
			singleWatermarks.add(singleWatermark);
			setOfWatermarks.add(singleWatermark);
		}

		for (int i = 2; i < numberOfColluders + 1 && i <= singleWatermarks.size(); i++) {
			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(singleWatermarks.size(), i);

			while (iterator.hasNext()) {
				int[] dataUsersCombination = iterator.next();
				Arrays.sort(dataUsersCombination);
				List<Integer> dataUsers = new LinkedList<>();
				BigDecimal[] watermark = new BigDecimal[matchingFragment.getMeasurements().size()];
				Arrays.fill(watermark, BigDecimal.valueOf(0));

				for (int index : dataUsersCombination) {
					DataUserWatermark singleWatermark = singleWatermarks.get(index);
					dataUsers.add(singleWatermark.getDataUsers().get(0));

					for (int j = 0; j < watermark.length; j++) {
						watermark[j] = watermark[j].add(singleWatermark.getWatermark()[j]);
					}
				}

				for (int j = 0; j < watermark.length; j++) {
					watermark[j] = watermark[j].divide(BigDecimal.valueOf(dataUsers.size()), 4, RoundingMode.HALF_UP);
				}

				setOfWatermarks.add(new DataUserWatermark(dataUsers, watermark));
			}
		}
		return setOfWatermarks;
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

	private static List<DataUserWatermark> getDatasetLeakers(int datasetSize,
			List<List<DataUserWatermark>> setOfMatchingWatermarks) {
		List<DataUserWatermark> datasetLeakers = new LinkedList<>();

		for (int i = 0; i < setOfMatchingWatermarks.size(); i++) {
			List<DataUserWatermark> matchingWatermarks = setOfMatchingWatermarks.get(i);

			for (int j = 0; j < matchingWatermarks.size(); j++) {
				DataUserWatermark matchingWatermark = matchingWatermarks.get(j);

				if (datasetLeakers.contains(matchingWatermark)) {
					DataUserWatermark potentialLeaker = datasetLeakers.get(datasetLeakers.indexOf(matchingWatermark));
					potentialLeaker
							.setProbability(potentialLeaker.getProbability().add(matchingWatermark.getProbability()));
				} else {
					datasetLeakers.add(matchingWatermark);
				}

			}
		}

		for (int i = 0; i < datasetLeakers.size(); i++) {
			DataUserWatermark dataLeaker = datasetLeakers.get(i);
			dataLeaker.setProbability(
					dataLeaker.getProbability().divide(BigDecimal.valueOf(datasetSize), 4, RoundingMode.HALF_UP));
			datasetLeakers.get(i).setProbability(dataLeaker.getProbability());
		}

		return datasetLeakers;
	}
}
