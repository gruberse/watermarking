package generators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import utilities.FileService;
import utilities.FragmentationService;

public class AttackGenerator {

	public static void performDeletionAttack(String datasetName, String newDatasetName, int n) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				if (i % n == 0) {
					fragment.getMeasurements().remove(j);
				}
			}
			dataset.set(i, fragment);
		}
		FileService.writeDataset(newDatasetName, dataset);
	}

	public static void performRoundingDistortionAttack(String datasetName, String newDatasetName, int decimalDigits) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal roundedValue = fragment.getMeasurements().get(j).getValue().setScale(decimalDigits,
						RoundingMode.HALF_UP);
				fragment.getMeasurements().get(j).setValue(roundedValue);
			}
			dataset.set(i, fragment);
		}
		FileService.writeDataset(newDatasetName, dataset);
	}

	public static void performRandomDistortionAttack(String datasetName, String newDatasetName, Double maxError) {
		Random random = new Random();
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal randomValue = BigDecimal.valueOf(random.nextDouble()).multiply(BigDecimal.valueOf(0.5));
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
		FileService.writeDataset(newDatasetName, dataset);
	}

	public static void performSubsetAttack(String datasetName, String newDatasetName, int startIndex, int endIndex) {
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				if (j < startIndex || j > endIndex) {
					fragment.getMeasurements().remove(j);
				}
			}
			dataset.set(i, fragment);
		}
		FileService.writeDataset(newDatasetName, dataset);
	}

	public static void performMeanCollusionAttack(List<String> datasetNames, String newDatasetName) {
		List<List<Fragment>> datasets = new LinkedList<>();
		for (String datasetName : datasetNames) {
			datasets.add(FragmentationService.getFragments(datasetName));
		}

		List<Fragment> dataset = new LinkedList<>();
		for (int i = 0; i < datasets.get(0).size(); i++) {
			Fragment newFragment = new Fragment(datasets.get(0).get(i).getDeviceId(), datasets.get(0).get(i).getType(),
					datasets.get(0).get(i).getUnit(), datasets.get(0).get(i).getDate());
			for (int j = 1; j < datasets.get(0).get(i).getMeasurements().size(); j++) {
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
		FileService.writeDataset(newDatasetName, dataset);
	}
}
