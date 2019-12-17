package testdrivers;

import java.io.ObjectInputStream.GetField;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import services.DetectionService;
import utils.FileService;
import utils.FragmentationService;

public class AttackSimulator {

	private static final BigDecimal fragmentSimilarityThreshold = new BigDecimal("0.9");
	private static final BigDecimal watermarkSimilarityThreshold = new BigDecimal("0.9");

	public static void performNoAttack(String datasetName) {
		String reportName = FileService.getFileName(datasetName, "report", "txt");
		DetectionService.getLeakageReport(datasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}

	public static void performDeletionAttack(String datasetName, int n) {
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
		String attackedDatasetName = FileService.getFileName(datasetName, "deletion", "json");
		FileService.writeDataset(attackedDatasetName, dataset);
		String reportName = FileService.getFileName(attackedDatasetName, "report", "txt");
		DetectionService.getLeakageReport(attackedDatasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}

	public static void performDistortionAttack_rounding(String datasetName, int decimalDigits) {
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
		String attackedDatasetName = FileService.getFileName(datasetName, "rounding", "json");
		FileService.writeDataset(attackedDatasetName, dataset);
		String reportName = FileService.getFileName(attackedDatasetName, "report", "txt");
		DetectionService.getLeakageReport(attackedDatasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}

	public static void performDistortionAttack_random(String datasetName, BigDecimal maxError) {
		Random random = new Random();
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);
			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal randomValue = BigDecimal.valueOf(random.nextDouble()).multiply(maxError);
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
		String attackedDatasetName = FileService.getFileName(datasetName, "random", "json");
		FileService.writeDataset(attackedDatasetName, dataset);
		String reportName = FileService.getFileName(attackedDatasetName, "report", "txt");
		DetectionService.getLeakageReport(attackedDatasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}

	public static void performSubsetAttack(String datasetName, BigDecimal percentage) {
		Random random = new Random();
		List<Fragment> dataset = FragmentationService.getFragments(datasetName);
		Collections.sort(dataset);
		for (int i = 0; i < dataset.size(); i++) {
			Fragment fragment = dataset.get(i);

			BigDecimal percentagePerMeasurement = BigDecimal.valueOf(100)
					.divide(BigDecimal.valueOf(fragment.getMeasurements().size()), 2, RoundingMode.HALF_UP)
					.divide(BigDecimal.valueOf(100));
			BigDecimal totalPercentage = new BigDecimal("0.0");
			BigDecimal subsetPercentage = new BigDecimal("0.0");
			boolean subset = false;
			List<Measurement> subsetList = new LinkedList<>();

			Collections.sort(fragment.getMeasurements());
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				totalPercentage = totalPercentage.add(percentagePerMeasurement);
				if (subsetPercentage.compareTo(BigDecimal.valueOf(0.0)) == 0
						&& random.nextInt((100 - percentage.intValue()) - totalPercentage.intValue()) == 1) {
					subset = true;
				}
				if (subset == true && percentage.compareTo(subsetPercentage) > 0) {
					subsetPercentage = subsetPercentage.add(percentagePerMeasurement);
					subsetList.add(fragment.getMeasurements().get(j));
				}
				if (subset == true && percentage.compareTo(subsetPercentage) <= 0) {
					break;
				}
			}
			fragment.setMeasurements(subsetList);
			dataset.set(i, fragment);
		}
		String attackedDatasetName = FileService.getFileName(datasetName, "subset", "json");
		FileService.writeDataset(attackedDatasetName, dataset);
		String reportName = FileService.getFileName(attackedDatasetName, "report", "txt");
		DetectionService.getLeakageReport(attackedDatasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}

	public static void performMeanCollusionAttack(List<String> datasetNames) {
		List<List<Fragment>> datasets = new LinkedList<>();
		String numbers = "";
		for (String datasetName : datasetNames) {
			datasets.add(FragmentationService.getFragments(datasetName));
			numbers = numbers + datasetName.split("dataset")[1].split(".json")[0] + "_";
		}
		numbers = numbers.substring(0, numbers.length() - 1);

		List<Fragment> dataset = new LinkedList<>();
		for (int i = 0; i < datasets.get(0).size(); i++) {
			Fragment newFragment = new Fragment(datasets.get(0).get(i).getDeviceId(), datasets.get(0).get(i).getType(),
					datasets.get(0).get(i).getUnit(), datasets.get(0).get(i).getDate());
			for (int j = 1; j < datasets.get(0).get(i).getMeasurements().size(); j++) {
				BigDecimal newValue = new BigDecimal("0.0");
				for (List<Fragment> colludingDataset : datasets) {
					newValue = newValue.add(colludingDataset.get(i).getMeasurements().get(j).getValue());
				}
				newValue = newValue.divide(BigDecimal.valueOf(datasets.size()), RoundingMode.HALF_UP);
				Measurement newMeasurement = new Measurement(newFragment.getDeviceId(), newFragment.getType(),
						newFragment.getUnit(), datasets.get(0).get(i).getMeasurements().get(j).getTime(), newValue);
				newFragment.getMeasurements().add(newMeasurement);
			}
			dataset.add(newFragment);
		}

		String attackedDatasetName = "files/collusion_dataset_" + numbers + ".json";
		FileService.writeDataset(attackedDatasetName, dataset);
		String reportName = FileService.getFileName(attackedDatasetName, "report", "txt");
		DetectionService.getLeakageReport(attackedDatasetName, reportName, fragmentSimilarityThreshold,
				watermarkSimilarityThreshold);
	}
}
