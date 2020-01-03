package simulators;

import java.time.LocalDate;

import services.DataService;
import utils.LogService;
import utils.StopwatchService;

public class DataUserSimulator {

	public static void requestDataset(String datasetName, int dataUserId, String deviceId, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset(datasetName=" + datasetName
				+ ", dataUserId=" + dataUserId + ", deviceId=" + deviceId + ", from=" + from + ", to=" + to + ")");
		
		StopwatchService stopwatchService = new StopwatchService();
		DataService.getDataset(datasetName, dataUserId, deviceId, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		stopwatchService.stop();
		
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset", stopwatchService.getTime());
	}

	public static void requestDataset(String datasetName, int dataUserId, int noOfDevices, String from, String to) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset(datasetName=" + datasetName
				+ ", dataUserId=" + dataUserId + ", noOfDevices=" + noOfDevices + ", from=" + from + ", to=" + to + ")");
		
		StopwatchService stopwatchService = new StopwatchService();
		DataService.getDataset(datasetName, dataUserId, noOfDevices, "cbg", "mmol/L", LocalDate.parse(from),
				LocalDate.parse(to));
		stopwatchService.stop();
		
		LogService.log(LogService.SIMULATOR_LEVEL, "DataUserSimulator", "requestDataset", stopwatchService.getTime());
	}
}
