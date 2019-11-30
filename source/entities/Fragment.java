package entities;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class Fragment implements Comparable<Fragment> {

	private String deviceId;
	private String type;
	private String unit;
	private LocalDate date;
	private int secretKey;
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
	public String toString() {
		return "Fragment [deviceId=" + deviceId + ", type=" + type + ", unit=" + unit + ", date=" + date
				+ ", measurements=" + measurements + "]";
	}
	
	@Override
	public int compareTo(Fragment o) {
		if (getDate() == null || o.getDate() == null) {
			return 0;
		}
		return getDate().compareTo(o.getDate());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fragment other = (Fragment) obj;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
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

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}

	public int getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(int secretKey) {
		this.secretKey = secretKey;
	}
}
