package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import entities.Fragment;

public class FileService {

	public static String FOLDER = "files/";

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
		try (FileWriter file = new FileWriter(FOLDER + fileName)) {
			file.write(message);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void deleteFiles(String term) {
		for (File file : new File(FOLDER).listFiles()) {
			if (file.getName().contains(term)) {
				file.delete();
			}
		}
	}
}
