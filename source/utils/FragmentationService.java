package utils;

import java.util.LinkedList;
import java.util.List;

import entities.Fragment;
import entities.Measurement;
import utils.JsonService.From;

public class FragmentationService {

	public static List<Fragment> getFragments(String location) {
		List<Measurement> measurements = JsonService.getMeasurementsFromJson(From.File, location);
		List<Fragment> fragments = getFragmentsFromMeasurements(measurements);
		return fragments;
	}

	private static List<Fragment> getFragmentsFromMeasurements(List<Measurement> measurements) {
		List<Fragment> list = new LinkedList<>();

		for (Measurement measurement : measurements) {
			Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(), measurement.getUnit(),
					measurement.getTime().toLocalDate());

			if (list.contains(fragment)) {
				list.get(list.indexOf(fragment)).getMeasurements().add(measurement);
			} else {
				fragment.getMeasurements().add(measurement);
				list.add(fragment);
			}
		}
		return list;
	}
}
