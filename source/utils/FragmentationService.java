package utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Fragment;
import entities.Measurement;

public class FragmentationService {

	/**
	 * retrieves measurements from a json file consisting of an array. constructs
	 * fragments from the retrieved measurements.
	 * 
	 * @param fileLocation file location of json array
	 * @return list of fragments
	 */
	public static List<Fragment> getFragments(String fileLocation) {
		List<Fragment> fragments = new LinkedList<>();
		Random prng = new Random();

		// retrieve measurements from a json file
		List<Measurement> measurements = getMeasurements(Source.File, fileLocation);

		// constructs fragments from the retrieved measurements
		for (Measurement measurement : measurements) {
			Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(), measurement.getUnit(),
					measurement.getTime().toLocalDate());

			if (fragments.contains(fragment)) {
				fragments.get(fragments.indexOf(fragment)).getMeasurements().add(measurement);
			} else {
				fragment.getMeasurements().add(measurement);
				// add secret key for watermark embedding
				fragment.setSecretKey(Math.abs(prng.nextInt()));
				fragments.add(fragment);
			}
		}
		return fragments;
	}

	/**
	 * the json's source type
	 */
	public enum Source {
		String, File
	}

	/**
	 * retrieves measurements from a json file consisting of an array.
	 * 
	 * @param source the json's source type
	 * @param json   the json string or the json file location
	 * @return list of measurements
	 */
	public static List<Measurement> getMeasurements(Source source, String json) {
		List<Measurement> measurements = new LinkedList<>();

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			JSONParser parser = new JSONParser();
			JSONArray array = new JSONArray();

			// check the json's source type
			if (source == Source.File) {
				array = (JSONArray) parser.parse(new FileReader(json));
			} else if (source == Source.String) {
				array = (JSONArray) parser.parse(json);
			} else {
				return new LinkedList<>();
			}

			// retrieve measurements
			for (Object object : array) {
				JSONObject measurementObject = (JSONObject) object;

				String deviceId = (String) measurementObject.get("deviceId");
				String type = (String) measurementObject.get("type");
				String unit = (String) measurementObject.get("unit");
				String timeString = (String) measurementObject.get("time");
				if (timeString.contains("T")) {
					timeString = timeString.replace("T", " ");
				}
				if (timeString.contains(".")) {
					timeString = timeString.split("\\.")[0];
				}
				LocalDateTime time = LocalDateTime.parse(timeString, formatter);
				Double value = (Double) measurementObject.get("value");

				measurements.add(new Measurement(deviceId, type, unit, time, value));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return measurements;
	}

}
