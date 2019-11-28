package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	public static void getDataset(String location, int dataUserId, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(deviceId, type, unit, from, to);
		List<Fragment> watermarkedFragments = watermarkEmbedding(dataUserId, originalFragments);
		provideFragments(location, dataUserId, watermarkedFragments);
	}

	public static void getDataset(String location, int dataUserId, int noOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		// TODO
	}

	private static void provideFragments(String location, int dataUserId, List<Fragment> fragments) {
		try (FileWriter file = new FileWriter(
				FileService.getFileName(location, "dataUser_" + dataUserId, "_request_"))) {
			String json = "[";
			for (int i = 0; i < fragments.size(); i++) {
				json = json + fragments.get(i).getMeasurementsAsJsonString();
				if (i + 1 < fragments.size())
					json = json + ",";
			}
			json = json + "\n]";
			file.write(json);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	// no consideration of noOfWatermarks, actual noOfWatermark and usability constraints
	private static List<Fragment> watermarkEmbedding(int dataUserId, List<Fragment> originalFragments) {
		LocalDateTime timestamp = LocalDateTime.now();
		List<Fragment> watermarkedFragments = new LinkedList<>();

		for (Fragment originalFragment : originalFragments) {
			Fragment watermarkedFragment = new Fragment();
			watermarkedFragment.setDeviceId(originalFragment.getDeviceId());
			watermarkedFragment.setType(originalFragment.getType());
			watermarkedFragment.setUnit(watermarkedFragment.getUnit());
			watermarkedFragment.setDate(originalFragment.getDate());
			watermarkedFragment.setMeasurements(new LinkedList<>());

			Double[] watermark = DatabaseService.getWatermark(originalFragment.getDeviceId(),
					originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate(), dataUserId);

			// Watermark Encoding
			if (watermark == null) {
				watermark = new Double[originalFragment.getMeasurements().size()];
				Random prng = new Random(Long.parseLong(dataUserId + "" + originalFragment.getSecretKey()));
				for (int i = 0; i < watermark.length; i++) {
					watermark[i] = prng.nextDouble();
				}

				DatabaseService.insertRequest(dataUserId, originalFragment.getDeviceId(), timestamp,
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate(), watermark);
			} else {
				DatabaseService.updateRequest(dataUserId, originalFragment.getDeviceId(), timestamp,
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			}

			// Watermark Embedding
			for (int i = 0; i < originalFragment.getMeasurements().size(); i++) {
				Measurement measurement = originalFragment.getMeasurements().get(i);
				measurement.setValue(measurement.getValue().doubleValue() + watermark[i]);
				watermarkedFragment.getMeasurements().add(measurement);
			}
			watermarkedFragments.add(watermarkedFragment);
		}
		return watermarkedFragments;
	}
}
