package simulators;

import java.time.LocalDate;

import services.DataService;
import utils.LogService;
import utils.StopwatchService;

public class DataUserSimulator {

	public static void requestDataset(String datasetName, int dataUserId, String deviceId, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator",
				"requestDataset(" + datasetName + ", " + dataUserId + ", " + deviceId + ", " + from + ", " + to + ")");
		StopwatchService.start();
		DataService.getDataset(datasetName, dataUserId, deviceId, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator",
				"requestDataset(" + datasetName + ", " + dataUserId + ", " + deviceId + ", " + from + ", " + to + ")",
				StopwatchService.stop());
	}

	public static void requestDataset(String datasetName, int dataUserId, int noOfDevices, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator",
				"requestDataset(" + datasetName + ", " + dataUserId + ", " + noOfDevices + ", " + from + ", " + to + ")");
		StopwatchService.start();
		DataService.getDataset(datasetName, dataUserId, noOfDevices, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator",
				"requestDataset(" + datasetName + ", " + dataUserId + ", " + noOfDevices + ", " + from + ", " + to + ")",
				StopwatchService.stop());
	}
}
