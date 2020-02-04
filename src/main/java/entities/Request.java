package entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Request {

	private String deviceId;
	private int dataUser;
	private String type;
	private String unit;
	private LocalDate date;
	private int numberOfWatermark;
	private ArrayList<LocalDateTime> timestamps;
	private int numberOfFragmentRequest;
	
	public Request(String deviceId, int dataUser, String type, String unit, LocalDate date, int numberOfWatermark, ArrayList<LocalDateTime> timestamps, int numberOfFragmentRequest) {
		super();
		this.deviceId = deviceId;
		this.dataUser = dataUser;
		this.type = type;
		this.unit = unit;
		this.date = date;
		this.numberOfWatermark = numberOfWatermark;
		this.timestamps = timestamps;
		this.numberOfFragmentRequest = numberOfFragmentRequest;
	}
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getDataUser() {
		return dataUser;
	}

	public void setDataUser(int dataUser) {
		this.dataUser = dataUser;
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

	public int getNumberOfFragmentRequest() {
		return numberOfFragmentRequest;
	}

	public void setNumberOfFragmentRequest(int numberOfFragmentRequest) {
		this.numberOfFragmentRequest = numberOfFragmentRequest;
	}
}
