package services;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entities.Fragment;
import utils.FragmentationService;

public class DetectionService {

	public static void detectDataLeakage(String datasetLocation, String reportLocation) {
		printReport(reportLocation, detection(FragmentationService.getFragments(datasetLocation)));
	}

	private static String detection(List<Fragment> suspiciousFragments) {
		String report = new String();
		report = "Watermark Detection Report";
		for (Fragment suspiciousFragment : suspiciousFragments) {
			//TODO
		}
		return report;
	}

	private static void printReport(String location, String report) {
		try (FileWriter file = new FileWriter(location + "/detected.json")) {
			file.write(report);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
