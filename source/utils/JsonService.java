package utils;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Measurement;

public class JsonService {

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
