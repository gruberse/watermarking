package utilities;

import java.util.concurrent.TimeUnit;

public class StopwatchService {

	private long startTime;
	private long endTime;
	
	public StopwatchService() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		endTime = System.nanoTime();
	}
	
	public Long getTime() {
		return TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
	}
}