package utils;

public class CleanupService {
	
	public static void cleanup() {
		cleanupFragments();
		cleanupRequests();
		cleanupFiles();
	}

	public static void cleanupRequests() {
		
	}
	
	public static void cleanupFragments() {
		DatabaseService.cleanupFragments();
	}
	
	public static void cleanupFiles() {
		
	}
}
