package services;

import utils.DatabaseService;
import utils.FragmentationService;

public class ContainerService {

	public static void uploadDataset(String location) {
		DatabaseService.uploadFragments(FragmentationService.getFragments(location));
	}
}
