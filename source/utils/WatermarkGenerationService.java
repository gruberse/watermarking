package utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Range;
import entities.DataProfile;
import entities.Fragment;
import entities.Measurement;
import entities.Request;
import entities.UsabilityConstraint;

public class WatermarkGenerationService {

	public static BigDecimal[] generateWatermark_w1(Request request, UsabilityConstraint usabilityConstraint,
			DataProfile dataProfile, Fragment fragment) {

		BigDecimal[] watermark = new BigDecimal[fragment.getMeasurements().size()];
		Random prng = new Random(fragment.getSecretKey() - Long.valueOf(request.getNumberOfWatermark()));

		Collections.sort(fragment.getMeasurements());
		for (int i = 0; i < fragment.getMeasurements().size(); i++) {

			Measurement measurement = fragment.getMeasurements().get(i);

			// final probabilities of value range
			List<Range<BigDecimal>> valueRange = new LinkedList<Range<BigDecimal>>();
			// intermediate probabilities
			BigDecimal[][] rangeProbabilities = new BigDecimal[usabilityConstraint.getNumberOfRanges() - 1][6];
			// sums to calculate final probability
			BigDecimal valueSum = new BigDecimal("0.0");
			BigDecimal slopeSum_1 = new BigDecimal("0.0");
			BigDecimal slopeSum_2 = new BigDecimal("0.0");
			BigDecimal curvatureSum_1 = new BigDecimal("0.0");
			BigDecimal curvatureSum_2 = new BigDecimal("0.0");
			BigDecimal curvatureSum_3 = new BigDecimal("0.0");
			
			BigDecimal valueRangeSize = BigDecimal.valueOf(0.1);
					//usabilityConstraint.getMaximumError().multiply(new BigDecimal(2))
					//.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges()));

			// skip last invalid range --> k + 1
			for (int k = 0; k + 1 < usabilityConstraint.getNumberOfRanges(); k++) {

				// compute range k [minimum, maximum)
				// (k + 1) skips first invalid range
				// minimum = (round(value(t), down) - maximumError) + ((k + 1) * valueRangeSize)
				BigDecimal valueMinimum = (measurement.getValue()
						.setScale(valueRangeSize.scale(), RoundingMode.DOWN)
						.subtract(usabilityConstraint.getMaximumError()))
								.add(new BigDecimal(k + 1).multiply(valueRangeSize));
				// maximum = (round(value(t), up) - maximumError) + ((k + 1) * valueRangeSize)
				BigDecimal valueMaximum = (measurement.getValue()
						.setScale(valueRangeSize.scale(), RoundingMode.UP)
						.subtract(usabilityConstraint.getMaximumError()))
								.add(new BigDecimal(k + 1).multiply(valueRangeSize));

				// check valid minimum value range
				if (valueMinimum.compareTo(usabilityConstraint.getMinimumValue()) <= 0) {
					valueMinimum = usabilityConstraint.getMinimumValue();
				}
				// check valid maximum value range
				if (valueMaximum.compareTo(usabilityConstraint.getMaximumValue()) >= 0) {
					valueMaximum = usabilityConstraint.getMaximumValue();
				}

				// initialize ranges with total relative probabilities
				valueRange.add(new Range<BigDecimal>(valueMinimum, valueMaximum, new BigDecimal("0.0")));

				// compute probability for value v(t) in range k
				BigDecimal valueProbability = new BigDecimal("0.0");
				for (Range<BigDecimal> range : dataProfile.getRelativeValueDistributionRanges(valueMinimum, valueMaximum)) {
					valueProbability = valueProbability.add(range.getValue());
				}

				// compute slope_1(t-1,t) probability in range k
				// slope_1(t-1,t) = value(t) - value(t-1)
				BigDecimal slopeProbability_1 = new BigDecimal("0.0");
				if (i > 0) {
					BigDecimal slopeMinimum_1 = valueMinimum.subtract(fragment.getMeasurements().get(i - 1).getValue());
					BigDecimal slopeMaximum_1 = valueMaximum.subtract(fragment.getMeasurements().get(i - 1).getValue());

					for (Range<BigDecimal> range : dataProfile.getRelativeSlopeDistributionRanges(slopeMinimum_1,
							slopeMaximum_1)) {
						slopeProbability_1 = slopeProbability_1.add(range.getValue());
					}
				}

				// compute slope_2(t,t+1) probability in range k
				// slope_2(t,t+1) = value(t+1) - value(t)
				BigDecimal slopeProbability_2 = new BigDecimal("0.0");
				if (i + 1 < fragment.getMeasurements().size()) {
					BigDecimal slopeMinimum_2 = fragment.getMeasurements().get(i + 1).getValue().subtract(valueMaximum);
					BigDecimal slopeMaximum_2 = fragment.getMeasurements().get(i + 1).getValue().subtract(valueMinimum);

					for (Range<BigDecimal> range : dataProfile.getRelativeSlopeDistributionRanges(slopeMinimum_2,
							slopeMaximum_2)) {
						slopeProbability_2 = slopeProbability_2.add(range.getValue());
					}
				}

				// compute curvature_1(t-1) probability in range k
				// curvature_1(t-1) = (value(t) - value(t-1)) - (value(t-1)-value(t-2))
				BigDecimal curvatureProbability_1 = new BigDecimal("0.0");
				if (i > 1) {
					BigDecimal curvatureMinimum_1 = (valueMinimum
							.subtract(fragment.getMeasurements().get(i - 1).getValue()))
									.subtract(fragment.getMeasurements().get(i - 1).getValue()
											.subtract(fragment.getMeasurements().get(i - 2).getValue()));
					BigDecimal curvatureMaximum_1 = (valueMaximum
							.subtract(fragment.getMeasurements().get(i - 1).getValue()))
									.subtract(fragment.getMeasurements().get(i - 1).getValue()
											.subtract(fragment.getMeasurements().get(i - 2).getValue()));

					for (Range<BigDecimal> range : dataProfile.getRelativeCurvatureDistributionRanges(curvatureMinimum_1,
							curvatureMaximum_1)) {
						curvatureProbability_1 = curvatureProbability_1.add(range.getValue());
					}
				}

				// compute curvature_2(t) probability in range k
				// curvature_2(t) = (value(t+1) - value(t)) - (value(t)-value(t-1))
				BigDecimal curvatureProbability_2 = new BigDecimal("0.0");
				if (i > 0 && i + 1 < fragment.getMeasurements().size()) {
					BigDecimal curvatureMinimum_2 = (fragment.getMeasurements().get(i + 1).getValue()
							.subtract(valueMaximum))
									.subtract(valueMaximum.subtract(fragment.getMeasurements().get(i - 1).getValue()));
					BigDecimal curvatureMaximum_2 = (fragment.getMeasurements().get(i + 1).getValue()
							.subtract(valueMinimum))
									.subtract(valueMinimum.subtract(fragment.getMeasurements().get(i - 1).getValue()));

					for (Range<BigDecimal> range : dataProfile.getRelativeCurvatureDistributionRanges(curvatureMinimum_2,
							curvatureMaximum_2)) {
						curvatureProbability_2 = curvatureProbability_2.add(range.getValue());
					}
				}

				// compute curvature_3(t+1) probability in range k
				// curvature_3(t+1) = (value(t+2) - value(t+1)) - (value(t+1)-value(t))
				BigDecimal curvatureProbability_3 = new BigDecimal("0.0");
				if (i + 2 < fragment.getMeasurements().size()) {
					BigDecimal curvatureMinimum_3 = (fragment.getMeasurements().get(i + 2).getValue()
							.subtract(fragment.getMeasurements().get(i + 1).getValue()))
									.subtract(fragment.getMeasurements().get(i + 1).getValue().subtract(valueMinimum));
					BigDecimal curvatureMaximum_3 = (fragment.getMeasurements().get(i + 2).getValue()
							.subtract(fragment.getMeasurements().get(i + 1).getValue()))
									.subtract(fragment.getMeasurements().get(i + 1).getValue().subtract(valueMaximum));

					for (Range<BigDecimal> range : dataProfile.getRelativeCurvatureDistributionRanges(curvatureMinimum_3,
							curvatureMaximum_3)) {
						curvatureProbability_3 = curvatureProbability_3.add(range.getValue());
					}
				}

				// collect probabilities
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
			for (int k = 0; k < rangeProbabilities.length; k++) {
				BigDecimal relativeValueProbability, relativeSlopeProbability_1, relativeSlopeProbability_2,
						relativeCurvatureProbability_1, relativeCurvatureProbability_2, relativeCurvatureProbability_3;

				if (valueSum.compareTo(new BigDecimal("0.0")) == 0) {
					relativeValueProbability = (new BigDecimal("1.0")).divide(new BigDecimal(rangeProbabilities.length),
							4, RoundingMode.HALF_UP);
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
					relativeCurvatureProbability_1 = rangeProbabilities[k][3].divide(curvatureSum_1, 4,
							RoundingMode.HALF_UP);
				}
				if (curvatureSum_2.compareTo(new BigDecimal("0.0")) == 0) {
					relativeCurvatureProbability_2 = (new BigDecimal("1.0"))
							.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
				} else {
					relativeCurvatureProbability_2 = rangeProbabilities[k][4].divide(curvatureSum_2, 4,
							RoundingMode.HALF_UP);
				}
				if (curvatureSum_3.compareTo(new BigDecimal("0.0")) == 0) {
					relativeCurvatureProbability_3 = (new BigDecimal("1.0"))
							.divide(new BigDecimal(rangeProbabilities.length), 4, RoundingMode.HALF_UP);
				} else {
					relativeCurvatureProbability_3 = rangeProbabilities[k][5].divide(curvatureSum_3, 4,
							RoundingMode.HALF_UP);
				}
				
				// weight probabilities
				relativeValueProbability = relativeValueProbability.multiply(BigDecimal.valueOf(1));
				relativeSlopeProbability_1 = relativeSlopeProbability_1.multiply(BigDecimal.valueOf(1));
				relativeSlopeProbability_2 = relativeSlopeProbability_2.multiply(BigDecimal.valueOf(1));
				relativeCurvatureProbability_1 = relativeCurvatureProbability_1.multiply(BigDecimal.valueOf(1));
				relativeCurvatureProbability_2 = relativeCurvatureProbability_2.multiply(BigDecimal.valueOf(1));
				relativeCurvatureProbability_3 = relativeCurvatureProbability_3.multiply(BigDecimal.valueOf(1));
				
				// add probabilities
				valueRange.get(k)
						.setValue((relativeValueProbability.add(relativeSlopeProbability_1)
								.add(relativeSlopeProbability_2).add(relativeCurvatureProbability_1)
								.add(relativeCurvatureProbability_2).add(relativeCurvatureProbability_3))
										.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP));
			}
			
			// select value range
			Range<BigDecimal> selectedValueRange = new Range<BigDecimal>(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
			BigDecimal randomNumber = BigDecimal.valueOf(prng.nextDouble()).setScale(2, RoundingMode.DOWN);
			BigDecimal actualValueRange = new BigDecimal("0.0");
			for (int k = 0; k < valueRange.size(); k++) {
				Range<BigDecimal> range = valueRange.get(k);
				actualValueRange = actualValueRange.add(range.getValue());
				if (randomNumber.compareTo(actualValueRange) <= 0) {
					selectedValueRange = new Range<BigDecimal>(range.getMinimum(), range.getMaximum());
					break;
				}
			}

			// compute mark i of watermark
			// (min + (max - min) * random) - value
			BigDecimal randomNumber4ValueSelection = BigDecimal.valueOf(prng.nextDouble());
			watermark[i] = (selectedValueRange.getMinimum()
					.add((selectedValueRange.getMaximum().subtract(selectedValueRange.getMinimum()))
							.multiply(randomNumber4ValueSelection))).subtract(measurement.getValue());
			watermark[i] = watermark[i].setScale(15, RoundingMode.HALF_UP);
		}

		return watermark;
	}

	public static BigDecimal[] generateWatermark_w2(Request request, UsabilityConstraint usabilityConstraint,
			Fragment fragment, Fragment prevFragment, Fragment nextFragment) {

		BigDecimal[] watermark = new BigDecimal[fragment.getMeasurements().size()];
		Random prng = new Random(fragment.getSecretKey() - Long.valueOf(request.getNumberOfWatermark()));

		Collections.sort(fragment.getMeasurements());
		for (int i = 0; i < fragment.getMeasurements().size(); i++) {

			Measurement measurement = fragment.getMeasurements().get(i);
			Measurement prevMeasurement = new Measurement();
			Measurement nextMeasurement = new Measurement();

			// previous and next measurement exist in fragment
			if (i > 0 && i + 1 < fragment.getMeasurements().size()) {
				prevMeasurement = fragment.getMeasurements().get(i - 1);
				nextMeasurement = fragment.getMeasurements().get(i + 1);
			}

			// previous measurement is missing
			else if (i == 0 && i + 1 < fragment.getMeasurements().size()) {
				if (prevFragment == null) {
					prevMeasurement.setValue(measurement.getValue());
				} else {
					prevMeasurement = prevFragment.getMeasurements().get(prevFragment.getMeasurements().size() - 1);
				}
				nextMeasurement = fragment.getMeasurements().get(i + 1);
			}

			// next measurement is missing
			else if (i > 0 && i + 1 == fragment.getMeasurements().size()) {
				if (nextFragment == null) {
					nextMeasurement.setValue(measurement.getValue());
				} else {
					nextMeasurement = nextFragment.getMeasurements().get(0);
				}
				prevMeasurement = fragment.getMeasurements().get(i - 1);
			}

			// both measurements are missing
			else {
				if (prevFragment == null) {
					prevMeasurement.setValue(measurement.getValue());
				} else {
					prevMeasurement = prevFragment.getMeasurements().get(prevFragment.getMeasurements().size() - 1);
				}
				if (nextFragment == null) {
					nextMeasurement.setValue(measurement.getValue());
				} else {
					nextMeasurement = nextFragment.getMeasurements().get(0);
				}
			}

			BigDecimal lowerBound = measurement.getValue().subtract(usabilityConstraint.getMaximumError());
			BigDecimal upperBound = measurement.getValue().add(usabilityConstraint.getMaximumError());

			// check valid bounds
			if (lowerBound.compareTo(usabilityConstraint.getMinimumValue()) < 0) {
				lowerBound = usabilityConstraint.getMinimumValue();
			}
			if (upperBound.compareTo(usabilityConstraint.getMaximumValue()) > 0) {
				upperBound = usabilityConstraint.getMaximumValue();
			}

			// compute boundaries for each case
			// v(t-1) < v(t) < v(t+1)
			if (measurement.getValue().compareTo(prevMeasurement.getValue()) > 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) < 0) {

				if (lowerBound.compareTo(prevMeasurement.getValue()) < 0) {
					lowerBound = prevMeasurement.getValue();
				}
				if (upperBound.compareTo(nextMeasurement.getValue()) > 0) {
					upperBound = nextMeasurement.getValue();
				}
			}
			// v(t-1) > v(t) > v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) < 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) > 0) {

				if (lowerBound.compareTo(nextMeasurement.getValue()) < 0) {
					lowerBound = nextMeasurement.getValue();
				}
				if (upperBound.compareTo(prevMeasurement.getValue()) > 0) {
					upperBound = prevMeasurement.getValue();
				}
			}
			// v(t-1) < v(t) > v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) > 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) > 0) {

				if (lowerBound.compareTo(prevMeasurement.getValue()) < 0) {
					lowerBound = prevMeasurement.getValue();
				}
				if (lowerBound.compareTo(nextMeasurement.getValue()) < 0) {
					lowerBound = nextMeasurement.getValue();
				}
			}
			// v(t-1) > v(t) < v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) < 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) < 0) {

