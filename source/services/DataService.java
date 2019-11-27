package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import entities.Fragment;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	public static void getDataset(String location, int dataUserId, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		List<Fragment> originalFragments = DatabaseService.getFragments(deviceId, type, unit, from, to);
		List<Fragment> watermarkedFragments = watermarking(originalFragments);
		provideFragments(location, dataUserId, watermarkedFragments);
	}

	public static void getDataset(String location, int dataUserId, int noOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		// TODO
	}

	private static void provideFragments(String location, int dataUserId, List<Fragment> fragments) {
		try (FileWriter file = new FileWriter(FileService.getFileName(location,
				"dataUser_" + dataUserId, "_request_"))){
			String json = "[";
			for (int i = 0; i < fragments.size(); i++) {
				json = json + fragments.get(i).getMeasurementsJsonString(false);
				if (i + 1 < fragments.size())
					json = json + ",";
			}
			json = json + "\n]";
			file.write(json);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static List<Fragment> watermarking(List<Fragment> originalFragments) {
		// TODO
		// List<Fragment> watermarkedFragments = new LinkedList<>();
		// return watermarkedFragments;
		return originalFragments;
	}
}
