package services;

import java.util.List;

import entities.Fragment;
import utils.DatabaseService;
import utils.FragmentationService;

public class ContainerService {

	public static void uploadDataset(String location) {
		List<Fragment> fragments = FragmentationService.getFragments(location);
		DatabaseService.uploadFragments(fragments);
	}
}
