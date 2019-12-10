package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

			// retrieve original fragments with similar means
			UsabilityConstraint usabilityConstraint = DatabaseService
					.getUsabilityConstraint(suspiciousFragment.getType(), suspiciousFragment.getUnit());
			BigDecimal mean = suspiciousFragment.getMean();
			List<Fragment> similarMeanFragments = DatabaseService.getFragments(suspiciousFragment.getType(),
					suspiciousFragment.getUnit(), mean.subtract(usabilityConstraint.getMaximumError()),
					mean.add(usabilityConstraint.getMaximumError()));

			for (Fragment fragment : similarMeanFragments) {

				// fragment similarity search
				BigDecimal fragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment);

				// set highest similarity fragment
				if (fragmentSimilarity.compareTo(matchingFragmentSimilarity) > 0) {
					matchingFragmentSimilarity = fragmentSimilarity;
					matchingFragment = fragment;
				}
			}

			if (matchingFragmentSimilarity.compareTo(fragmentSimilarityThreshold) >= 0) {

				// print matching original fragment
				report = report + "\nmatching original fragment:\t" + matchingFragment.getDeviceId();
				report = report + "\t" + matchingFragment.getType();
				report = report + "\t" + matchingFragment.getUnit();
				report = report + "\t" + matchingFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + matchingFragmentSimilarity;

				// extract embedded watermark
				BigDecimal[] noisyWatermark = getWatermark(suspiciousFragment, matchingFragment);

				List<DataLeaker> fragmentLeakers = new LinkedList<>();

				// retrieve requests on matching fragment
				List<Request> requests = DatabaseService.getRequests(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate());

				Fragment prevFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(),
						matchingFragment.getDate().minusDays(1));
				Fragment nextFragment = DatabaseService.getFragment(matchingFragment.getDeviceId(),
						matchingFragment.getType(), matchingFragment.getUnit(), matchingFragment.getDate().plusDays(1));

				// simple watermark //TODO consider combinations
				for (Request request : requests) {
					BigDecimal[] watermark = WatermarkGenerationService.generateWatermark_w2(request,
							usabilityConstraint, matchingFragment, prevFragment, nextFragment);

					// watermark similarity search
					BigDecimal watermarkSimilarity = getWatermarkSimilarity(noisyWatermark, watermark, usabilityConstraint);

					// add to list if above threshold
					if (watermarkSimilarity.compareTo(watermarkSimilarityThreshold) >= 0) {
						List<Integer> dataUserIds = new LinkedList<>();
						dataUserIds.add(request.getDataUserId());
						DataLeaker leaker = new DataLeaker(watermarkSimilarity, dataUserIds);
						fragmentLeakers.add(leaker);
					}
				}

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
			report = report + "\n";
		}
		System.out.println(report);
	}
	
	private static BigDecimal[] getWatermark(Fragment suspiciousFragment, Fragment originalFragment) {
		BigDecimal[] watermark = new BigDecimal[suspiciousFragment.getMeasurements().size()];

		// consider subset and deletion attack
		for (int i = 0; i < watermark.length; i++) {
			watermark[i] = suspiciousFragment.getMeasurements().get(i).getValue()
					.subtract(originalFragment.getMeasurements().get(i).getValue());
		}
		return watermark;
	}

	private static BigDecimal getFragmentSimilarity(Fragment suspiciousFragment, Fragment originalFragment) {

		// suspicious fragment is already sorted
		Collections.sort(originalFragment.getMeasurements());

		BigDecimal similarity = new BigDecimal("0.0");

		// calculate distances
		if (suspiciousFragment.getMeasurements().size() == originalFragment.getMeasurements().size()) {
			for (int i = 0; i < originalFragment.getMeasurements().size(); i++) {

				Measurement suspiciousMeasurement = suspiciousFragment.getMeasurements().get(i);
				Measurement originalMeasurement = originalFragment.getMeasurements().get(i);

				BigDecimal distance = (suspiciousMeasurement.getValue().subtract(originalMeasurement.getValue())).abs();
				BigDecimal measurementSimilarity = BigDecimal.valueOf(1)
						.subtract(distance.divide(originalMeasurement.getValue(), 4, RoundingMode.HALF_UP));

				// sum measurement similarities
				similarity = similarity.add(measurementSimilarity);
			}

			// calculate mean similarity
			similarity = similarity.divide(BigDecimal.valueOf(suspiciousFragment.getMeasurements().size()), 4,
					RoundingMode.HALF_UP);
		} else {
			// deletion & subset
		}

		return similarity;
	}

	private static BigDecimal getWatermarkSimilarity(BigDecimal[] suspiciousWatermark, BigDecimal[] originalWatermark,
			UsabilityConstraint usabilityConstraint) {
		BigDecimal similarity = new BigDecimal("0.0");

		// calculate distances
		if (suspiciousWatermark.length == originalWatermark.length) {
			for (int i = 0; i < originalWatermark.length; i++) {

				BigDecimal markSimilarity = new BigDecimal("0.0");
				BigDecimal distance = (suspiciousWatermark[i].subtract(originalWatermark[i])).abs();

				markSimilarity = BigDecimal.valueOf(1.0)
						.subtract(distance.divide(usabilityConstraint.getMaximumError(), 4, RoundingMode.HALF_UP));

				// sum measurement similarities
				similarity = similarity.add(markSimilarity);
			}

			// calculate mean similarity
			similarity = similarity.divide(BigDecimal.valueOf(suspiciousWatermark.length), 4, RoundingMode.HALF_UP);
		} else {
			// deletion & subset
		}

		return similarity;
	}

}
