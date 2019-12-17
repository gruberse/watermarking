package testdrivers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import entities.Fragment;
import entities.Measurement;
import services.ContainerService;
import utils.FileService;

public class DataGenerator {

	public static void generateDataset(String fileName, int noOfDevices, LocalDate from, LocalDate to) {
		List<Fragment> dataset = new LinkedList<>();
		Random randomPRNG = new Random();

		for (int i = 0; i < noOfDevices; i++) {
			String deviceId = "Dev" + i;

			int minute = randomPRNG.nextInt(5);
			int second = randomPRNG.nextInt(60);
			BigDecimal value = BigDecimal.valueOf(randomPRNG.nextDouble() * randomPRNG.nextInt(15));
			int noOfDeclining = 0;
			int noOfUpclining = 0;

			for (LocalDate date : from.datesUntil(to).collect(Collectors.toList())) {
				Fragment fragment = new Fragment();
				fragment.setMeasurements(new LinkedList<>());

				// generate Measurements
				int hour = 0;
				while (hour < 24) {
					
					LocalDateTime time = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour,
							minute, second);
					
					// generate new value and decide whether calculate + or -
					BigDecimal nextValue = BigDecimal.valueOf(randomPRNG.nextDouble());
					if(value.subtract(nextValue).compareTo(BigDecimal.valueOf(0)) <= 0) {
						value = value.add(nextValue);
						noOfDeclining = 0;
						noOfUpclining = -20;
					}
					else if(randomPRNG.nextInt(100) > ((50 + noOfUpclining) - noOfDeclining)) {
						noOfUpclining = noOfUpclining + 5;
						noOfDeclining = 0;
						value = value.add(nextValue);
					}
					else {
						noOfDeclining = noOfDeclining + 5;
						noOfUpclining = 0;
						value = value.subtract(nextValue);
					}
					
					Measurement measurement = new Measurement(deviceId, "cbg", "mmol/L", time, value);
					fragment.getMeasurements().add(measurement);
					
					minute = minute + 5;
					if(minute >= 60) {
						hour = hour + 1;
						minute = minute - 60;
					}
				}
				dataset.add(fragment);
			}
		}
		FileService.writeDataset(fileName, dataset);
		ContainerService.uploadDataset(fileName);
	}
}