				if (upperBound.compareTo(prevMeasurement.getValue()) > 0) {
					upperBound = prevMeasurement.getValue();
				}
				if (upperBound.compareTo(nextMeasurement.getValue()) > 0) {
					upperBound = nextMeasurement.getValue();
				}
			}
			// v(t-1) = v(t) < v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) == 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) < 0) {

				if (upperBound.compareTo(nextMeasurement.getValue()) > 0) {
					upperBound = nextMeasurement.getValue();
				}
			}
			// v(t-1) = v(t) > v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) == 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) > 0) {

				if (lowerBound.compareTo(nextMeasurement.getValue()) < 0) {
					lowerBound = nextMeasurement.getValue();
				}
			}
			// v(t-1) < v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) > 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				if (lowerBound.compareTo(prevMeasurement.getValue()) < 0) {
					lowerBound = prevMeasurement.getValue();
				}
			}
			// v(t-1) > v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) < 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				if (upperBound.compareTo(prevMeasurement.getValue()) < 0) {
					upperBound = prevMeasurement.getValue();
				}
			}
			// v(t-1) = v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) == 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				// keep lower and upper bound
			}

			// total probabilities
			List<Range<BigDecimal>> rangeProbabilities = new LinkedList<Range<BigDecimal>>();

			// compute range sizes
			BigDecimal totalLowerValueRangeSize = measurement.getValue().subtract(lowerBound);
			BigDecimal totalUpperValueRangeSize = upperBound.subtract(measurement.getValue());

			// compute range sizes
			BigDecimal lowerRangeSize = totalLowerValueRangeSize
					.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges() / 2), 15, RoundingMode.DOWN);
			BigDecimal upperRangeSize = totalUpperValueRangeSize
					.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges() / 2), 15, RoundingMode.DOWN);

			// create ranges of upper and lower together
			// probability of 5 ranges per direction: 25%, 12.5%, 6.25%, 3.125%, 3.125%
			BigDecimal currentProbability = BigDecimal.valueOf(0.5);
			for (int k = 0; k < (usabilityConstraint.getNumberOfRanges() / 2); k++) {

				if (k + 1 < (usabilityConstraint.getNumberOfRanges() / 2)) {
					currentProbability = currentProbability.divide(BigDecimal.valueOf(2.0));
				}

				BigDecimal lowerRangeMinimum = measurement.getValue()
						.subtract(lowerRangeSize.multiply(BigDecimal.valueOf(k + 1)));
				BigDecimal lowerRangeMaximum = measurement.getValue()
						.subtract(lowerRangeSize.multiply(BigDecimal.valueOf(k)));
				BigDecimal lowerRangeProbability = currentProbability;
				rangeProbabilities.add(new Range<BigDecimal>(lowerRangeMinimum, lowerRangeMaximum, lowerRangeProbability));

				BigDecimal upperRangeMinimum = measurement.getValue().add(upperRangeSize.multiply(BigDecimal.valueOf(k)));
				BigDecimal upperRangeMaximum = measurement.getValue()
						.add(upperRangeSize.multiply(BigDecimal.valueOf(k + 1)));
				BigDecimal upperRangeProbability = currentProbability;
				rangeProbabilities.add(new Range<BigDecimal>(upperRangeMinimum, upperRangeMaximum, upperRangeProbability));
			}
			Collections.sort(rangeProbabilities);

			// select range
			Range<BigDecimal> selectedValueRange = new Range<BigDecimal>(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
			BigDecimal randomNumber4RangeSelection = BigDecimal.valueOf(prng.nextDouble());
			BigDecimal currentValue = BigDecimal.valueOf(0.0);
			for (int k = 0; k < rangeProbabilities.size(); k++) {
				Range<BigDecimal> range = rangeProbabilities.get(k);
				currentValue = currentValue.add(range.getValue());
				if (randomNumber4RangeSelection.compareTo(currentValue) <= 0) {
					selectedValueRange = range;
					break;
				}
			}

			// compute mark i of watermark
			// (min + (max - min) * random) - value
			BigDecimal randomNumber4ValueSelection = BigDecimal.valueOf(prng.nextDouble()).setScale(2, RoundingMode.DOWN);
			watermark[i] = (selectedValueRange.getMinimum()
					.add((selectedValueRange.getMaximum().subtract(selectedValueRange.getMinimum()))
							.multiply(randomNumber4ValueSelection))).subtract(measurement.getValue());
			watermark[i] = watermark[i].setScale(15, RoundingMode.HALF_UP);
		}
		return watermark;
	}
}
