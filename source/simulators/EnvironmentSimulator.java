package simulators;

import java.util.Arrays;

import utils.DatabaseService;
import utils.FileService;

public class EnvironmentSimulator {

	/**
	 * resets all request and report files. also, resets the requests and fragments
	 * table.
	 */
	public static void resetEnvironment() {
		FileService.deleteFiles("files/", Arrays.asList(new String[] { "request", "report" }));
		DatabaseService.deleteTable(DatabaseService.Tables.requests);
		DatabaseService.deleteTable(DatabaseService.Tables.fragments);
	}

	/**
	 * resets all request and report files.
	 */
	public static void resetFiles() {
		FileService.deleteFiles("files/", Arrays.asList(new String[] { "request", "report" }));
	}

	/**
	 * resets all request files.
	 */
	public static void resetRequestFiles() {
		FileService.deleteFiles("files/", Arrays.asList(new String[] { "request" }));
	}

	/**
	 * resets all report files.
	 */
	public static void resetReportFiles() {
		FileService.deleteFiles("files/", Arrays.asList(new String[] { "report" }));
	}

	/**
	 * resets the requests table.
	 */
	public static void resetRequestsTable() {
		DatabaseService.deleteTable(DatabaseService.Tables.requests);
	}

	/**
	 * resets the fragments table
	 */
	public static void resetFragmentsTable() {
		DatabaseService.deleteTable(DatabaseService.Tables.fragments);
	}
}
