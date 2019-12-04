package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileService {

	public static void writeFile(String fileName, String fileContent) {
		try (FileWriter file = new FileWriter(fileName)) {
			file.write(fileContent);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void appendFile(String fileName, String content) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName, true));
			out.write(content);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void deleteFiles(String folderLocation, String namePart) {
		for (final File fileEntry : new File(folderLocation).listFiles()) {
			if (fileEntry.getName().contains(namePart)) {
				fileEntry.delete();
			}
		}
	}

	public static String getFileName(String folderLocation, String containingTerm, String splittingTerm,
			String fileEnding) {
		int number = 0;
		for (File fileEntry : new File(folderLocation).listFiles()) {
			if (fileEntry.getName().contains(containingTerm)) {
				int curNumber = Integer.parseInt(fileEntry.getName().split(splittingTerm)[1].split("\\.")[0]);
				if (curNumber > number) {
					number = curNumber;
				}
			}
		}
		number = number + 1;
		return folderLocation + containingTerm + splittingTerm + number + fileEnding;
	}
}
