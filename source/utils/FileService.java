package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entities.Fragment;

public class FileService {
	
	private static final String folder = "files/";

	public static void writeDataset(String fileName, List<Fragment> dataset) {
		String jsonString = "[";
		for (int i = 0; i < dataset.size(); i++) {
			jsonString = jsonString + dataset.get(i).getMeasurementsAsJsonString();
			if (i + 1 < dataset.size())
				jsonString = jsonString + ",";
		}
		jsonString = jsonString + "\n]";

		writeFile(fileName, jsonString);
	}

	public static void writeFile(String fileName, String message) {
		try (FileWriter file = new FileWriter(fileName)) {
			file.write(message);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static String getFileName(String datasetName, String operation, String type) {
		datasetName = datasetName.replace(folder, "").split("\\.")[0];
		return folder + datasetName + "_" + operation + "." + type;
	}

	public static void deleteFiles(String type) {
		for (File file : new File(folder).listFiles()) {
			if(file.getName().contains(type)) {
				file.delete();
			}
		}
	}
}
