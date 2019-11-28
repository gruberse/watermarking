package utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
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

	public static List<Fragment> getFragments(String location) {
		List<Measurement> measurements = getMeasurementsFromJson(From.File, location);
		List<Fragment> fragments = getFragmentsFromMeasurements(measurements);
		return fragments;
	}

	private static List<Fragment> getFragmentsFromMeasurements(List<Measurement> measurements) {
		List<Fragment> list = new LinkedList<>();
		Random prng = new Random();

		for (Measurement measurement : measurements) {
			Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(), measurement.getUnit(),
					measurement.getTime().toLocalDate());

			if (list.contains(fragment)) {
				list.get(list.indexOf(fragment)).getMeasurements().add(measurement);
			} else {
				fragment.getMeasurements().add(measurement);
				fragment.setSecretKey(Math.abs(prng.nextInt()));
				list.add(fragment);
			}
		}
		return list;
	}
	
	public enum From {
		String, File
	}

	public static List<Measurement> getMeasurementsFromJson(From source, String json) {
		List<Measurement> list = new LinkedList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		JSONParser parser = new JSONParser();

		try {
			JSONArray array = new JSONArray();
			if (source == From.File) {
				array = (JSONArray) parser.parse(new FileReader(json));
			} else if (source == From.String) {
				array = (JSONArray) parser.parse(json);
			} else {
				return new LinkedList<>();
			}

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

				list.add(new Measurement(deviceId, type, unit, time, value));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return list;
	}
}
