package utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService {

	public static String SIMULATOR_LEVEL = "\t";
	public static String SERVICE_LEVEL = "\t\t";
	public static String METHOD_LEVEL = "\t\t\t";
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private static final String logName = "0_log.txt";

	public static void log(String level, String className, String operation) {
		FileService.writeLine(logName,
				LocalDateTime.now().format(formatter) + level + "[" + className + "]\t" + operation + ": ...");
	}

	public static void log(String level, String className, String operation, Long seconds) {
		FileService.writeLine(logName, LocalDateTime.now().format(formatter) + level + "[" + className + "]\t"
				+ operation + ": " + seconds + "sec.");
		if (level.contentEquals(SIMULATOR_LEVEL)) {
			FileService.writeLine(logName, "");
		}
	}

	public static void delete() {
		FileService.deleteFiles(logName);
	}
}
