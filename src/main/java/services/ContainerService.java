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
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments");
		TimeService timeService = new TimeService();
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments",
				timeService.getTime());
		
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKeys");
		timeService = new TimeService();
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
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKeys",
				timeService.getTime());

		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments");
		timeService = new TimeService();
		DatabaseService.insertFragments(fragments);
		timeService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments",
				timeService.getTime());
	}

}
