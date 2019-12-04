package services;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import entities.Fragment;
import utils.DatabaseService;
import utils.FileService;
import utils.FragmentationService;

public class DetectionService {

	/**
	 * retrieves suspicious fragments from dataset location. detects watermarks for
	 * data leakage detection. provides a detection report to the report location.
	 * 
	 * @param datasetLocation              location of the suspicious dataset
	 * @param reportLocation               location of the report
	 * @param fragmentSimilarityThreshold  threshold to determine whether a fragment
	 *                                     matches another one
	 * @param watermarkSimilarityThreshold threshold to determine whether a
	 *                                     watermark matches another one
	 */
	public static void getLeakageReport(String datasetLocation, String reportLocation,
			Double fragmentSimilarityThreshold, Double watermarkSimilarityThreshold) {
		// provides a detection report
		provideReport(reportLocation,
				// detects watermarks for data leakage detection
				watermarkDetection(datasetLocation, fragmentSimilarityThreshold, watermarkSimilarityThreshold,
						// retrieve suspicious fragments from file
						FragmentationService.getFragments(datasetLocation)));
	}

	/*
	 * generates file name. writes report to file.
	 */
	private static void provideReport(String reportLocation, String report) {
		// writes report to file
		FileService.writeFile(
				// generates file name
				FileService.getFileName(reportLocation, "detection", "Report", ".txt"), report);
	}

	/*
	 * detects watermarks for data leakage detection.
	 */
	private static String watermarkDetection(String datasetLocation, Double fragmentSimilarityThreshold,
			Double watermarkSimilarityThreshold, List<Fragment> suspiciousFragments) {

		// print header
		String report = "watermark detection report";
		report = report + "\ninvestigated dataset:\t\t\t" + datasetLocation;
		report = report + "\ndate:\t\t\t\t\t\t\t" + LocalDateTime.now();
		report = report + "\nfragment similarity threshold:\t" + fragmentSimilarityThreshold;
		report = report + "\nwatermark similarity threshold:\t" + watermarkSimilarityThreshold;
		report = report + "\n";

		HashMap<Integer, Integer> detectionSummary = new HashMap<>();
		Collections.sort(suspiciousFragments);

		// similarity search for each dataset fragment
		for (int i = 0; i < suspiciousFragments.size(); i++) {
			report = report + "\n" + (i + 1) + ". suspicious fragment";
			Fragment suspiciousFragment = suspiciousFragments.get(i);
			Collections.sort(suspiciousFragment.getMeasurements());

			// print suspicious fragment
			report = report + "\nsuspicious fragment:\t\t\t" + suspiciousFragment.getDeviceId();
			report = report + "\t" + suspiciousFragment.getType();
			report = report + "\t" + suspiciousFragment.getUnit();
			report = report + "\t" + suspiciousFragment.getDate();

			// fragment similarity search
			Fragment matchingOriginalFragment = new Fragment();
			Double fragmentSimilarity = 0.0;
			HashMap<Fragment, Double> matchingOriginalFragments = new HashMap<>();
			// search for mean first, no date dependent search
			/*for (Fragment fragment : DatabaseService.getFragments(suspiciousFragment.getDeviceId(),
					suspiciousFragment.getType(), suspiciousFragment.getUnit(), suspiciousFragment.getDate())) {

				Collections.sort(fragment.getMeasurements());
				Double currentFragmentSimilarity = getFragmentSimilarity(suspiciousFragment, fragment, 1.0);

				if (currentFragmentSimilarity >= fragmentSimilarityThreshold) {
					matchingOriginalFragments.put(fragment, currentFragmentSimilarity);
				}

				if (currentFragmentSimilarity > fragmentSimilarity) {
					fragmentSimilarity = currentFragmentSimilarity;
					matchingOriginalFragment = fragment;
				}
			}*/

			if (fragmentSimilarity >= fragmentSimilarityThreshold) {

				// print matching original fragment
				report = report + "\nmatching original fragment:\t\t" + matchingOriginalFragment.getDeviceId();
				report = report + "\t" + matchingOriginalFragment.getType();
				report = report + "\t" + matchingOriginalFragment.getUnit();
				report = report + "\t" + matchingOriginalFragment.getDate();
				report = report + "\nmatching fragment similarity:\t" + round(fragmentSimilarity, 2);

				// extract embedded watermark
				Double[] noisyWatermark = new Double[suspiciousFragment.getMeasurements().size()];
				for (int j = 0; j < noisyWatermark.length; j++) {
					/*noisyWatermark[j] = suspiciousFragment.getMeasurements().get(j).getValue()
							- matchingOriginalFragment.getMeasurements().get(j).getValue();*/
				}
				report = report + "\nextracted noisy watermark:\t\t" + Arrays.toString(noisyWatermark);

				// watermark similarity search
				Double[] matchingOriginalWatermark = new Double[matchingOriginalFragment.getMeasurements().size()];
				Double watermarkSimilarity = 0.0;
				HashMap<Double[], Double> matchingOriginalWatermarks = new HashMap<>();
				/*for (Double[] watermark : DatabaseService.getWatermarks(matchingOriginalFragment.getDeviceId(),
						matchingOriginalFragment.getType(), matchingOriginalFragment.getUnit(),
						matchingOriginalFragment.getDate())) {

					Double currentWatermarkSimilarity = getWatermarkSimilarity(noisyWatermark, watermark, 0.5);

					if (currentWatermarkSimilarity >= watermarkSimilarityThreshold) {
						matchingOriginalWatermarks.put(watermark, currentWatermarkSimilarity);
					}

					if (currentWatermarkSimilarity > watermarkSimilarity) {
						watermarkSimilarity = currentWatermarkSimilarity;
						matchingOriginalWatermark = watermark;
					}
				}*/

				if (watermarkSimilarity >= watermarkSimilarityThreshold) {
					// print matching original watermark
					report = report + "\noriginal matching watermark:\t" + Arrays.toString(matchingOriginalWatermark);
					report = report + "\nmatching watermark similarity:\t" + round(watermarkSimilarity, 2);

					// detect data leaker
					int dataLeaker = DatabaseService.getDataUserId(matchingOriginalWatermark,
							matchingOriginalFragment.getDeviceId(), matchingOriginalFragment.getType(),
							matchingOriginalFragment.getUnit(), matchingOriginalFragment.getDate());
					report = report + "\ndetected data leaker:\t\t\t" + dataLeaker;
					detectionSummary.put(i + 1, dataLeaker);

				} else {
					report = report + "\nmatching watermark similarity:\t" + watermarkSimilarity;
					report = report + "\nno data leaker detected";
					detectionSummary.put(i + 1, 0);
				}

			} else {
				report = report + "\nmatching fragment similarity:\t" + fragmentSimilarity;
				report = report + "\nno data leaker detected";
				detectionSummary.put(i + 1, 0);
			}
			report = report + "\n";
		}

		// print detection summary
		report = report + "\n\ndetection summary";
		HashMap<Integer, Integer> leakerSummary = new HashMap<>();
		for (Map.Entry<Integer, Integer> entry : detectionSummary.entrySet()) {
			report = report + "\nfragment: " + entry.getKey() + ",\tdetected data leaker: " + entry.getValue();

			if (leakerSummary.containsKey(entry.getValue())) {
				int count = leakerSummary.get(entry.getValue());
				leakerSummary.replace(entry.getValue(), count, count + 1);
			} else {
				leakerSummary.put(entry.getValue(), 1);
			}
		}

		// print leaker summary
		report = report + "\n\nleaker summary";
		for (Map.Entry<Integer, Integer> entry : leakerSummary.entrySet()) {
			report = report + "\nleaker: " + entry.getKey() + ",\tabsolute leakage: " + entry.getValue()
					+ ",\trelative leakage: " + round((double) entry.getValue() / (double) detectionSummary.size(), 2);
		}

		return report;
	}

