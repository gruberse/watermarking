package utils;

import java.io.File;
import java.util.List;

public class FileService {

	/**
	 * deletes files from the folder location containing the name parts.
	 * 
	 * @param folderLocation folder location of the files
	 * @param fileNameParts  name parts of the files
	 */
	public static void deleteFiles(String folderLocation, List<String> fileNameParts) {
		for (final File fileEntry : new File(folderLocation).listFiles()) {
			for (String namePart : fileNameParts) {
				if (fileEntry.getName().contains(namePart)) {
					fileEntry.delete();
				}
			}
		}
	}

	/**
	 * generates a file name.
	 * 
	 * @param folderLocation folder location where the file should be provided to
	 * @param term1          first part of the file name
	 * @param term2          second part of the file name
	 * @param fileEnding     ending of file
	 * @return file name
	 */
	public static String getFileName(String folderLocation, String term1, String term2, String fileEnding) {
		int number = 0;
		for (File fileEntry : new File(folderLocation).listFiles()) {
			if (fileEntry.getName().contains(term1)) {
				int curNumber = Integer.parseInt(fileEntry.getName().split(term2)[1].split("\\.")[0]);
				if (curNumber > number) {
					number = curNumber;
				}
			}
		}
		number = number + 1;
		return folderLocation + term1 + term2 + number + fileEnding;
	}
}
