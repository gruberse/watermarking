package testdriver;

import java.math.BigDecimal;

import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FileService;
import utils.LogService;

public class EnvironmentSetup {

	public static void reset() {
		FileService.deleteFiles("dataset");
		LogService.deleteLogs();
		DatabaseService.deleteTable("request");
		DatabaseService.deleteTable("fragment");
		DatabaseService.deleteTable("usability_constraint");
	}
	
	public static void resetDatabase() {
		DatabaseService.deleteTable("request");
		DatabaseService.deleteTable("fragment");
		//DatabaseService.deleteTable("usability_constraint");
	}
	
	public static void resetFiles() {
		FileService.deleteFiles("dataset");
	}
	
	public static void startLogging(String logName) {
		LogService.startLogging(logName);
	}
	
	public static void stopLogging() {
		LogService.stopLogging();
	}
	
	public static void resetLogs() {
		LogService.deleteLogs();
	}
	
	public static void setUsabilityConstraint(Double maxError, int numberOfWatermarks, int numberOfRanges) {
		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint("cbg", "mmol/L", new BigDecimal("0.0"),
				new BigDecimal("55.0"), BigDecimal.valueOf(maxError), numberOfWatermarks, numberOfRanges));
	}

}
