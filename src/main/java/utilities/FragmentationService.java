package utilities;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import entities.Fragment;
import entities.Measurement;

public class FragmentationService {

	public static List<Fragment> getFragments(String fileName) {
		List<Fragment> fragments = new LinkedList<>();
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(new FileReader(FileService.FOLDER + fileName));
			
			for (Object measurementObject : array) {
				JSONObject jsonMeasurement = (JSONObject) measurementObject;
				String deviceId = (String) jsonMeasurement.get("deviceId");
				String type = (String) jsonMeasurement.get("type");
				String unit = (String) jsonMeasurement.get("unit");
				String timeString = (String) jsonMeasurement.get("time");
				
				if (timeString.contains("T")) {
					timeString = timeString.replace("T", " ");
				}
				
				if (timeString.contains(".")) {
					timeString = timeString.split("\\.")[0];
				}
				
				LocalDateTime time = LocalDateTime.parse(timeString, formatter);
				BigDecimal value = new BigDecimal(jsonMeasurement.get("value").toString());
				Measurement measurement = new Measurement(deviceId, type, unit, time, value);

				Fragment fragment = new Fragment(measurement.getDeviceId(), measurement.getType(),
						measurement.getUnit(), measurement.getTime().toLocalDate());
				if (fragments.contains(fragment)) {
					fragments.get(fragments.indexOf(fragment)).getMeasurements().add(measurement);
				} else {
					fragment.getMeasurements().add(measurement);
					fragments.add(fragment);
				}
			}
			
			Collections.sort(fragments);
			for (Fragment fragment : fragments) {
				Collections.sort(fragment.getMeasurements());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		
		return fragments;
	}
}
