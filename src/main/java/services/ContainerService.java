package services;

import java.util.List;
import java.util.Random;

import entities.Fragment;
import utilities.DatabaseService;
import utilities.FragmentationService;
import utilities.LogService;
import utilities.TimeService;

public class ContainerService {

	public static void storeDataset(Boolean randomSecretKey, String datasetName) {
		// retrieve fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments",
				timeService.getTime());

		// complete fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKey");
		timeService = new TimeService();
		fragments = generateSecretKeys(randomSecretKey, fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKeys", timeService.getTime());

		// insert fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments");
		timeService = new TimeService();
		DatabaseService.insertFragments(fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments",
				timeService.getTime());
	}

	private static List<Fragment> generateSecretKeys(Boolean randomSecretKey, List<Fragment> fragments) {
		if (randomSecretKey == true) {
			Random random = new Random();
			for (Fragment fragment : fragments) {
				fragment.setSecretKey(random.nextLong());
			}
		} else {
			for (Fragment fragment : fragments) {
				fragment.setSecretKey(1);
			}
		}
		return fragments;
	}
}
