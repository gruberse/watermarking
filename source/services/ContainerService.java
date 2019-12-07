package services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Bin;
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
			dataProfile.setValueBinSize(usabilityConstraint.getMaximumError().multiply(new BigDecimal(2))
					.divide(BigDecimal.valueOf(usabilityConstraint.getNumberOfRanges())));
			List<Bin<Integer>> absoluteValueDistribution = new LinkedList<>();
			dataProfile.setSlopeBinSize(dataProfile.getValueBinSize().divide(new BigDecimal(10)));
			List<Bin<Integer>> absoluteSlopeDistribution = new LinkedList<>();
			dataProfile.setCurvatureBinSize(dataProfile.getSlopeBinSize().divide(new BigDecimal(10)));
			List<Bin<Integer>> absoluteCurvatureDistribution = new LinkedList<>();

			// compute absolute distributions
			for (int i = 0; i < dataProfile.getMeasurements().size(); i++) {
				Measurement measurement = dataProfile.getMeasurements().get(i);

				// compute absolute value distribution
				BigDecimal value = measurement.getValue();
				Bin<Integer> valueBin = new Bin<Integer>(
						value.setScale(dataProfile.getValueBinSize().scale(), RoundingMode.DOWN),
						value.setScale(dataProfile.getValueBinSize().scale(), RoundingMode.UP));
				if (absoluteValueDistribution.contains(valueBin)) {
					valueBin = absoluteValueDistribution.get(absoluteValueDistribution.indexOf(valueBin));
					valueBin.setValue(valueBin.getValue() + 1);
				} else {
					valueBin.setValue(1);
					absoluteValueDistribution.add(valueBin);
				}

				// compute absolute slope distribution
				// slope(t,t+1) = value(t+1) - value(t)
				if (i + 1 < dataProfile.getMeasurements().size()) {
					BigDecimal slope = dataProfile.getMeasurements().get(i + 1).getValue()
							.subtract(measurement.getValue());
					Bin<Integer> slopeBin = new Bin<Integer>(
							slope.setScale(dataProfile.getSlopeBinSize().scale(), RoundingMode.DOWN),
							slope.setScale(dataProfile.getSlopeBinSize().scale(), RoundingMode.UP));
					if (absoluteSlopeDistribution.contains(slopeBin)) {
						slopeBin = absoluteSlopeDistribution.get(absoluteSlopeDistribution.indexOf(slopeBin));
						slopeBin.setValue(slopeBin.getValue() + 1);
					} else {
						slopeBin.setValue(1);
						absoluteSlopeDistribution.add(slopeBin);
					}
				}

				// compute absolute curvature distribution
				// curvature(t) = slope(t,t+1) - slope(t-1,t)
				if (i > 0 && i + 1 < dataProfile.getMeasurements().size()) {
					BigDecimal curvature = (dataProfile.getMeasurements().get(i + 1).getValue()
							.subtract(measurement.getValue()))
									.subtract(measurement.getValue()
											.subtract(dataProfile.getMeasurements().get(i - 1).getValue()));
					Bin<Integer> curvatureBin = new Bin<Integer>(
							curvature.setScale(dataProfile.getCurvatureBinSize().scale(), RoundingMode.DOWN),
							curvature.setScale(dataProfile.getCurvatureBinSize().scale(), RoundingMode.UP));
					if (absoluteCurvatureDistribution.contains(curvatureBin)) {
						curvatureBin = absoluteCurvatureDistribution
								.get(absoluteCurvatureDistribution.indexOf(curvatureBin));
						curvatureBin.setValue(curvatureBin.getValue() + 1);
					} else {
						curvatureBin.setValue(1);
						absoluteCurvatureDistribution.add(curvatureBin);
					}
				}
			}

			// compute relative value distribution
			for (Bin<Integer> valueBin : absoluteValueDistribution) {
				dataProfile.getRelativeValueDistribution()
						.add(new Bin<BigDecimal>(valueBin.getMinimum(), valueBin.getMaximum(),
								new BigDecimal(valueBin.getValue()).divide(
										new BigDecimal(dataProfile.getMeasurements().size()), 4,
										RoundingMode.HALF_UP)));
			}

			// compute relative slope distribution
			for (Bin<Integer> slopeBin : absoluteSlopeDistribution) {
				dataProfile.getRelativeSlopeDistribution()
						.add(new Bin<BigDecimal>(slopeBin.getMinimum(), slopeBin.getMaximum(),
								new BigDecimal(slopeBin.getValue()).divide(
										new BigDecimal(dataProfile.getMeasurements().size()), 4,
										RoundingMode.HALF_UP)));
			}

			// compute relative curvature distribution
			for (Bin<Integer> curvatureBin : absoluteCurvatureDistribution) {
				dataProfile.getRelativeCurvatureDistribution()
						.add(new Bin<BigDecimal>(curvatureBin.getMinimum(), curvatureBin.getMaximum(),
								new BigDecimal(curvatureBin.getValue()).divide(
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
