package utils;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Fragment;
import entities.Measurement;

public class FragmentationService {

	public static List<Fragment> getFragments(String location) {
		return getFragmentsFromMeasurements(getMeasurementsFromJsonFile(location));
	}

	private static List<Fragment> getFragmentsFromMeasurements(List<Measurement> measurements) {
		List<Fragment> list = new LinkedList<>();

		for (Measurement measurement : measurements) {
			Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(), measurement.getUnit(),
					measurement.getTime().toLocalDate());

			if (!list.contains(fragment)) {
				JSONArray array = fragment.getData();
				array.add(convertMeasurement2JSONObject(measurement));
				fragment.setData(array);
				list.add(fragment);
			} else {
				JSONArray array = list.get(list.indexOf(fragment)).getData();
				array.add(convertMeasurement2JSONObject(measurement));
				list.get(list.indexOf(fragment)).setData(array);
			}
		}
		return list;
	}

	private static JSONObject convertMeasurement2JSONObject(Measurement measurement) {
		JSONObject object = new JSONObject();
		object.put("deviceId", measurement.getDeviceId());
		object.put("type", measurement.getType());
		object.put("unit", measurement.getUnit());
		object.put("time", measurement.getTime().toString());
		object.put("value", measurement.getValue());
		return object;
	}

	private static List<Measurement> getMeasurementsFromJsonFile(String location) {
		List<Measurement> list = new LinkedList<>();
		JSONParser parser = new JSONParser();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		try {
			JSONArray array = (JSONArray) parser.parse(new FileReader(location));

			for (Object object : array) {
				JSONObject measurementObject = (JSONObject) object;

				String deviceId = (String) measurementObject.get("deviceId");
				String type = (String) measurementObject.get("type");
				String unit = (String) measurementObject.get("unit");
				String timeString = (String) measurementObject.get("time");
				LocalDateTime time = LocalDateTime.parse(timeString.replace("T", " ").split("\\.")[0], formatter);
				Double value = (Double) measurementObject.get("value");

				list.add(new Measurement(deviceId, type, unit, time, value));
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return list;
	}
}
