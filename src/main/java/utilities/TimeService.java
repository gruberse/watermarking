package utilities;

import java.util.concurrent.TimeUnit;

public class TimeService {

	private long startTime;
	private long endTime;
	
	public TimeService() {
		startTime = System.nanoTime();
	}
	
	public void stop() {
		endTime = System.nanoTime();
	}
	
	public Long getTime() {
		return TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
	}
}