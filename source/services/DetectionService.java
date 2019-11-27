package services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entities.Fragment;
import utils.FileService;
import utils.FragmentationService;

public class DetectionService {

	public static void detectDataLeakage(String datasetLocation, String reportLocation) {
		List<Fragment> suspiciousFragments = FragmentationService.getFragments(datasetLocation);
		String report = detection(suspiciousFragments);
		provideReport(reportLocation, datasetLocation, report);
	}

	private static String detection(List<Fragment> suspiciousFragments) {
		String report = new String();
		report = "Watermark Detection Report";
		for (Fragment suspiciousFragment : suspiciousFragments) {
			//TODO
		}
		return report;
	}

	private static void provideReport(String reportLocation, String datasetLocation, String report) {
		String[] suspiciousDatasetAbbreviations = datasetLocation.split("/")[1].split("_");
		String abbreviation = new String();
		for(String term : suspiciousDatasetAbbreviations) {
			abbreviation = abbreviation + term.charAt(0);
		}
		
		try (FileWriter file = new FileWriter(FileService.getFileName(reportLocation,
				"detectionOf_" + abbreviation, "_report_"))){
			file.write(report);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
