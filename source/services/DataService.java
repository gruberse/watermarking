package services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import entities.Fragment;
import utils.DatabaseService;

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
		int no = 0;
		for (File fileEntry : new File(location).listFiles()) {
			if (fileEntry.getName().contains("dataUser_" + dataUserId + "_request")) {
				int curNo = Integer.parseInt(fileEntry.getName().split("_request_")[1].split("\\.")[0]);
				if (curNo > no) {
					no = curNo;
				}
			}
		}
		no = no + 1;
		try (FileWriter file = new FileWriter(
				location + "/dataUser_" + dataUserId + "_request_" + no + ".json")) {
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
