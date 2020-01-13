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
import utilities.StopwatchService;
import utilities.WatermarkGenerationService;

public class DetectionService {

	public static void getReport(String datasetName, String reportName, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold) {

		// get suspicious fragments
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments");
		StopwatchService stopwatchService = new StopwatchService();
		List<Fragment> suspiciousFragments = FragmentationService.getFragments(datasetName);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FragmentationService.getFragments",
				stopwatchService.getTime());

		// watermark detection
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection");
		stopwatchService = new StopwatchService();
		String report = watermarkDetection(suspiciousFragments, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "watermarkDetection", stopwatchService.getTime());

		// write to file system
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile");
		stopwatchService = new StopwatchService();
		FileService.writeFile(reportName, report);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DetectionService", "FileService.writeFile",
				stopwatchService.getTime());
	}

	private static String watermarkDetection(List<Fragment> suspiciousFragments, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold) {
		List<List<DataLeaker>> datasetLeaker = new LinkedList<>();

		String report = "watermark detection report";
		report = report + "\ndate:\t\t\t\t\t\t\t" + LocalDateTime.now();
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\n";

		// similarity search for each suspicious fragment
		Collections.sort(suspiciousFragments);
		for (int i = 0; i < suspiciousFragments.size(); i++) {
			report = report + "\n[" + (i + 1) + "] suspicious fragment";
			Fragment suspiciousFragment = suspiciousFragments.get(i);
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

				Collections.sort(fragment.getMeasurements());
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

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {

				report = report + "\nmatching original fragment:\t\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;

				// extract (noisy) watermark
				BigDecimal[] noisyWatermark = getEmbeddedWatermark(suspiciousFragment, matchingFragment,
						matchingSequence);

				List<DataLeaker> fragmentLeakers = new LinkedList<>();

				// retrieve requests on matching fragment
				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());

				// retrieve previous and next fragment
				Fragment prevFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(),
						matchingFragment.getDate().minusDays(1));
				Fragment nextFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate().plusDays(1));

				// watermark similarity search
				for (Request request : requests) {
					BigDecimal[] watermark = WatermarkGenerationService.generateWatermark(request, usabilityConstraint,
							matchingFragment, prevFragment, nextFragment);

					BigDecimal watermarkSimilarity = getWatermarkSimilarity(noisyWatermark, watermark,
							usabilityConstraint, matchingSequence);

					// add to list if above threshold
					if (watermarkSimilarity.compareTo(watermarkSimilarityThreshold) >= 0) {
						DataLeaker leaker = new DataLeaker(watermarkSimilarity, request.getDataUserId());
						fragmentLeakers.add(leaker);
					}
				}

				if (fragmentLeakers.size() > 0) {
					Collections.sort(fragmentLeakers);
					report = report + "\nmatching watermarks: ";
					for (DataLeaker leaker : fragmentLeakers) {
						report = report + "\n\t\t\t\t\t" + leaker.toString();
					}
				} else {
					report = report + "\nno matching watermarks detected";
				}

				datasetLeaker.add(fragmentLeakers);
			} else {
				report = report + "\nno matching fragment detected";
			}
		}

		// compute detection summary
		List<DataLeaker> totalLeakageProbabilities = new LinkedList<>();
		for (int i = 0; i < datasetLeaker.size(); i++) {
			for (int j = 0; j < datasetLeaker.get(i).size(); j++) {
				DataLeaker leaker = datasetLeaker.get(i).get(j);
				BigDecimal probability = leaker.getProbability();

				if (totalLeakageProbabilities.contains(leaker)) {
					int index = totalLeakageProbabilities.indexOf(leaker);

					totalLeakageProbabilities.get(index)
							.setProbability(totalLeakageProbabilities.get(index).getProbability().add(probability));
				} else {
					leaker.setProbability(probability);
					totalLeakageProbabilities.add(leaker);
				}
			}
		}
		for(int i = 0; i < totalLeakageProbabilities.size(); i++) {
			BigDecimal probability = totalLeakageProbabilities.get(i).getProbability();
			probability = probability.divide(BigDecimal.valueOf(datasetLeaker.size()));
			totalLeakageProbabilities.get(i).setProbability(probability);
		}

		report = report + "\n\ndetection report summary";
		if (totalLeakageProbabilities.size() > 0) {
			Collections.sort(totalLeakageProbabilities);
			for (DataLeaker leaker : totalLeakageProbabilities) {
				report = report + "\nleaker:\t" + leaker;
			}
		} else {
			report = report + "\nno leakage detected";
		}

		return report;
	}

	private static HashMap<Integer, Integer> getSequence(Fragment suspiciousFragment, Fragment originalFragment,
			UsabilityConstraint usabilityConstraint) {

		HashMap<Integer, Integer> sequence = new HashMap<>();;
		
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

	private static BigDecimal[] getEmbeddedWatermark(Fragment suspiciousFragment, Fragment originalFragment,
			HashMap<Integer, Integer> sequence) {

		BigDecimal[] watermark = new BigDecimal[suspiciousFragment.getMeasurements().size()];

		for (Entry<Integer, Integer> entry : sequence.entrySet()) {
			watermark[entry.getKey()] = suspiciousFragment.getMeasurements()
					.get(entry.getKey()).getValue()
					.subtract(originalFragment.getMeasurements().get(entry.getValue()).getValue());
		}

		return watermark;
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
