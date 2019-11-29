package services;

import utils.DatabaseService;
import utils.FragmentationService;

public class ContainerService {

	/**
	 * constructs fragments from measurements. insert constructed fragments into
	 * the database.
	 * 
	 * @param fileLocation file location containing a single json array consisting
	 *                     of measurements
	 */
	public static void uploadDataset(String fileLocation) {
		// inserts fragments into the database
		DatabaseService.insertFragments(
				// constructs fragments from measurements
				FragmentationService.getFragments(fileLocation));
	}
}
