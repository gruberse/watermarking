package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogService {

	public static String FOLDER = "logs/";
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static String SIMULATOR_LEVEL = "\t";
	public static String SERVICE_LEVEL = "\t\t";

	public static void startLogging() {
		try (FileWriter file = new FileWriter(FOLDER + "log.txt")) {
			file.write("START LOGGING ...\n");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void stopLogging() {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(FOLDER + "log.txt", true))) {
			out.append("\n\nSTOPPED LOGGING.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static void log(String level, String className, String operation) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(FOLDER + "log.txt", true))) {
			out.write("\n" + LocalDateTime.now().format(formatter) + level + "[" + className + "]\t" + operation + ": ...");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static void log(String level, String className, String operation, Long seconds) {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(FOLDER + "log.txt", true))) {
			out.write("\n" + LocalDateTime.now().format(formatter) + level + "[" + className + "]\t" + operation + ": " + seconds + "sec.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
