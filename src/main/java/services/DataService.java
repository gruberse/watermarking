package services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import entities.Fragment;
import entities.Request;
import entities.UsabilityConstraint;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.LogService;
import utilities.TimeService;
import utilities.WatermarkService;

public class DataService {

	public static void requestDataset(String datasetName, int dataUser, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = DatabaseService.getFragments(deviceId, type, unit, from, to);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments", timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding");
		timeService = new TimeService();
		fragments = embedWatermarks(dataUser, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding", timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset");
		timeService = new TimeService();
		FileService.writeDataset(datasetName, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset", timeService.getTime());
	}

	public static void requestDataset(String datasetName, int dataUser, int numberOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = DatabaseService.getFragments(numberOfDevices, type, unit, from, to);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments", timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding");
		timeService = new TimeService();
		fragments = embedWatermarks(dataUser, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding", timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset");
		timeService = new TimeService();
		FileService.writeDataset(datasetName, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset", timeService.getTime());
	}

	private static List<Fragment> embedWatermarks(int dataUser, List<Fragment> fragments) {
		LocalDateTime timestamp = LocalDateTime.now();
		
		for (int i = 0; i < fragments.size(); i++) {
			Fragment fragment = fragments.get(i);
			UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(fragment.getType(),
					fragment.getUnit());
			Request request = DatabaseService.getRequest(dataUser, fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getDate());
			
			if (request == null) {
				int numberOfWatermark = 1 + DatabaseService.getNumberOfWatermark(fragment.getDeviceId(),
						fragment.getType(), fragment.getUnit(), fragment.getDate());
				ArrayList<LocalDateTime> timestamps = new ArrayList<>();
				timestamps.add(timestamp);
				request = new Request(fragment.getDeviceId(), dataUser, fragment.getType(), fragment.getUnit(),
						fragment.getDate(), numberOfWatermark, timestamps);
				DatabaseService.insertRequest(request);
			} else {
				request.getTimestamps().add(timestamp);
				DatabaseService.updateRequest(request);
			}

			BigDecimal[] watermark = WatermarkService.generateWatermark(request, usabilityConstraint, fragment);
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal watermarkedValue = fragment.getMeasurements().get(j).getValue().add(watermark[j]);
				fragment.getMeasurements().get(j).setValue(watermarkedValue);
			}
			fragments.set(i, fragment);
		}
		
		return fragments;
	}
}
