package entities;

import java.time.LocalDateTime;

public class Measurement {

	private String deviceId;
	private String type;
	private String unit;
	private LocalDateTime time;
	private Double value;
	public Measurement(String deviceId, String type, String unit, LocalDateTime time, Double value) {
		super();
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.time = time;
		this.value = value;
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
	public LocalDateTime getTime() {
		return time;
	}
	public void setTime(LocalDateTime time) {
		this.time = time;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "Measurement [deviceId=" + deviceId + ", type=" + type + ", unit=" + unit + ", time=" + time + ", value="
				+ value + "]";
	}
	
	
}
