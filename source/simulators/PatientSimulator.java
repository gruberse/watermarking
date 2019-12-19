package simulators;

import services.ContainerService;

public class PatientSimulator {
	
	public static void storeDataset() {
		ContainerService.uploadDataset("files/testdata.json");
	}

	public static void storeDataset(String datasetName) {
		ContainerService.uploadDataset(datasetName);
	}
}
