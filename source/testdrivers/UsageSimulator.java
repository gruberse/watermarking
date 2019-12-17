package testdrivers;

import java.time.LocalDate;
import java.util.Random;

import services.DataService;

public class UsageSimulator {

	public static void performPhysicianRequest(String fileName, int maxNoOfResearchers, int maxNoOfDevices, LocalDate minFrom, LocalDate maxTo) {
	}
	
	public static void performResearcherRequest(String fileName, int noOfDevices) {
		Random random = new Random();
		DataService.getDataset(2, fileName, random.nextInt(), noOfDevices, "cbg", "mmol/L");
	}
}
