package utils;

import java.io.File;

public class FileService {
	
	public static void cleanupFiles() {
		for (final File fileEntry : new File("files/").listFiles()) {
			if(fileEntry.getName().contains("request")
					|| fileEntry.getName().contains("report")) {
				fileEntry.delete();
			}
		}
	}
}
