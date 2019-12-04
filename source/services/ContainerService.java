package services;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Bin;
import entities.DataProfile;
import entities.Fragment;
import entities.Measurement;
import entities.UsabilityConstraint;
import utils.DatabaseService;

public class ContainerService {

	public static void uploadDataset(String fileName) {
		List<Fragment> fragments = new LinkedList<>();
		List<DataProfile> dataProfiles = new LinkedList<>();

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			JSONParser parser = new JSONParser();

			// retrieve measurements from json file
			JSONArray array = (JSONArray) parser.parse(new FileReader(fileName));
			for (Object measurementObject : array) {
				JSONObject jsonMeasurement = (JSONObject) measurementObject;

				// format fields
				String deviceId = (String) jsonMeasurement.get("deviceId");
				String type = (String) jsonMeasurement.get("type");
				String unit = (String) jsonMeasurement.get("unit");
				String timeString = (String) jsonMeasurement.get("time");
				if (timeString.contains("T")) {
					timeString = timeString.replace("T", " ");
				}
				if (timeString.contains(".")) {
					timeString = timeString.split("\\.")[0];
				}
				LocalDateTime time = LocalDateTime.parse(timeString, formatter);
				BigDecimal value = new BigDecimal(jsonMeasurement.get("value").toString());
				Measurement measurement = new Measurement(deviceId, type, unit, time, value);

				// collect fragments
				Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(),
						measurement.getUnit(), 5, measurement.getTime().toLocalDate());
				if (fragments.contains(fragment)) {
					fragments.get(fragments.indexOf(fragment)).getMeasurements().add(measurement);
				} else {
					fragment.getMeasurements().add(measurement);
					fragments.add(fragment);
				}

				// collect data profiles
				DataProfile dataProfile = new DataProfile(fileName, measurement.getDeviceId(), measurement.getType(),
						measurement.getUnit(), 5);
				if (dataProfiles.contains(dataProfile)) {
					dataProfiles.get(dataProfiles.indexOf(dataProfile)).getMeasurements().add(measurement);
				} else {
					dataProfile.getMeasurements().add(measurement);
					dataProfiles.add(dataProfile);
				}

			}

			// complete fragments
			Random secretKeyGenerator = new Random();
			for (Fragment fragment : fragments) {
				Collections.sort(fragment.getMeasurements());
				fragment.setDatasetId(fileName);
				fragment.setSecretKey(secretKeyGenerator.nextLong());
			}

			// complete data profiles
			for (DataProfile profile : dataProfiles) {
				Collections.sort(profile.getMeasurements());

				// retrieve usability constraint to choose useful bin sizes
				UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(profile.getType(),
						profile.getUnit(), profile.getFrequency());

				// initialize absolute distributions
				BigDecimal valueBinSize = usabilityConstraint.getMaximumError().multiply(new BigDecimal(2))
						.divide(new BigDecimal(usabilityConstraint.getNumberOfRanges()));
				List<Bin<Integer>> absoluteValueDistribution = new LinkedList<>();

				BigDecimal slopeBinSize = usabilityConstraint.getMaximumError().multiply(new BigDecimal(20))
						.divide(new BigDecimal(usabilityConstraint.getNumberOfRanges()));
				List<Bin<Integer>> absoluteSlopeDistribution = new LinkedList<>();

				BigDecimal curvatureBinSize = usabilityConstraint.getMaximumError().multiply(new BigDecimal(200))
						.divide(new BigDecimal(usabilityConstraint.getNumberOfRanges()));
				List<Bin<Integer>> absoluteCurvatureDistribution = new LinkedList<>();

				// compute absolute distributions
				for (int i = 0; i < profile.getMeasurements().size(); i++) {
					Measurement measurement = profile.getMeasurements().get(i);

					// compute absolute value distribution
					BigDecimal value = measurement.getValue();
					Bin<Integer> valueBin = new Bin<Integer>(
							value.setScale(valueBinSize.precision(), RoundingMode.DOWN),
							value.setScale(valueBinSize.precision(), RoundingMode.UP));
					if (absoluteValueDistribution.contains(valueBin)) {
						valueBin = absoluteValueDistribution.get(absoluteValueDistribution.indexOf(valueBin));
						valueBin.setValue(valueBin.getValue() + 1);
					} else {
						valueBin.setValue(1);
						absoluteValueDistribution.add(valueBin);
					}

					// compute absolute slope distribution, slope(t,t+1) = value(t+1) - value(t)
					if (i + 1 < profile.getMeasurements().size()) {
						BigDecimal slope = profile.getMeasurements().get(i + 1).getValue()
								.subtract(measurement.getValue());
						Bin<Integer> slopeBin = new Bin<Integer>(
								slope.setScale(slopeBinSize.precision(), RoundingMode.DOWN),
								slope.setScale(slopeBinSize.precision(), RoundingMode.UP));
						if (absoluteSlopeDistribution.contains(slopeBin)) {
							slopeBin = absoluteSlopeDistribution.get(absoluteSlopeDistribution.indexOf(slopeBin));
							slopeBin.setValue(slopeBin.getValue() + 1);
						} else {
							slopeBin.setValue(1);
							absoluteSlopeDistribution.add(slopeBin);
						}
					}

					// compute absolute curvature distribution, curvature(t) = slope(t,t+1) -
					// slope(t-1,t)
					if (i > 0 && i + 1 < profile.getMeasurements().size()) {
						BigDecimal curvature = (profile.getMeasurements().get(i + 1).getValue()
								.subtract(measurement.getValue()))
										.subtract(measurement.getValue()
												.subtract(profile.getMeasurements().get(i - 1).getValue()));
						Bin<Integer> curvatureBin = new Bin<Integer>(
								curvature.setScale(curvatureBinSize.precision(), RoundingMode.DOWN),
								curvature.setScale(curvatureBinSize.precision(), RoundingMode.UP));
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
					profile.getRelativeValueDistribution()
							.add(new Bin<BigDecimal>(valueBin.getMinimum(), valueBin.getMaximum(),
									new BigDecimal(valueBin.getValue()).divide(
											new BigDecimal(profile.getMeasurements().size()), 4,
											RoundingMode.HALF_UP)));
				}

				// compute relative slope distribution
				for (Bin<Integer> slopeBin : absoluteSlopeDistribution) {
					profile.getRelativeSlopeDistribution()
							.add(new Bin<BigDecimal>(slopeBin.getMinimum(), slopeBin.getMaximum(),
									new BigDecimal(slopeBin.getValue()).divide(
											new BigDecimal(profile.getMeasurements().size()), 4,
											RoundingMode.HALF_UP)));
				}

				// compute relative curvature distribution
				for (Bin<Integer> curvatureBin : absoluteCurvatureDistribution) {
					profile.getRelativeCurvatureDistribution()
							.add(new Bin<BigDecimal>(curvatureBin.getMinimum(), curvatureBin.getMaximum(),
									new BigDecimal(curvatureBin.getValue()).divide(
											new BigDecimal(profile.getMeasurements().size()), 4,
											RoundingMode.HALF_UP)));
				}

				Collections.sort(profile.getRelativeValueDistribution());
				Collections.sort(profile.getRelativeSlopeDistribution());
				Collections.sort(profile.getRelativeCurvatureDistribution());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}

		// insert results in database
		DatabaseService.insertFragments(fragments);
		DatabaseService.insertProfiles(dataProfiles);
	}
}
