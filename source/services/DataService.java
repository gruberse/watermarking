package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Bin;
import entities.DataProfile;
import entities.Fragment;
import entities.Measurement;
import entities.Request;
import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	public static void getDataset(String fileName, int dataUserId, String deviceId, String type, String unit,
			int frequency, LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(deviceId, type, unit, frequency, from, to);
		List<Fragment> watermarkedFragments = watermarkEmbedding(dataUserId, originalFragments);
		// provideDataset(folderLocation, dataUserId, watermarkedFragments);
	}

	/*
	 * public static void getDataset(String folderLocation, int dataUserId, int
	 * noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
	 * List<Fragment> originalFragments = DatabaseService.getFragments(noOfDevices,
	 * type, unit, from, to); List<Fragment> watermarkedFragments =
	 * watermarkEmbedding(dataUserId, type, unit, originalFragments);
	 * provideDataset(folderLocation, dataUserId, watermarkedFragments); }
	 */

	private static List<Fragment> watermarkEmbedding(int dataUserId, List<Fragment> fragments) {
		LocalDateTime timestamp = LocalDateTime.now();

		Collections.sort(fragments);
		for (int i = 0; i < fragments.size(); i++) {
			Fragment fragment = fragments.get(i);

			// retrieve usability constraint
			UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(fragment.getType(),
					fragment.getUnit(), fragment.getFrequency());

			// retrieve number of watermark from database
			Request request = DatabaseService.getRequest(dataUserId, fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getFrequency(), fragment.getDate());

			// data user has not requested fragment yet
			if (request == null) {

				// retrieve next watermark number
				int numberOfWatermark = 1 + DatabaseService.getNumberOfWatermark(fragment.getDeviceId(),
						fragment.getType(), fragment.getUnit(), fragment.getFrequency(), fragment.getDate());
				// limit number of watermarks
				if (numberOfWatermark > usabilityConstraint.getNumberOfWatermarks()) {
					numberOfWatermark = numberOfWatermark % usabilityConstraint.getNumberOfWatermarks();
				}

				ArrayList<LocalDateTime> timestamps = new ArrayList<>();
				timestamps.add(timestamp);

				request = new Request(fragment.getDeviceId(), dataUserId, fragment.getType(), fragment.getUnit(),
						fragment.getFrequency(), fragment.getDate(), numberOfWatermark, timestamps);

				// insert new request
				DatabaseService.insertRequest(request);
			}
			// data user has already requested fragment
			else {
				request.getTimestamps().add(timestamp);

				// update existing request
				DatabaseService.updateRequest(request);
			}

			// retrieve data profile
			DataProfile dataProfile = DatabaseService.getDataProfile(fragment.getDatasetId(), fragment.getDeviceId(),
					fragment.getType(), fragment.getUnit(), fragment.getFrequency());

			// watermark generation and embedding
			// possible overflow in seed
			Random prng = new Random(fragment.getSecretKey() - Long.valueOf(request.getNumberOfWatermark()));
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {

				Measurement measurement = fragment.getMeasurements().get(j);

				// compute probabilities for each value range based on distributions
				// (...) - 1 skips last invalid range
				// numberOfRanges = (maximumError * 2 / valueBinSize) - 1
				BigDecimal numberOfRanges = (usabilityConstraint.getMaximumError().multiply(new BigDecimal(2))
						.divide(dataProfile.getValueBinSize())).subtract(new BigDecimal(1));

				List<Bin<BigDecimal>> valueRange = new LinkedList<Bin<BigDecimal>>();
				BigDecimal[][] rangeProbabilities = new BigDecimal[numberOfRanges.intValue()][6];
				BigDecimal valueSum = new BigDecimal("0.0"); 
				BigDecimal slopeSum_1 = new BigDecimal("0.0");
				BigDecimal slopeSum_2 = new BigDecimal("0.0");
				BigDecimal curvatureSum_1 = new BigDecimal("0.0");
				BigDecimal curvatureSum_2 = new BigDecimal("0.0");
				BigDecimal curvatureSum_3 = new BigDecimal("0.0");

				for (int k = 0; k < numberOfRanges.intValue(); k++) {

					// compute range k [minimum, maximum)
					// (k + 1) skips first invalid range
					// minimum = (round(value(t), down) - maximumError) + ((k + 1) * valueBinSize)
					BigDecimal valueMinimum = (measurement.getValue()
							.setScale(dataProfile.getValueBinSize().scale(), RoundingMode.DOWN)
							.subtract(usabilityConstraint.getMaximumError()))
									.add(new BigDecimal(k + 1).multiply(dataProfile.getValueBinSize()));
					// maximum = (round(value(t), up) - maximumError) + ((k + 1) * valueBinSize)
					BigDecimal valueMaximum = (measurement.getValue()
							.setScale(dataProfile.getValueBinSize().scale(), RoundingMode.UP)
							.subtract(usabilityConstraint.getMaximumError()))
									.add(new BigDecimal(k + 1).multiply(dataProfile.getValueBinSize()));

					// check valid minimum value range
					if (valueMinimum.compareTo(usabilityConstraint.getMinimumValue()) <= 0) {
						valueMinimum = usabilityConstraint.getMinimumValue();
					}
					// check valid maximum value range
					if (valueMaximum.compareTo(usabilityConstraint.getMaximumValue()) >= 0) {
						valueMaximum = usabilityConstraint.getMaximumValue();
					}

					valueRange.add(new Bin<BigDecimal>(valueMinimum, valueMaximum, new BigDecimal("0.0")));

					// compute probability for value v(t) in range k
					BigDecimal valueProbability = new BigDecimal("0.0");
					for (Bin<BigDecimal> bin : dataProfile.getRelativeValueDistributionBins(valueMinimum,
							valueMaximum)) {
						valueProbability = valueProbability.add(bin.getValue());
					}

					// compute slope_1(t-1,t) probability in range k
					// slope_1(t-1,t) = value(t) - value(t-1)
					BigDecimal slopeProbability_1 = new BigDecimal("0.0");
					if (j > 0) {
						BigDecimal slopeMinimum_1 = valueMinimum
								.subtract(fragment.getMeasurements().get(j - 1).getValue());
						BigDecimal slopeMaximum_1 = valueMaximum
								.subtract(fragment.getMeasurements().get(j - 1).getValue());

						for (Bin<BigDecimal> bin : dataProfile.getRelativeSlopeDistributionBins(slopeMinimum_1,
								slopeMaximum_1)) {
							slopeProbability_1 = slopeProbability_1.add(bin.getValue());
						}
					}

					// compute slope_2(t,t+1) probability in range k
					// slope_2(t,t+1) = value(t+1) - value(t)
					BigDecimal slopeProbability_2 = new BigDecimal("0.0");
					if (j + 1 < fragment.getMeasurements().size()) {
						BigDecimal slopeMinimum_2 = fragment.getMeasurements().get(j + 1).getValue()
								.subtract(valueMaximum);
						BigDecimal slopeMaximum_2 = fragment.getMeasurements().get(j + 1).getValue()
								.subtract(valueMinimum);

						for (Bin<BigDecimal> bin : dataProfile.getRelativeSlopeDistributionBins(slopeMinimum_2,
								slopeMaximum_2)) {
							slopeProbability_2 = slopeProbability_2.add(bin.getValue());
						}
					}

					// compute curvature_1(t-1) probability in range k
					// curvature_1(t-1) = (value(t) - value(t-1)) - (value(t-1)-value(t-2))
					BigDecimal curvatureProbability_1 = new BigDecimal("0.0");
					if (j > 1) {
						BigDecimal curvatureMinimum_1 = (valueMinimum
								.subtract(fragment.getMeasurements().get(j - 1).getValue()))
										.subtract(fragment.getMeasurements().get(j - 1).getValue()
												.subtract(fragment.getMeasurements().get(j - 2).getValue()));
						BigDecimal curvatureMaximum_1 = (valueMaximum
								.subtract(fragment.getMeasurements().get(j - 1).getValue()))
										.subtract(fragment.getMeasurements().get(j - 1).getValue()
												.subtract(fragment.getMeasurements().get(j - 2).getValue()));

						for (Bin<BigDecimal> bin : dataProfile.getRelativeCurvatureDistributionBins(curvatureMinimum_1,
								curvatureMaximum_1)) {
							curvatureProbability_1 = curvatureProbability_1.add(bin.getValue());
						}
					}

					// compute curvature_2(t) probability in range k
					// curvature_2(t) = (value(t+1) - value(t)) - (value(t)-value(t-1))
					BigDecimal curvatureProbability_2 = new BigDecimal("0.0");
					if (j > 0 && j + 1 < fragment.getMeasurements().size()) {
						BigDecimal curvatureMinimum_2 = (fragment.getMeasurements().get(j + 1).getValue()
								.subtract(valueMaximum)).subtract(
										valueMaximum.subtract(fragment.getMeasurements().get(j - 1).getValue()));
						BigDecimal curvatureMaximum_2 = (fragment.getMeasurements().get(j + 1).getValue()
								.subtract(valueMinimum)).subtract(
										valueMinimum.subtract(fragment.getMeasurements().get(j - 1).getValue()));

						for (Bin<BigDecimal> bin : dataProfile.getRelativeCurvatureDistributionBins(curvatureMinimum_2,
								curvatureMaximum_2)) {
							curvatureProbability_2 = curvatureProbability_2.add(bin.getValue());
						}
					}

					// compute curvature_3(t+1) probability in range k
					// curvature_3(t+1) = (value(t+2) - value(t+1)) - (value(t+1)-value(t))
					BigDecimal curvatureProbability_3 = new BigDecimal("0.0");
					if (j + 2 < fragment.getMeasurements().size()) {
						BigDecimal curvatureMinimum_3 = (fragment.getMeasurements().get(j + 2).getValue()
								.subtract(fragment.getMeasurements().get(j + 1).getValue())).subtract(
										fragment.getMeasurements().get(j + 1).getValue().subtract(valueMinimum));
						BigDecimal curvatureMaximum_3 = (fragment.getMeasurements().get(j + 2).getValue()
								.subtract(fragment.getMeasurements().get(j + 1).getValue())).subtract(
										fragment.getMeasurements().get(j + 1).getValue().subtract(valueMaximum));

						for (Bin<BigDecimal> bin : dataProfile.getRelativeCurvatureDistributionBins(curvatureMinimum_3,
								curvatureMaximum_3)) {
							curvatureProbability_3 = curvatureProbability_3.add(bin.getValue());
						}
					}

					// set probabilities
					rangeProbabilities[k][0] = valueProbability;
					rangeProbabilities[k][1] = slopeProbability_1;
					rangeProbabilities[k][2] = slopeProbability_2;
					rangeProbabilities[k][3] = curvatureProbability_1;
					rangeProbabilities[k][4] = curvatureProbability_2;
					rangeProbabilities[k][5] = curvatureProbability_3;
					
					// collect probability sums
					valueSum = valueSum.add(valueProbability);
					slopeSum_1 = slopeSum_1.add(slopeProbability_1);
					slopeSum_2 = slopeSum_2.add(slopeProbability_2);
					curvatureSum_1 = curvatureSum_1.add(curvatureProbability_1);
					curvatureSum_2 = curvatureSum_2.add(curvatureProbability_2);
					curvatureSum_3 = curvatureSum_3.add(curvatureProbability_3);
				}

				// compute final probabilities
				for(int k = 0; k < rangeProbabilities.length; k++) {
					BigDecimal relativeValueProbability, relativeSlopeProbability_1, relativeSlopeProbability_2, relativeCurvatureProbability_1,
					relativeCurvatureProbability_2, relativeCurvatureProbability_3;
					if (valueSum.compareTo(new BigDecimal("0.0")) == 0) {
						relativeValueProbability = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeValueProbability = rangeProbabilities[k][0].divide(valueSum, 4, RoundingMode.HALF_UP);
					}
					if (slopeSum_1.compareTo(new BigDecimal("0.0")) == 0) {
						relativeSlopeProbability_1 = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeSlopeProbability_1 = rangeProbabilities[k][1].divide(slopeSum_1, 4, RoundingMode.HALF_UP);
					}
					if (slopeSum_2.compareTo(new BigDecimal("0.0")) == 0) {
						relativeSlopeProbability_2 = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeSlopeProbability_2 = rangeProbabilities[k][2].divide(slopeSum_2, 4, RoundingMode.HALF_UP);
					}
					if (curvatureSum_1.compareTo(new BigDecimal("0.0")) == 0) {
						relativeCurvatureProbability_1 = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeCurvatureProbability_1 = rangeProbabilities[k][3].divide(curvatureSum_1, 4, RoundingMode.HALF_UP);
					}
					if (curvatureSum_2.compareTo(new BigDecimal("0.0")) == 0) {
						relativeCurvatureProbability_2 = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeCurvatureProbability_2 = rangeProbabilities[k][4].divide(curvatureSum_2, 4, RoundingMode.HALF_UP);
					}
					if (curvatureSum_3.compareTo(new BigDecimal("0.0")) == 0) {
						relativeCurvatureProbability_3 = (new BigDecimal("1.0"))
								.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
					} else {
						relativeCurvatureProbability_3 = rangeProbabilities[k][5].divide(curvatureSum_3, 4, RoundingMode.HALF_UP);
					}
					valueRange.get(k).setValue((relativeValueProbability.add(relativeSlopeProbability_1).add(relativeSlopeProbability_2).add(relativeCurvatureProbability_1)
							.add(relativeCurvatureProbability_2).add(relativeCurvatureProbability_3)).divide(new BigDecimal("6.0"), 2, RoundingMode.HALF_UP));
				}
				
				System.out.println("VALUE: " + measurement.getValue());
				for (int k = 0; k < numberOfRanges.intValue(); k++) {
					System.out.println(
							valueRange.get(k) + "   \t" + rangeProbabilities[k][0] + "\t" + rangeProbabilities[k][1]
									+ "\t" + rangeProbabilities[k][2] + "\t" + rangeProbabilities[k][3] + "\t"
									+ rangeProbabilities[k][4] + "\t" + rangeProbabilities[k][5]);
				}

				// measurement.setValue(measurement.getValue().add(new BigDecimal(100)));
				fragment.getMeasurements().set(j, measurement);
			}
			fragments.set(i, fragment);
		}
		return fragments;
	}

	private static void provideDataset(String requestLocation, int dataUserId, List<Fragment> fragments) {
		String json = "[";
		for (int i = 0; i < fragments.size(); i++) {
			json = json + fragments.get(i).getMeasurementsAsJsonString();
			if (i + 1 < fragments.size())
				json = json + ",";
		}
		json = json + "\n]";

		String fileName = FileService.getFileName(requestLocation, "dataUser" + dataUserId, "request", ".json");
		FileService.writeFile(fileName, json);
	}
}