	/*
	 * get similarity between two fragments considering a distance threshold
	 */
	private static Double getFragmentSimilarity(Fragment suspiciousFragment, Fragment originalFragment,
			double threshold) {
		Double similarity = 0.0;

		// calculate distances
		List<Double> distances = new LinkedList<>();
		double totalDistance = 0.0;
		for (int i = 0; i < originalFragment.getMeasurements().size(); i++) {
			double distance = 0.0; //suspiciousFragment.getMeasurements().get(i).getValue() - originalFragment.getMeasurements().get(i).getValue();
			distances.add(distance);
			totalDistance = totalDistance + distance;
		}
		double meanDistance = Math.abs(totalDistance) / originalFragment.getMeasurements().size();

		// calculate similarity
		similarity = (threshold - meanDistance) / threshold;

		return similarity;
	}

	/*
	 * get similarity between two watermarks considering a distance threshold
	 */
	private static Double getWatermarkSimilarity(Double[] suspiciousWatermark, Double[] originalWatermark,
			double threshold) {
		Double similarity = 0.0;

		// calculate distances
		List<Double> distances = new LinkedList<>();
		double totalDistance = 0.0;
		for (int i = 0; i < originalWatermark.length; i++) {
			double distance = suspiciousWatermark[i] - originalWatermark[i];
			distances.add(distance);
			totalDistance = totalDistance + distance;
		}
		double meanDistance = Math.abs(totalDistance) / originalWatermark.length;

		// calculate similarity
		similarity = (threshold - meanDistance) / threshold;

		return similarity;
	}

	/*
	 * rounds a double value based on a given number of places
	 */
	public static double round(double value, int places) {
		return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}
}
