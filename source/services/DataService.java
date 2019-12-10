package services;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entities.DataProfile;
import entities.Fragment;
import entities.Request;
import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.WatermarkGenerationService;

public class DataService {

	public static void getDataset(int method, String fileName, int dataUserId, String deviceId, String type,
			String unit, LocalDate from, LocalDate to) {
		// provide watermarked dataset
		provideDataset(fileName,
				// watermark fragments
				watermarkEmbedding(method, dataUserId,
						// retrieve fragments
						DatabaseService.getFragments(deviceId, type, unit, from, to)));
	}

	public static void getDataset(int method, String fileName, int dataUserId, int noOfDevices, String type,
			String unit, LocalDate from, LocalDate to) {
		// provide dataset
		provideDataset(fileName,
				// watermark fragments
				watermarkEmbedding(method, dataUserId,
						// retrieve fragments
						DatabaseService.getFragments(noOfDevices, type, unit, from, to)));
	}

	private static void provideDataset(String fileName, List<Fragment> fragments) {
		String jsonString = "[";
		for (int i = 0; i < fragments.size(); i++) {
			jsonString = jsonString + fragments.get(i).getMeasurementsAsJsonString();
			if (i + 1 < fragments.size())
				jsonString = jsonString + ",";
		}
		jsonString = jsonString + "\n]";

		try (FileWriter file = new FileWriter(fileName)) {
			file.write(jsonString);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static List<Fragment> watermarkEmbedding(int method, int dataUserId, List<Fragment> fragments) {
		LocalDateTime timestamp = LocalDateTime.now();

		Collections.sort(fragments);
		for (int i = 0; i < fragments.size(); i++) {
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
			if (method == 1) {
				DataProfile dataProfile = DatabaseService.getDataProfile(fragment.getDatasetId(),
						fragment.getDeviceId(), fragment.getType(), fragment.getUnit());
				watermark = WatermarkGenerationService.generateWatermark_w1(request, usabilityConstraint, dataProfile,
						fragment);
			} else {
				Fragment prevFragment = DatabaseService.getFragment(fragment.getDeviceId(), fragment.getType(),
						fragment.getUnit(), fragment.getDate().minusDays(1));
				Fragment nextFragment = DatabaseService.getFragment(fragment.getDeviceId(), fragment.getType(),
						fragment.getUnit(), fragment.getDate().plusDays(1));
				watermark = WatermarkGenerationService.generateWatermark_w2(request, usabilityConstraint, fragment,
						prevFragment, nextFragment);
			}

			// embed generated watermark
			for (int j = 0; j < fragment.getMeasurements().size(); j++) {
				BigDecimal originalValue = fragment.getMeasurements().get(j).getValue();
				BigDecimal watermarkedValue = originalValue.add(watermark[j]);
				fragment.getMeasurements().get(j).setValue(watermarkedValue);
				//System.out.println(fragment.getMeasurements().get(j).getValue().toString().replace(".",
				//","));
			}
			// update watermarked fragment
			fragments.set(i, fragment);
		}
		return fragments;
	}
}
