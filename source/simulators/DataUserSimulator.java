package simulators;

import java.time.LocalDate;

import services.DataService;

public class DataUserSimulator {

	public static void requestDataset(String datasetName, int dataUserId, String deviceId, String from,
			String to) {
		DataService.getDataset(datasetName, dataUserId, deviceId, "cbg", "mmol/L", LocalDate.parse(from), LocalDate.parse(to));
	}
	
	public static void requestDataset(String datasetName, int dataUserId, int noOfDevices, String from,
			String to) {
		DataService.getDataset(datasetName, dataUserId, noOfDevices, "cbg", "mmol/L", LocalDate.parse(from), LocalDate.parse(to));
	}
}
