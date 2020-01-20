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

	public static void requestDataset(String datasetName, int dataUserId, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		
		// retrieve fragments
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = DatabaseService.getFragments(deviceId, type, unit, from, to);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments", timeService.getTime());

		// watermark embedding
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding");
		timeService = new TimeService();
		fragments = watermarkEmbedding(dataUserId, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding", timeService.getTime());
		
		// write to file system
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset");
		timeService = new TimeService();
		FileService.writeDataset(datasetName, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset", timeService.getTime());
	}

	public static void requestDataset(String datasetName, int dataUserId, int noOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		
		// retrieve fragments
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = DatabaseService.getFragments(noOfDevices, type, unit, from, to);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "DatabaseService.getFragments", timeService.getTime());

		// watermark embedding
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding");
		timeService = new TimeService();
		fragments = watermarkEmbedding(dataUserId, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "watermarkEmbedding", timeService.getTime());
		
		// write to file system
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset");
		timeService = new TimeService();
		FileService.writeDataset(datasetName, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "DataService", "FileService.writeDataset", timeService.getTime());
	}

	private static List<Fragment> watermarkEmbedding(int dataUserId, List<Fragment> fragments) {
		LocalDateTime timestamp = LocalDateTime.now();

		for (int i = 0; i < fragments.size(); i++) {
			
			LogService.log(LogService.METHOD_LEVEL, "watermarkEmbedding", "fragmentWatermarking");
			TimeService timeService = new TimeService();
			
			Fragment fragment = fragments.get(i);

			// retrieve usability constraint
			UsabilityConstraint usabilityConstraint = DatabaseService.getUsabilityConstraint(fragment.getType(),
					fragment.getUnit());

			// retrieve number of watermark from database
			Request request = DatabaseService.getRequest(dataUserId, fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getDate());

			// data user has not requested fragment yet
			if (request == null) {

				// retrieve next watermark number
				int numberOfWatermark = 1 + DatabaseService.getNumberOfWatermark(fragment.getDeviceId(),
						fragment.getType(), fragment.getUnit(), fragment.getDate());
				// limit number of watermarks
				if (numberOfWatermark > usabilityConstraint.getNumberOfWatermarks()) {
					numberOfWatermark = numberOfWatermark % usabilityConstraint.getNumberOfWatermarks();
				}

				ArrayList<LocalDateTime> timestamps = new ArrayList<>();
				timestamps.add(timestamp);

				request = new Request(fragment.getDeviceId(), dataUserId, fragment.getType(), fragment.getUnit(),
						fragment.getDate(), numberOfWatermark, timestamps);

				// insert new request
				DatabaseService.insertRequest(request);
			}
			// data user has already requested fragment
			else {
				request.getTimestamps().add(timestamp);

				// update existing request
				DatabaseService.updateRequest(request);
			}

			// generate watermark
			BigDecimal[] watermark = new BigDecimal[fragment.getMeasurements().size()];
			Fragment prevFragment = DatabaseService.getFragment(fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getDate().minusDays(1));
			Fragment nextFragment = DatabaseService.getFragment(fragment.getDeviceId(), fragment.getType(),
					fragment.getUnit(), fragment.getDate().plusDays(1));
			watermark = WatermarkService.generateWatermark(request, usabilityConstraint, fragment,
					prevFragment, nextFragment);

			// embed generated watermark
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				// System.out.println(fragment.getMeasurements().get(j).getValue().toString().replace(".",
				// ","));
				BigDecimal watermarkedValue = fragment.getMeasurements().get(j).getValue().add(watermark[j]);
				fragment.getMeasurements().get(j).setValue(watermarkedValue);
				// System.out.println(fragment.getMeasurements().get(j).getValue().toString().replace(".",
				// ","));
			}
			
			// update watermarked fragment
			fragments.set(i, fragment);

			timeService.stop();
			LogService.log(LogService.METHOD_LEVEL, "watermarkEmbedding", "fragment", timeService.getTime());
		}
		return fragments;
	}
}
