package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entities.Fragment;

public class FileService {

	public static String FOLDER = "files/";
	
	public static void writeDataset(String datasetName, List<Fragment> dataset) {
		String jsonString = "[";
		for (int i = 0; i < dataset.size(); i++) {
			jsonString = jsonString + dataset.get(i).getMeasurementsAsJsonString();
			if (i + 1 < dataset.size())
				jsonString = jsonString + ",";
		}
		jsonString = jsonString + "\n]";
		writeFile(datasetName, jsonString);
	}
	
	public static void writeLine(String fileName, String line) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(FileService.FOLDER + fileName, true))) {
			out.write(line + "\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void writeFile(String fileName, String message) {
		try (FileWriter file = new FileWriter(FOLDER + fileName)) {
			file.write(message);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void deleteFiles() {
		for (File file : new File(FOLDER).listFiles()) {
			if (file.getName().contains("Dataset") || file.getName().contains("report")) {
				file.delete();
			}
		}
	}
	
	public static void deleteFiles(String name) {
		for (File file : new File(FOLDER).listFiles()) {
			if (file.getName().contains(name)) {
				file.delete();
			}
		}
	}
}
