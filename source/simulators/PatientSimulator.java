package simulators;

import services.ContainerService;
import utils.LogService;
import utils.StopwatchService;

public class PatientSimulator {
	
	public static void storeDataset() {
		storeDataset("testdata.json");
	}

	public static void storeDataset(String datasetName) {
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(datasetName=" + datasetName + ")");

		StopwatchService stopwatchService = new StopwatchService();
		ContainerService.uploadDataset(datasetName);
		stopwatchService.stop();
		
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset", stopwatchService.getTime());
	}
}
