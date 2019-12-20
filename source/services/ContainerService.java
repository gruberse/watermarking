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
		StopwatchService.start();
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "FragmentationService.getFragments(" + datasetName + ")", StopwatchService.stop());
		
		// complete fragments
		StopwatchService.start();
		Random random = new Random();
		Long min = Long.MIN_VALUE;
		Long max = Long.MAX_VALUE - Long.valueOf(Integer.MAX_VALUE);
		for (Fragment fragment : fragments) {
			fragment.setSecretKey(min + ((long) (random.nextDouble() * (max - min))));
		}
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "setSecretKeys", StopwatchService.stop());
		
		// insert fragments
		StopwatchService.start();
		DatabaseService.insertFragments(fragments);
		LogService.log(LogService.SERVICE_LEVEL, "ContainerService", "DatabaseSerivce.insertFragments", StopwatchService.stop());
	}
}
