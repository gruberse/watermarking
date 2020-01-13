package simulators;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import services.ContainerService;
import utilities.FileService;
import utilities.LogService;
import utilities.StopwatchService;

public class PatientSimulator {

	public static void storeDataset() {
		storeDataset("testdata.json");
	}

	public static void storeDataset(String datasetName) {
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(datasetName=" + datasetName + ")");

		StopwatchService stopwatchService = new StopwatchService();
		ContainerService.uploadDataset(datasetName);
		stopwatchService.stop();

		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset", stopwatchService.getTime());
	}

	public static void generateDataset(String datasetName, String deviceId, String from, String to) {
		List<Fragment> dataset = new LinkedList<>();
		Random random = new Random();

		int minute = random.nextInt(5);
		int second = random.nextInt(60);
		BigDecimal value = BigDecimal.valueOf(random.nextDouble() * 22.5);

		int sequenceLength = 0;
		char sequenceSign = '+';
		if (random.nextBoolean() == false) {
			sequenceSign = '-';
		}

		LocalDate startDate = LocalDate.parse(from);
		LocalDate endDate = LocalDate.parse(to);

		while (!startDate.isAfter(endDate)) {
			Fragment fragment = new Fragment();
			fragment.setMeasurements(new LinkedList<>());

			int hour = 0;
			while (hour < 24) {
				LocalDateTime time = LocalDateTime.of(startDate.getYear(), startDate.getMonth(),
						startDate.getDayOfMonth(), hour, minute, second);

				if (sequenceLength == 0) {
					if (random.nextBoolean() == true) {
						sequenceLength = random.nextInt(20);
					} else {
						sequenceLength = random.nextInt(40);
					}
					if (sequenceSign == '+') {
						sequenceSign = '-';
					} else {
						sequenceSign = '+';
					}
				} else {
					sequenceLength = sequenceLength - 1;
				}

				BigDecimal valueChange = BigDecimal.valueOf(random.nextDouble());
				if (sequenceSign == '+') {
					value = value.add(valueChange);
					if (value.compareTo(BigDecimal.valueOf(55.0)) >= 0) {
						value = value.subtract(valueChange).subtract(valueChange);
						sequenceLength = 0;
					}
				} else {
					value = value.subtract(valueChange);
					if (value.compareTo(BigDecimal.valueOf(0.0)) <= 0) {
						value = value.add(valueChange).add(valueChange);
						sequenceLength = 0;
					}
				}

				Measurement measurement = new Measurement(deviceId, "cbg", "mmol/L", time, value);
				fragment.getMeasurements().add(measurement);

				minute = minute + 5;
				if (minute >= 60) {
					hour = hour + 1;
					minute = minute - 60;
				}
			}
			dataset.add(fragment);
			startDate = startDate.plusDays(1);
		}
		FileService.writeDataset(datasetName, dataset);
	}
}