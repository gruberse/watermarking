package entities;

import java.math.BigDecimal;

public class DataLeaker implements Comparable<DataLeaker> {

	private BigDecimal probability;
	private int dataUser;
	private BigDecimal[] watermark;
	
	public DataLeaker() {
	}
	
	public DataLeaker(BigDecimal probability, int dataUser) {
		this.probability = probability;
		this.dataUser = dataUser;
	}
	
	public DataLeaker(int dataUser, BigDecimal[] watermark) {
		this.dataUser = dataUser;
		this.watermark = watermark;
	}
	
	@Override
	public String toString() {
		return "probability of " + probability + "\tby data user " + dataUser;
	}
	
	@Override
	public int compareTo(DataLeaker o) {
		if (getProbability() == null || o.getProbability() == null) {
			return 0;
		}
		return o.getProbability().compareTo(getProbability());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataLeaker other = (DataLeaker) obj;
		if (dataUser != other.dataUser)
			return false;
		return true;
	}

	public BigDecimal getProbability() {
		return probability;
	}

	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	public int getDataUser() {
		return dataUser;
	}

	public void setDataUser(int dataUser) {
		this.dataUser = dataUser;
	}

	public BigDecimal[] getWatermark() {
		return watermark;
	}

	public void setWatermark(BigDecimal[] watermark) {
		this.watermark = watermark;
	}
}
