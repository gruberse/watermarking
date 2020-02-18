package services;

import java.util.List;
import java.util.Random;

import entities.Fragment;
import utilities.DatabaseService;
import utilities.FragmentationService;
import utilities.LogService;
import utilities.TimeService;

public class ContainerService {

	public static void storeDataset(String datasetName) {
		// retrieve fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments", timeService.getTime());
		
		// complete fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKey");
		timeService = new TimeService();
		fragments = generateSecretKeys(fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKeys", timeService.getTime());
		
		// insert fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments");
		timeService = new TimeService();
		DatabaseService.insertFragments(fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments", timeService.getTime());
	}
	
	private static List<Fragment> generateSecretKeys(List<Fragment> fragments) {
		Random random = new Random();
		for (Fragment fragment : fragments) {
			fragment.setSecretKey(random.nextLong());
		}
		return fragments;
	}
}
