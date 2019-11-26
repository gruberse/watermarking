package entities;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;

public class Fragment {
	
	private String deviceId;
	private String type;
	private String unit;
	private LocalDate date;
	private JSONArray data;
	
	public Fragment(String deviceId, String type, String unit, LocalDate date) {
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.date = date;
		this.data = new JSONArray();
	}
	@Override
	public String toString() {
		return "Fragment [deviceId=" + deviceId + ", type=" + type + ", unit=" + unit + ", date=" + date + ", data="
				+ data + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
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

	public JSONArray getData() {
		return data;
	}

	public void setData(JSONArray data) {
		this.data = data;
	}
	
}
