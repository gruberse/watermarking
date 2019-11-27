package utils;

import java.io.File;

public class FileService {
	
	public static String getFileName(String location, String containsTerm, String splitTerm) {
		int number = 0;
		for (File fileEntry : new File(location).listFiles()) {
			if (fileEntry.getName().contains(containsTerm)) {
				int curNumber = Integer.parseInt(fileEntry.getName().split(splitTerm)[1].split("\\.")[0]);
				if (curNumber > number) {
					number = curNumber;
				}
			}
		}
		number = number + 1;
		return location + containsTerm + splitTerm + number + ".json";
	}
	
	public static void cleanupFiles() {
		for (final File fileEntry : new File("files/").listFiles()) {
			if(fileEntry.getName().contains("request")
					|| fileEntry.getName().contains("report")) {
				fileEntry.delete();
			}
		}
	}
}
