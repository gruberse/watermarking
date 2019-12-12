package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import entities.DataLeaker;
import entities.Fragment;
import entities.IndexMap;
import entities.Measurement;
import entities.Request;
import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FragmentationService;
import utils.WatermarkGenerationService;

public class DetectionService {

	public static void getLeakageReport(String datasetName, String reportName, BigDecimal fragmentSimilarityThreshold,
			BigDecimal watermarkSimilarityThreshold) {

		// print header
		String report = "watermark detection report";
		report = report + "\ninvestigated dataset:\t\t" + datasetName;
		report = report + "\ndate:\t\t\t\t" + LocalDateTime.now();
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\n";

		List<Fragment> suspiciousFragments = FragmentationService.getFragments(datasetName);
		List<List<DataLeaker>> datasetLeaker = new LinkedList<>();

		// similarity search for each suspicious fragment
		Collections.sort(suspiciousFragments);
		for (int i = 0; i < suspiciousFragments.size(); i++) {
			report = report + "\n[" + (i + 1) + "] suspicious fragment";
			Fragment suspiciousFragment = suspiciousFragments.get(i);
			Collections.sort(suspiciousFragment.getMeasurements());

			// print suspicious fragment
			report = report + "\nsuspicious fragment:\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			Fragment matchingFragment = new Fragment();
			BigDecimal matchingFragmentSimilarity = BigDecimal.valueOf(0.0);
			List<List<IndexMap>> matchingSequences = new LinkedList<>();

			// retrieve original fragments with similar means
			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());

			for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit())) {

				Collections.sort(fragment.getMeasurements());

				// get sequences
				List<List<IndexMap>> sequences = getSequences(suspiciousFragment, fragment, usabilityConstraint);

				// fragment similarity search
				BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment, sequences);

				// set highest similarity fragment
				if (fragmentSimilarity.compareTo(matchingFragmentSimilarity) > 0) {
					matchingFragmentSimilarity = fragmentSimilarity;
					matchingFragment = fragment;
					matchingSequences = sequences;
				}

				/*
				 * if (fragment.getDate().equals(LocalDate.of(2017, 2, 4))) {
				 * System.out.println("sequences"); for (List<IndexMap> sequence : sequences) {
				 * System.out.println(" sequence"); for (IndexMap indexMap : sequence) {
				 * System.out.println("  " + indexMap); } } }
				 */

			}

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {

				// print matching original fragment
				report = report + "\nmatching original fragment:\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;

				// extract embedded watermark
				BigDecimal[] noisyWatermark = getWatermark(suspiciousFragment, matchingFragment, matchingSequences);

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

				// generated embedded watermarks
				for (Request request : requests) {
					BigDecimal[] watermark = WatermarkGenerationService.generateWatermark_w2(request,
							usabilityConstraint, matchingFragment, prevFragment, nextFragment);

					// watermark similarity search
					BigDecimal watermarkSimilarity = getWatermarkSimilarity(noisyWatermark, watermark,
							usabilityConstraint, matchingSequences);

					// add to list if above threshold
					if (watermarkSimilarity.compareTo(watermarkSimilarityThreshold) >= 0) {
						List<Integer> dataUserIds = new LinkedList<>();
						dataUserIds.add(request.getDataUserId());
						DataLeaker leaker = new DataLeaker(watermarkSimilarity, dataUserIds);
						fragmentLeakers.add(leaker);
					}
				}

				datasetLeaker.add(fragmentLeakers);

				if (fragmentLeakers.size() > 0) {
					Collections.sort(fragmentLeakers);
					report = report + "\nmatching watermarks: ";
					for (DataLeaker leaker : fragmentLeakers) {
						report = report + "\n\t\t\t\t" + leaker.toString();
					}
				} else {
					report = report + "\nno matching watermarks detected";
				}

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
				probability = probability.divide(BigDecimal.valueOf(datasetLeaker.size()), 4, RoundingMode.HALF_UP);

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
		// print detection summary
		report = report + "\n\ndetection report summary";
		if (totalLeakageProbabilities.size() > 0) {
			Collections.sort(totalLeakageProbabilities);
			for (DataLeaker leaker : totalLeakageProbabilities) {
				report = report + "\nleaker:\t" + leaker;
			}
		} else {
			report = report + "\nno leakage detected";
		}

		System.out.println(report);
	}

	private static List<List<IndexMap>> getSequences(Fragment suspiciousFragment, Fragment originalFragment,
			UsabilityConstraint usabilityConstraint) {
		List<List<IndexMap>> sequences = new LinkedList<>();

		// collect sequences of measurements
		for (int i = 0; i < suspiciousFragment.getMeasurements().size(); i++) {
			Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(i);

			int start = i;
			if (sequences.size() > 0) {
				List<IndexMap> sequence = sequences.get(sequences.size() - 1);
				start = sequence.get(sequence.size() - 1).getOriginalIndex() + 1;
			}

			for (int j = start; j < originalFragment.getMeasurements().size(); j++) {
				Measurement originalMeasurement = originalFragment.getMeasurements().get(j);

				if ((originalMeasurement.getValue().subtract(usabilityConstraint.getMaximumError()))
						.compareTo(suspiciousMeasurement.getValue()) <= 0
						&& (originalMeasurement.getValue().add(usabilityConstraint.getMaximumError()))
								.compareTo(suspiciousMeasurement.getValue()) >= 0) {

					if (sequences.size() > 0) {
						List<IndexMap> sequence = sequences.get(sequences.size() - 1);
						if (sequence.get(sequence.size() - 1).getOriginalIndex() < j) {
							sequence.add(new IndexMap(i, j));
							sequences.set(sequences.size() - 1, sequence);
						} else {
							sequence = new LinkedList<>();
							sequence.add(new IndexMap(i, j));
							sequences.add(sequence);
						}
					} else {
						List<IndexMap> sequence = new LinkedList<>();
						sequence.add(new IndexMap(i, j));
						sequences.add(sequence);
					}
					break;
				}
			}
		}

		List<List<IndexMap>> resultSequences = new LinkedList<>();
		for (List<IndexMap> sequence : sequences) {
			if (sequence.size() > 2) {
				resultSequences.add(sequence);
			}
		}

		return resultSequences;
	}

	private static BigDecimal getFragmentSimilarity(Fragment suspiciousFragment, Fragment originalFragment,
			List<List<IndexMap>> sequences) {

		BigDecimal similarity = new BigDecimal("0.0");
		int sumMatchingMeasurements = 0;

		for (List<IndexMap> sequence : sequences) {

			BigDecimal sequenceSimilarity = new BigDecimal("0.0");
			sumMatchingMeasurements = sumMatchingMeasurements + sequence.size();

			for (IndexMap indexMap : sequence) {
				
				BigDecimal measurementSimilarity = new BigDecimal("0.0");
				
				Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements()
						.get(indexMap.getSuspiciousIndex());
				Measurement originalMeasurement = originalFragment.getMeasurements().get(indexMap.getOriginalIndex());

				BigDecimal distance = (suspiciousMeasurement.getValue().subtract(originalMeasurement.getValue())).abs();
				if (distance.compareTo(BigDecimal.valueOf(0.0)) == 0) {
					measurementSimilarity = new BigDecimal("1.0");
				} else {
					measurementSimilarity = BigDecimal.valueOf(1)
							.subtract(distance.divide(originalMeasurement.getValue(), 4, RoundingMode.HALF_UP));
				}

				sequenceSimilarity = sequenceSimilarity.add(measurementSimilarity);
			}

			sequenceSimilarity = sequenceSimilarity.divide(BigDecimal.valueOf(sequence.size()), 4,
					RoundingMode.HALF_UP);
			similarity = similarity.add(sequenceSimilarity);
		}

		if (similarity.compareTo(BigDecimal.valueOf(0.0)) == 0) {
			return BigDecimal.valueOf(0.0);
		}

		similarity = similarity.divide(BigDecimal.valueOf(sequences.size()), 4, RoundingMode.HALF_UP);

		// relativate similarity by invalid measurements
		int missingMeasurements = suspiciousFragment.getMeasurements().size() - sumMatchingMeasurements;
		BigDecimal relativeMatchingMeasurements;
		if (missingMeasurements == 0) {
			relativeMatchingMeasurements = new BigDecimal("1.0");
		} else {
			relativeMatchingMeasurements = BigDecimal.valueOf(sumMatchingMeasurements)
					.divide(BigDecimal.valueOf(missingMeasurements + sumMatchingMeasurements), 4, RoundingMode.HALF_UP);
		}

		similarity = similarity.multiply(relativeMatchingMeasurements);

		return similarity;
	}

	private static BigDecimal[] getWatermark(Fragment suspiciousFragment, Fragment originalFragment,
			List<List<IndexMap>> sequences) {
		BigDecimal[] watermark = new BigDecimal[suspiciousFragment.getMeasurements().size()];

		for (List<IndexMap> sequence : sequences) {
			for (IndexMap indexMap : sequence) {
				watermark[indexMap.getSuspiciousIndex()] = suspiciousFragment.getMeasurements()
						.get(indexMap.getSuspiciousIndex()).getValue()
						.subtract(originalFragment.getMeasurements().get(indexMap.getOriginalIndex()).getValue());
			}
		}
		return watermark;
	}

	// TODO
	private static BigDecimal getWatermarkSimilarity(BigDecimal[] suspiciousWatermark, BigDecimal[] originalWatermark,
			UsabilityConstraint usabilityConstraint, List<List<IndexMap>> sequences) {

		BigDecimal similarity = new BigDecimal("0.0");

		for (List<IndexMap> sequence : sequences) {

			for (IndexMap indexMap : sequence) {

				BigDecimal markSimilarity = new BigDecimal("0.0");
				BigDecimal suspiciousMark = suspiciousWatermark[indexMap.getSuspiciousIndex()];
				BigDecimal originalMark = originalWatermark[indexMap.getOriginalIndex()];

				BigDecimal distance = (suspiciousMark.subtract(originalMark)).abs();

				if (distance.compareTo(BigDecimal.valueOf(0.0)) == 0) {
					markSimilarity = new BigDecimal("1.0");
				} else {
					markSimilarity = BigDecimal.valueOf(1)
							.subtract(distance.divide(usabilityConstraint.getMaximumError(), 4, RoundingMode.HALF_UP));
				}

				similarity = similarity.add(markSimilarity);
			}
		}

		similarity = similarity.divide(BigDecimal.valueOf(suspiciousWatermark.length), 4, RoundingMode.HALF_UP);

		return similarity;
	}

}
