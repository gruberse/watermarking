package services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import entities.Fragment;
import entities.Request;
import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	public static void getDataset(String fileName, int dataUserId, String deviceId, String type, String unit,
			int frequency, LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(deviceId, type, unit, frequency, from, to);
		List<Fragment> watermarkedFragments = watermarkEmbedding(dataUserId, originalFragments);
		// provideDataset(folderLocation, dataUserId, watermarkedFragments);
	}

	/*
	 * public static void getDataset(String folderLocation, int dataUserId, int
	 * noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
	 * List<Fragment> originalFragments = DatabaseService.getFragments(noOfDevices,
	 * type, unit, from, to); List<Fragment> watermarkedFragments =
	 * watermarkEmbedding(dataUserId, type, unit, originalFragments);
	 * provideDataset(folderLocation, dataUserId, watermarkedFragments); }
	 */

	private static List<Fragment> watermarkEmbedding(int dataUserId, List<Fragment> originalFragments) {
		LocalDateTime timestamp = LocalDateTime.now();
		List<Fragment> watermarkedFragments = new LinkedList<>();

		Collections.sort(originalFragments);
		for (Fragment fragment : originalFragments) {

			// retrieve usability constraint
			UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(fragment.getType(),
					fragment.getUnit(), fragment.getFrequency());

			// retrieve number of watermark from database
			Request request = DatabaseService.getRequest(dataUserId, fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getFrequency(), fragment.getDate());

			// data user has not requested fragment yet
			if (request == null) {

				// retrieve next watermark number
				int numberOfWatermark = 1 + DatabaseService.getNumberOfWatermark(fragment.getDeviceId(),
						fragment.getType(), fragment.getUnit(), fragment.getFrequency(), fragment.getDate());
				if(numberOfWatermark > usabilityConstraint.getNumberOfWatermarks()) {
					numberOfWatermark = numberOfWatermark % usabilityConstraint.getNumberOfWatermarks();
				}

				ArrayList<LocalDateTime> timestamps = new ArrayList<>();
				timestamps.add(timestamp);
				
				request = new Request(fragment.getDeviceId(), dataUserId, fragment.getType(), fragment.getUnit(),
						fragment.getFrequency(), fragment.getDate(), numberOfWatermark, timestamps);
				
				// insert new request
				DatabaseService.insertRequest(request);
			}
			// data user has already requested fragment
			else {
				request.getTimestamps().add(timestamp);
				
				// update existing request
				DatabaseService.updateRequest(request);
			}

			// watermark generation
/*			Double[] watermark = new Double[originalFragment.getMeasurements().size()];
			Random prng = new Random(Long.parseLong(noOfWatermark + "" + originalFragment.getSecretKey()));
			for (int i = 0; i < watermark.length; i++) {
				watermark[i] = prng.nextDouble() - 0.5;
			}

			if (noOfWatermark == 1) {
				// insert new request into the database
				DatabaseService.insertRequest(dataUserId, timestamp, noOfWatermark, originalFragment.getDeviceId(),
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			} else {
				// update request in the database
				DatabaseService.updateRequest(dataUserId, timestamp, originalFragment.getDeviceId(),
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			}

			// watermark embedding
			Collections.sort(originalFragment.getMeasurements());
			for (int i = 0; i < originalFragment.getMeasurements().size(); i++) {
				Measurement measurement = originalFragment.getMeasurements().get(i);
				measurement.setValue(measurement.getValue().add(BigDecimal.valueOf(watermark[i])));
				watermarkedFragment.getMeasurements().add(measurement);
			}
			watermarkedFragments.add(watermarkedFragment);*/
		}
		return watermarkedFragments;
	}

	private static void provideDataset(String requestLocation, int dataUserId, List<Fragment> fragments) {
		String json = "[";
		for (int i = 0; i < fragments.size(); i++) {
			json = json + fragments.get(i).getMeasurementsAsJsonString();
			if (i + 1 < fragments.size())
				json = json + ",";
		}
		json = json + "\n]";

		String fileName = FileService.getFileName(requestLocation, "dataUser" + dataUserId, "request", ".json");
		FileService.writeFile(fileName, json);
	}
}
