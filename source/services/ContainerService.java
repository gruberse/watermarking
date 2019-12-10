package services;

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
import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FragmentationService;

public class ContainerService {

	public static void uploadDataset(String fileName) {
		List<DataProfile> dataProfiles = new LinkedList<>();

		// retrieve fragments
		List<Fragment> fragments = FragmentationService.getFragments(fileName);

		// complete fragments and collect data profiles
		Random secretKeyGenerator = new Random();
		for (Fragment fragment : fragments) {
			fragment.setDatasetId(fileName);
			fragment.setSecretKey(secretKeyGenerator.nextLong());

			DataProfile dataProfile = new DataProfile(fileName, fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit());
			if (dataProfiles.contains(dataProfile)) {
				dataProfiles.get(dataProfiles.indexOf(dataProfile)).getMeasurements()
						.addAll(fragment.getMeasurements());
			} else {
				dataProfile.getMeasurements().addAll(fragment.getMeasurements());
				dataProfiles.add(dataProfile);
			}
		}

		// complete data profiles
		for (DataProfile dataProfile : dataProfiles) {
			Collections.sort(dataProfile.getMeasurements());

			// retrieve usability constraint
			UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(dataProfile.getType(),
					dataProfile.getUnit());

			// initialize absolute distributions
			BigDecimal valueRangeSize = BigDecimal.valueOf(0.1);
					//usabilityConstraint.getMaximumError().multiply(new BigDecimal(2))
					//.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges()));
			List<Range<Integer>> absoluteValueDistribution = new LinkedList<>();
			
			BigDecimal slopeRangeSize = valueRangeSize.divide(BigDecimal.valueOf(10));
			List<Range<Integer>> absoluteSlopeDistribution = new LinkedList<>();
			
			BigDecimal curvatureRangeSize = slopeRangeSize.divide(BigDecimal.valueOf(10));
			List<Range<Integer>> absoluteCurvatureDistribution = new LinkedList<>();

			// compute absolute distributions
			for (int i = 0; i < dataProfile.getMeasurements().size(); i++) {
				Measurement measurement = dataProfile.getMeasurements().get(i);

				// compute absolute value distribution
				BigDecimal value = measurement.getValue();
				Range<Integer> valueRange = new Range<Integer>(
						value.setScale(valueRangeSize.scale(), RoundingMode.DOWN),
						value.setScale(valueRangeSize.scale(), RoundingMode.UP));
				if (absoluteValueDistribution.contains(valueRange)) {
					valueRange = absoluteValueDistribution.get(absoluteValueDistribution.indexOf(valueRange));
					valueRange.setValue(valueRange.getValue() + 1);
				} else {
					valueRange.setValue(1);
					absoluteValueDistribution.add(valueRange);
				}

				// compute absolute slope distribution
				// slope(t,t+1) = value(t+1) - value(t)
				if (i + 1 < dataProfile.getMeasurements().size()) {
					BigDecimal slope = dataProfile.getMeasurements().get(i + 1).getValue()
							.subtract(measurement.getValue());
					Range<Integer> slopeRange = new Range<Integer>(
							slope.setScale(slopeRangeSize.scale(), RoundingMode.DOWN),
							slope.setScale(slopeRangeSize.scale(), RoundingMode.UP));
					if (absoluteSlopeDistribution.contains(slopeRange)) {
						slopeRange = absoluteSlopeDistribution.get(absoluteSlopeDistribution.indexOf(slopeRange));
						slopeRange.setValue(slopeRange.getValue() + 1);
					} else {
						slopeRange.setValue(1);
						absoluteSlopeDistribution.add(slopeRange);
					}
				}

				// compute absolute curvature distribution
				// curvature(t) = slope(t,t+1) - slope(t-1,t)
				if (i > 0 && i + 1 < dataProfile.getMeasurements().size()) {
					BigDecimal curvature = (dataProfile.getMeasurements().get(i + 1).getValue()
							.subtract(measurement.getValue()))
									.subtract(measurement.getValue()
											.subtract(dataProfile.getMeasurements().get(i - 1).getValue()));
					Range<Integer> curvatureRange = new Range<Integer>(
							curvature.setScale(curvatureRangeSize.scale(), RoundingMode.DOWN),
							curvature.setScale(curvatureRangeSize.scale(), RoundingMode.UP));
					if (absoluteCurvatureDistribution.contains(curvatureRange)) {
						curvatureRange = absoluteCurvatureDistribution
								.get(absoluteCurvatureDistribution.indexOf(curvatureRange));
						curvatureRange.setValue(curvatureRange.getValue() + 1);
					} else {
						curvatureRange.setValue(1);
						absoluteCurvatureDistribution.add(curvatureRange);
					}
				}
			}

			// compute relative value distribution
			for (Range<Integer> valueRange : absoluteValueDistribution) {
				dataProfile.getRelativeValueDistribution()
						.add(new Range<BigDecimal>(valueRange.getMinimum(), valueRange.getMaximum(),
								new BigDecimal(valueRange.getValue()).divide(
										new BigDecimal(dataProfile.getMeasurements().size()), 4,
										RoundingMode.HALF_UP)));
			}

			// compute relative slope distribution
			for (Range<Integer> slopeRange : absoluteSlopeDistribution) {
				dataProfile.getRelativeSlopeDistribution()
						.add(new Range<BigDecimal>(slopeRange.getMinimum(), slopeRange.getMaximum(),
								new BigDecimal(slopeRange.getValue()).divide(
										new BigDecimal(dataProfile.getMeasurements().size()), 4,
										RoundingMode.HALF_UP)));
			}

			// compute relative curvature distribution
			for (Range<Integer> curvatureRange : absoluteCurvatureDistribution) {
				dataProfile.getRelativeCurvatureDistribution()
						.add(new Range<BigDecimal>(curvatureRange.getMinimum(), curvatureRange.getMaximum(),
								new BigDecimal(curvatureRange.getValue()).divide(
										new BigDecimal(dataProfile.getMeasurements().size()), 4,
										RoundingMode.HALF_UP)));
			}

			Collections.sort(dataProfile.getRelativeValueDistribution());
			Collections.sort(dataProfile.getRelativeSlopeDistribution());
			Collections.sort(dataProfile.getRelativeCurvatureDistribution());
		}

		// insert fragments and profiles
		DatabaseService.insertFragments(fragments);
		DatabaseService.insertProfiles(dataProfiles);
	}
}
