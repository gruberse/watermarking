package utils;

import java.util.concurrent.TimeUnit;

public class StopwatchService {

	private static long startTime;
	private static long endTime;
	
	public static void start() {
		startTime = System.nanoTime();
	}
	
	public static Long stop() {
		endTime = System.nanoTime();
		return TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
	}
}
