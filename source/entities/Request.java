package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {

	private String deviceId;
	private Integer dataUserId;
	private String type;
	private String unit;
	private LocalDate date;
	private int numberOfWatermark;
	private ArrayList<LocalDateTime> timestamps;
	
	public Request(String deviceId, Integer dataUserId, String type, String unit, LocalDate date, int numberOfWatermark, ArrayList<LocalDateTime> timestamps) {
		super();
		this.deviceId = deviceId;
		this.dataUserId = dataUserId;
		this.type = type;
		this.unit = unit;
		this.date = date;
		this.numberOfWatermark = numberOfWatermark;
		this.timestamps = timestamps;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getDataUserId() {
		return dataUserId;
	}

	public void setDataUserId(Integer dataUserId) {
		this.dataUserId = dataUserId;
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

	public int getNumberOfWatermark() {
		return numberOfWatermark;
	}

	public void setNumberOfWatermark(int numberOfWatermark) {
		this.numberOfWatermark = numberOfWatermark;
	}

	public ArrayList<LocalDateTime> getTimestamps() {
		return timestamps;
	}

	public void setTimestamps(ArrayList<LocalDateTime> timestamps) {
		this.timestamps = timestamps;
	}
}
