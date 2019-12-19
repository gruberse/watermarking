package services;

import java.util.List;
import java.util.Random;

import entities.Fragment;
import utils.DatabaseService;
import utils.FragmentationService;

public class ContainerService {

	public static void uploadDataset(String datasetName) {
		// retrieve fragments
		List<Fragment> fragments = FragmentationService.getFragments(datasetName);

		// complete fragments
		Random random = new Random();
		Long min = Long.MIN_VALUE;
		Long max = Long.MAX_VALUE - Long.valueOf(Integer.MAX_VALUE);
		for (Fragment fragment : fragments) {
			fragment.setSecretKey(min + ((long) (random.nextDouble() * (max - min))));
		}
		// insert fragments and profiles
		DatabaseService.insertFragments(fragments);
	}
}
