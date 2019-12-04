package services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	public static void getDataset(String folderLocation, int dataUserId, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(deviceId, type, unit, from, to);
		List<Fragment> watermarkedFragments = watermarkEmbedding(dataUserId, type, unit, originalFragments);
		provideDataset(folderLocation, dataUserId, watermarkedFragments);
	}

	public static void getDataset(String folderLocation, int dataUserId, int noOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(noOfDevices, type, unit, from, to);
		List<Fragment> watermarkedFragments = watermarkEmbedding(dataUserId, type, unit, originalFragments);
		provideDataset(folderLocation, dataUserId, watermarkedFragments);
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

	private static List<Fragment> watermarkEmbedding(int dataUserId, String type, String unit, List<Fragment> originalFragments) {
		LocalDateTime timestamp = LocalDateTime.now();
		List<Fragment> watermarkedFragments = new LinkedList<>();
		
		// retrieve usability constraint
		Double maxError = 0.0; //DatabaseService.getUsabilityConstraint(type, unit);

		Collections.sort(originalFragments);
		for (Fragment originalFragment : originalFragments) {
			Fragment watermarkedFragment = new Fragment();
			watermarkedFragment.setDeviceId(originalFragment.getDeviceId());
			watermarkedFragment.setType(originalFragment.getType());
			watermarkedFragment.setUnit(watermarkedFragment.getUnit());
			watermarkedFragment.setDate(originalFragment.getDate());
			watermarkedFragment.setMeasurements(new LinkedList<>());

			// retrieve number of watermark from database if fragment was already requested by data
			// user
			Integer noOfWatermark = DatabaseService.getWatermark(dataUserId, originalFragment.getDeviceId(),
					originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			
			if(noOfWatermark == null) {
				noOfWatermark = 1;
			}

			// watermark generation
			Double[] watermark = new Double [originalFragment.getMeasurements().size()];
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
			watermarkedFragments.add(watermarkedFragment);
		}
		return watermarkedFragments;
	}
}
