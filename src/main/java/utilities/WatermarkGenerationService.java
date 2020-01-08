package utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import entities.Range;
import entities.Request;
import entities.UsabilityConstraint;

public class WatermarkGenerationService {

	public static BigDecimal[] generateWatermark(Request request, UsabilityConstraint usabilityConstraint,
			Fragment fragment, Fragment prevFragment, Fragment nextFragment) {

		BigDecimal[] watermark = new BigDecimal[fragment.getMeasurements().size()];
		Random random = new Random(fragment.getSecretKey() + Long.valueOf(request.getNumberOfWatermark()));

		// compute range probabilities: 50% / (2^((noOfRanges/2) - i))
		// exception when i = 0 to complete 100%
		List<BigDecimal> probabilities = new LinkedList<>();
		for (int i = 0; i < usabilityConstraint.getNumberOfRanges() / 2; i++) {

			BigDecimal probability = BigDecimal.valueOf(0.5)
					.divide(BigDecimal.valueOf(2).pow((usabilityConstraint.getNumberOfRanges() / 2) - i));
			if (i == 0) {
				probability = BigDecimal.valueOf(0.5)
						.divide(BigDecimal.valueOf(2).pow((usabilityConstraint.getNumberOfRanges() / 2) - (i + 1)));
			}

			probabilities.add(probability);
		}

		Collections.sort(fragment.getMeasurements());
		for (int i = 0; i < fragment.getMeasurements().size(); i++) {

			Measurement measurement = fragment.getMeasurements().get(i);
			Measurement prevMeasurement = new Measurement();
			Measurement nextMeasurement = new Measurement();

			// set previous measurement
			if (i > 0) {
				prevMeasurement = fragment.getMeasurements().get(i - 1);
			} else {
				if (prevFragment == null) {
					prevMeasurement.setValue(measurement.getValue());
				} else {
					prevMeasurement = prevFragment.getMeasurements().get(prevFragment.getMeasurements().size() - 1);
				}
			}

			// set next measurement
			if (i + 1 < fragment.getMeasurements().size()) {
				nextMeasurement = fragment.getMeasurements().get(i + 1);
			} else {
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
				lowerBound = prevMeasurement.getValue();
			}
			// v(t-1) = v(t) > v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) == 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) > 0) {

				if (lowerBound.compareTo(nextMeasurement.getValue()) < 0) {
					lowerBound = nextMeasurement.getValue();
				}
				upperBound = prevMeasurement.getValue();
			}
			// v(t-1) < v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) > 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				if (lowerBound.compareTo(prevMeasurement.getValue()) < 0) {
					lowerBound = prevMeasurement.getValue();
				}
				upperBound = nextMeasurement.getValue();
			}
			// v(t-1) > v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) < 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				if (upperBound.compareTo(prevMeasurement.getValue()) < 0) {
					upperBound = prevMeasurement.getValue();
				}
				lowerBound = nextMeasurement.getValue();
			}
			// v(t-1) = v(t) = v(t+1)
			else if (measurement.getValue().compareTo(prevMeasurement.getValue()) == 0
					&& measurement.getValue().compareTo(nextMeasurement.getValue()) == 0) {

				// keep lower and upper bound
			}

			// compute ranges and range sizes
			List<Range<BigDecimal>> ranges = new LinkedList<Range<BigDecimal>>();
			
			BigDecimal lowerRange = measurement.getValue().subtract(lowerBound);
			BigDecimal upperRange = upperBound.subtract(measurement.getValue());
			
			BigDecimal lowerRangeSize = lowerRange
					.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges() / 2), 15, RoundingMode.DOWN);
			BigDecimal upperRangeSize = upperRange
					.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges() / 2), 15, RoundingMode.DOWN);
			
			// compute final ranges and assign probabilities
			for (int j = 0; j < (usabilityConstraint.getNumberOfRanges() / 2); j++) {

				BigDecimal lowerRangeMinimum = lowerRange.negate().add(lowerRangeSize.multiply(BigDecimal.valueOf(j)));
				BigDecimal lowerRangeMaximum = lowerRange.negate()
						.add(lowerRangeSize.multiply(BigDecimal.valueOf(j + 1)));

				BigDecimal upperRangeMinimum = upperRange.subtract(upperRangeSize.multiply(BigDecimal.valueOf(j + 1)));
				BigDecimal upperRangeMaximum = upperRange.subtract(upperRangeSize.multiply(BigDecimal.valueOf(j)));
				
				if(lowerRange.compareTo(BigDecimal.valueOf(0.0)) == 0) {
					ranges.add(new Range<BigDecimal>(upperRangeMinimum, upperRangeMaximum, probabilities.get(j).add(probabilities.get(j))));
				}
				else if(upperRange.compareTo(BigDecimal.valueOf(0.0)) == 0) {
					ranges.add(new Range<BigDecimal>(lowerRangeMinimum, lowerRangeMaximum, probabilities.get(j).add(probabilities.get(j))));
				}
				else {
					ranges.add(new Range<BigDecimal>(lowerRangeMinimum, lowerRangeMaximum, probabilities.get(j)));
					ranges.add(new Range<BigDecimal>(upperRangeMinimum, upperRangeMaximum, probabilities.get(j)));
				}
				
				
			}
			Collections.sort(ranges);

			// select range
			Range<BigDecimal> selectedRange = new Range<BigDecimal>(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
			BigDecimal randomNumber4RangeSelection = BigDecimal.valueOf(random.nextDouble());
			BigDecimal currentValue = BigDecimal.valueOf(0.0);
			for (int j = 0; j < ranges.size(); j++) {
				Range<BigDecimal> range = ranges.get(j);
				currentValue = currentValue.add(range.getValue());
				if (randomNumber4RangeSelection.compareTo(currentValue) <= 0) {
					selectedRange = range;
					break;
				}
			}

			// compute mark: (min + (max - min) * random)
			BigDecimal randomNumber4ValueSelection = BigDecimal.valueOf(random.nextDouble());
			watermark[i] = selectedRange.getMinimum().add((selectedRange.getMaximum().subtract(selectedRange.getMinimum()))
							.multiply(randomNumber4ValueSelection));
			watermark[i] = watermark[i].setScale(15, RoundingMode.HALF_UP);
			//System.out.println(watermark[i].toString().replace(".", ","));
		}
		
		return watermark;
	}
}
