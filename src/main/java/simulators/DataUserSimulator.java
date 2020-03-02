package simulators;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import services.DataService;
import utilities.FileService;
import utilities.FragmentationService;
import utilities.LogService;
import utilities.TimeService;

public class DataUserSimulator {

	public static void requestDataset(int dataUserId, String deviceId, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset(dataUserId=" + dataUserId
				+ ", deviceId=" + deviceId + ", from=" + from + ", to=" + to + ")");

		TimeService timeService = new TimeService();
		String datasetName = "requestedDataset_by" + dataUserId + "_" + deviceId + "_" + from.toString() + "_"
				+ to.toString() + ".json";
		DataService.requestDataset(datasetName, dataUserId, deviceId, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		timeService.stop();

		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset", timeService.getTime());
	}

	public static void requestDataset(int dataUserId, int numberOfDevices, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset(dataUserId=" + dataUserId
				+ ", noOfDevices=" + numberOfDevices + ", from=" + from + ", to=" + to + ")");

		TimeService timeService = new TimeService();
		String datasetName = "requestedDataset_by" + dataUserId + "_" + numberOfDevices + "_" + from.toString() + "_"
				+ to.toString() + ".json";
		DataService.requestDataset(datasetName, dataUserId, numberOfDevices, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		timeService.stop();

		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset", timeService.getTime());
	}

	public static void attackDatasetByDeletion(String datasetName, int n) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		List<Fragment> newDataset = new LinkedList<>();
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Fragment newFragment = new Fragment(fragment.getDeviceId(), fragment.getType(), fragment.getUnit(),
					fragment.getDate());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				if (j % n != 0) {
					newFragment.getMeasurements().add(fragment.getMeasurements().get(j));
				}
			}
			newDataset.add(newFragment);
		}
		String attackedDatasetName = datasetName.substring(0, datasetName.indexOf(".json")) + "_deletion_" + n
				+ ".json";
		FileService.writeDataset(attackedDatasetName, newDataset);
	}

	public static void attackDatasetByRounding(String datasetName, int decimalDigit) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal roundedValue = fragment.getMeasurements().get(j).getValue().setScale(decimalDigit,
						RoundingMode.HALF_UP);
				fragment.getMeasurements().get(j).setValue(roundedValue);
			}
			dataset.set(i, fragment);
		}
		String attackedDatasetName = datasetName.substring(0, datasetName.indexOf(".json")) + "_rounding_"
				+ decimalDigit + ".json";
		FileService.writeDataset(attackedDatasetName, dataset);
	}

	public static void attackDatasetbyRandom(String datasetName, Double maxError, Long seed) {
		Random random = new Random(seed);
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal randomValue = BigDecimal.valueOf(random.nextDouble() * maxError);
				BigDecimal newValue = fragment.getMeasurements().get(j).getValue();
				if (random.nextBoolean() == true) {
					newValue = newValue.add(randomValue);
				} else {
					newValue = newValue.subtract(randomValue);
				}
				fragment.getMeasurements().get(j).setValue(newValue);
			}
			dataset.set(i, fragment);
		}
		String attackedDatasetName = datasetName.substring(0, datasetName.indexOf(".json")) + "_random_"
				+ maxError.toString().replace(".", "-") + ".json";
		FileService.writeDataset(attackedDatasetName, dataset);
	}

	public static void attackDatasetbySubset(String datasetName, int startIndex, int endIndex) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		List<Fragment> newDataset = new LinkedList<>();
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Fragment newFragment = new Fragment(fragment.getDeviceId(), fragment.getType(), fragment.getUnit(),
					fragment.getDate());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				if (j >= startIndex && j <= endIndex) {
					newFragment.getMeasurements().add(fragment.getMeasurements().get(j));
				}
			}
			newDataset.add(newFragment);
		}
		String attackedDatasetName = datasetName.substring(0, datasetName.indexOf(".json")) + "_subset_" + startIndex
				+ "-" + endIndex + ".json";
		FileService.writeDataset(attackedDatasetName, newDataset);
	}

	public static void attackDatasetByCollusion(List<String> datasetNames) {
		List<List<Fragment>> datasets = new LinkedList<>();
		String attackedDatasetName = "colludedDataset_by";
		for (String datasetName : datasetNames) {
			datasets.add(FragmentationService.getFragments(datasetName));
			attackedDatasetName = attackedDatasetName + "-" + datasetName.split("_")[1].substring(2);
		}

		List<Fragment> dataset = new LinkedList<>();
		for (int i = 0; i < datasets.get(0).size(); i++) {
			Fragment newFragment = new Fragment(datasets.get(0).get(i).getDeviceId(), datasets.get(0).get(i).getType(),
					datasets.get(0).get(i).getUnit(), datasets.get(0).get(i).getDate());
			for (int j = 0; j < datasets.get(0).get(i).getMeasurements().size(); j++) {
				BigDecimal newValue = new BigDecimal("0.0");
				for (List<Fragment> colludingDataset : datasets) {
					newValue = newValue.add(colludingDataset.get(i).getMeasurements().get(j).getValue());
				}
				newValue = newValue.divide(BigDecimal.valueOf(datasets.size()), 15, RoundingMode.HALF_UP);
				Measurement newMeasurement = new Measurement(newFragment.getDeviceId(), newFragment.getType(),
						newFragment.getUnit(), datasets.get(0).get(i).getMeasurements().get(j).getTime(), newValue);
				newFragment.getMeasurements().add(newMeasurement);
			}
			dataset.add(newFragment);
		}
		attackedDatasetName = attackedDatasetName + ".json";
		FileService.writeDataset(attackedDatasetName, dataset);
	}
}
