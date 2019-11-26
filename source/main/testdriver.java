package main;

import services.ContainerService;
import utils.CleanupService;

public class testdriver {

	public static void main(String[] args) {
		CleanupService.cleanupFragments();
		
		ContainerService.uploadDataset("files/testdata.json");

	}

}
