package simulators;

import services.ContainerService;
import utils.LogService;
import utils.StopwatchService;

public class PatientSimulator {
	
	public static void storeDataset() {
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(files/testdata.json)");
		StopwatchService.start();
		ContainerService.uploadDataset("testdata.json");
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(files/testdata.json)", StopwatchService.stop());
	}

	public static void storeDataset(String datasetName) {
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(" + datasetName + ")");
		StopwatchService.start();
		ContainerService.uploadDataset(datasetName);
		LogService.log(LogService.SIMULATOR_LEVEL, "PatientSimulator", "storeDataset(" + datasetName + ")", StopwatchService.stop());
	}
}
