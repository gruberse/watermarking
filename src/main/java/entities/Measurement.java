package entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Measurement implements Comparable<Measurement> {

	private String deviceId;
	private String type;
	private String unit;
	private LocalDateTime time;
	private BigDecimal value;

	public Measurement() {
	}

	public Measurement(String deviceId, String type, String unit, LocalDateTime time, BigDecimal value) {
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.time = time;
		this.value = value;
	}

	@Override
	public int compareTo(Measurement o) {
		if (getTime() == null || o.getTime() == null) {
			return 0;
		}
		return getTime().compareTo(o.getTime());
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

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}
}
