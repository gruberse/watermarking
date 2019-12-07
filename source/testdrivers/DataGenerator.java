package testdrivers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import entities.Fragment;
import services.ContainerService;

public class DataGenerator {

	/**
	 * generates specified dataset. provides dataset to file. uploads dataset to
	 * database.
	 * 
	 * @param noOfDevices number of devices
	 * @param type        type of measurements
	 * @param unit        unit of measurements
	 * @param from        the beginning of the time period
	 * @param to          the end of the time period
	 */
	public static void addDataset(int noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
		// uploads dataset to database
		ContainerService.uploadDataset(
				// provides dataset to file
				provideDataset("files/",
						// generate specified dataset
						generateDataset(noOfDevices, type, unit, from, to)));
	}

	/*
	 * creates json array as string. generates file name. writes json to file.
	 */
	private static String provideDataset(String requestLocation, List<Fragment> fragments) {
		String json = "[";
		for (int i = 0; i < fragments.size(); i++) {
			json = json + fragments.get(i).getMeasurementsAsJsonString();
			if (i + 1 < fragments.size())
				json = json + ",";
		}
		json = json + "\n]";

		//String fileName = FileService.getFileName(requestLocation, "generated", "Dataset", ".json");
		//FileService.writeFile(fileName, json);

		return "";
	}

	/*
	 * generates specified dataset.
	 */
	private static List<Fragment> generateDataset(int noOfDevices, String type, String unit, LocalDate from,
			LocalDate to) {
		List<Fragment> dataset = new LinkedList<>();
		Random randomPRNG = new Random();

		for (int i = 0; i < noOfDevices; i++) {
			String deviceId = "Dev" + System.currentTimeMillis();

			int minute = randomPRNG.nextInt(5);
			int second = randomPRNG.nextInt(60);
			double value = randomPRNG.nextDouble() * randomPRNG.nextInt(15);
			int noOfDeclining = 0;
			int noOfUpclining = 0;

			for (LocalDate date : from.datesUntil(to).collect(Collectors.toList())) {
				Fragment fragment = new Fragment(); // 

				// generate Measurements
				int hour = 0;
				while (hour < 24) {
					
					LocalDateTime time = LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour,
							minute, second);
					
					// generate new value and decide whether calculate + or -
					double nextValue = randomPRNG.nextDouble();
					if(value - nextValue <= 0) {
						value = value + nextValue;
						noOfDeclining = 0;
						noOfUpclining = -20;
					}
					else if(randomPRNG.nextInt(100) > ((50 + noOfUpclining) - noOfDeclining)) {
						noOfUpclining = noOfUpclining + 5;
						noOfDeclining = 0;
						value = value + nextValue;
					}
					else {
						noOfDeclining = noOfDeclining + 5;
						noOfUpclining = 0;
						value = value - nextValue;
					}
					
					//Measurement measurement = new Measurement(deviceId, type, unit, time, value);
					//fragment.getMeasurements().add(measurement);
					
					minute = minute + 5;
					if(minute >= 60) {
						hour = hour + 1;
						minute = minute - 60;
					}
				}
				dataset.add(fragment);
			}

		}

		return dataset;
	}

}
