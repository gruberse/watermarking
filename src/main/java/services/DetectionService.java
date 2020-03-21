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
		List<DataLeaker> datasetLeakers = new LinkedList<>();

		String report = "watermark detection report";
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\nnumber of colluders:\t\t\t" + numberOfColluders;
		report = report + "\n";

		Collections.sort(suspiciousFragments);
		for (int i = 0; i < suspiciousFragments.size(); i++) {
			Fragment suspiciousFragment = suspiciousFragments.get(i);	
			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());

			report = report + "\n[" + (i + 1) + "] fragment leakage";
			report = report + "\nsuspicious fragment:\t\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			Fragment matchingFragment = new Fragment();
			BigDecimal matchingFragmentSimilarity = BigDecimal.valueOf(0.0);
			HashMap<Integer, Integer> matchingMeasurements = new HashMap<>();

			for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit(), suspiciousFragment.getDate())) {
				HashMap<Integer, Integer> matches = getMatchingMeasurements(suspiciousFragment, fragment);
				
				if (matches.size() == suspiciousFragment.getMeasurements().size()) {
					BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment,
							usabilityConstraint, matches);
					
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

				BigDecimal[] extractedWatermark = extractWatermark(suspiciousFragment, matchingFragment,
						matchingMeasurements);
				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());
				List<DataLeaker> fragmentLeakers = new LinkedList<>();

				for (DataLeaker potentialLeaker : getSetOfPotentialLeakers(requests, usabilityConstraint,
						matchingFragment, numberOfColluders)) {
					BigDecimal watermarkSimilarity = getWatermarkSimilarity(extractedWatermark,
							potentialLeaker.getWatermark(), usabilityConstraint, matchingMeasurements);
					
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

	private static List<DataLeaker> getSetOfPotentialLeakers(List<Request> requests,
			UsabilityConstraint usabilityConstraint, Fragment matchingFragment, int numberOfColluders) {
		List<DataLeaker> setOfPotentialLeakers = new LinkedList<>();
		List<DataLeaker> singleLeakers = new LinkedList<>();
		
		for (Request request : requests) {
			BigDecimal[] watermark = WatermarkService.generateWatermark(request, usabilityConstraint, matchingFragment);
			DataLeaker singleLeaker = new DataLeaker(Arrays.asList(request.getDataUser()), watermark);
			singleLeakers.add(singleLeaker);
			setOfPotentialLeakers.add(singleLeaker);
		}
		
		for (int i = 2; i < numberOfColluders + 1 && i <= singleLeakers.size(); i++) {
			Iterator<int[]> iterator = CombinatoricsUtils.combinationsIterator(singleLeakers.size(), i);
			
			while (iterator.hasNext()) {
				int[] dataUsersCombination = iterator.next();
				Arrays.sort(dataUsersCombination);
				List<Integer> dataUsers = new LinkedList<>();
				BigDecimal[] watermark = new BigDecimal[matchingFragment.getMeasurements().size()];
				Arrays.fill(watermark, BigDecimal.valueOf(0));
				
				for (int index : dataUsersCombination) {
					DataLeaker singleLeaker = singleLeakers.get(index);
					dataUsers.add(singleLeaker.getDataUsers().get(0));
					
					for (int j = 0; j < watermark.length; j++) {
						watermark[j] = watermark[j].add(singleLeaker.getWatermark()[j]);
					}
				}
				
				for (int j = 0; j < watermark.length; j++) {
					watermark[j] = watermark[j].divide(BigDecimal.valueOf(dataUsers.size()), 4, RoundingMode.HALF_UP);
				}
				setOfPotentialLeakers.add(new DataLeaker(dataUsers, watermark));
			}
		}
		return setOfPotentialLeakers;
	}

	private static BigDecimal getWatermarkSimilarity(BigDecimal[] extractedWatermark, BigDecimal[] originalWatermark,
			UsabilityConstraint usabilityConstraint, HashMap<Integer, Integer> matchingMeasurements) {
		BigDecimal similarity = new BigDecimal("0.0");
		
		for (Entry<Integer, Integer> entry : matchingMeasurements.entrySet()) {
			BigDecimal suspiciousMark = extractedWatermark[entry.getKey()];
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
