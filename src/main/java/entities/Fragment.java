package entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Fragment implements Comparable<Fragment> {

	private String deviceId;
	private String type;
	private String unit;
	private LocalDate date;
	private long secretKey;
	private List<Measurement> measurements;

	public Fragment() {
	}

	public Fragment(String deviceId, String type, String unit, LocalDate date) {
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.date = date;
		this.measurements = new LinkedList<Measurement>();
	}

	public Fragment(String deviceId, String type, String unit, LocalDate date, long secretKey) {
		super();
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.date = date;
		this.secretKey = secretKey;
		this.measurements = new LinkedList<Measurement>();
	}

	public void setMeasurementsFromJsonArrayString(String measurements) {
		this.measurements = new LinkedList<Measurement>();

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(measurements);

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
				LocalDateTime time = LocalDateTime.parse(timeString, formatter);
				BigDecimal value = new BigDecimal(measurementObject.get("value").toString());

				this.measurements.add(new Measurement(deviceId, type, unit, time, value));
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
	}

	public String getMeasurementsAsJsonArrayString() {
		return "[" + getMeasurementsAsJsonString() + "\n]";
	}

	public String getMeasurementsAsJsonString() {
		String json = new String();
		for (int i = 0; i < getMeasurements().size(); i++) {
			Measurement measurement = getMeasurements().get(i);
			json = json + "\n\t{";
			json = json + "\n\t\t\"deviceId\": \"" + measurement.getDeviceId() + "\",";
			json = json + "\n\t\t\"type\": \"" + measurement.getType() + "\",";
			json = json + "\n\t\t\"unit\": \"" + measurement.getUnit() + "\",";
			json = json + "\n\t\t\"time\": \"" + measurement.getTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					+ "\",";
			json = json + "\n\t\t\"value\": " + measurement.getValue();
			if (i + 1 < getMeasurements().size())
				json = json + "\n\t},";
			else {
				json = json + "\n\t}";
			}
		}
		return json;
	}

	@Override
	public int compareTo(Fragment o) {
		if (getDate() == null || o.getDate() == null) {
			return 0;
		}
		return getDate().compareTo(o.getDate());
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public long getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(long secretKey) {
		this.secretKey = secretKey;
	}

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}
}
