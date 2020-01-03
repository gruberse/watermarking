package services;

import java.util.List;
import java.util.Random;

import entities.Fragment;
import utils.DatabaseService;
import utils.FragmentationService;
import utils.LogService;
import utils.StopwatchService;

public class ContainerService {

	public static void uploadDataset(String datasetName) {
		// retrieve fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments");
		StopwatchService stopwatchService = new StopwatchService();
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments", stopwatchService.getTime());
		
		// complete fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKey");
		stopwatchService = new StopwatchService();
		fragments = generateSecretKey(fragments);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "generateSecretKey", stopwatchService.getTime());
		
		// insert fragments
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments");
		stopwatchService = new StopwatchService();
		DatabaseService.insertFragments(fragments);
		stopwatchService.stop();
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseService.insertFragments", stopwatchService.getTime());
	}
	
	private static List<Fragment> generateSecretKey(List<Fragment> fragments) {
		Random random = new Random();
		Long min = Long.MIN_VALUE;
		Long max = Long.MAX_VALUE - Long.valueOf(Integer.MAX_VALUE);
		for (Fragment fragment : fragments) {
			fragment.setSecretKey(min + ((long) (random.nextDouble() * (max - min))));
		}
		return fragments;
	}
}
