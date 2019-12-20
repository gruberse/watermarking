package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService {

	private static final String FOLDER = "logs/";
	private static String LOG_NAME = "";
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static String SIMULATOR_LEVEL = "\t";
	public static String SERVICE_LEVEL = "\t\t";

	public static void startLogging(String logName) {
		LOG_NAME = FOLDER + logName;
		try (FileWriter file = new FileWriter(LOG_NAME)) {
			file.write(LOG_NAME.toUpperCase() + " START LOGGING ...");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void stopLogging() {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(LOG_NAME, true))) {
			out.append("\n\n" + LOG_NAME.toUpperCase() + " STOPPED LOGGING.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void log(String level, String className, String operation) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(LOG_NAME, true))) {
			out.write("\n\n" + LocalDateTime.now().format(formatter) + level + "[" + className + "]\t" + operation + ": ...");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void log(String level, String className, String operation, Long seconds) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(LOG_NAME, true))) {
			out.write("\n" + LocalDateTime.now().format(formatter) + level + "[" + className + "]\t" + operation + ": " + seconds + "sec.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void deleteLogs() {
		for (File file : new File(FOLDER).listFiles()) {
			file.delete();
		}
	}
}
