package entities;

import java.math.BigDecimal;
import java.util.List;

public class DataLeaker implements Comparable<DataLeaker> {

	private BigDecimal probability;
	private List<Integer> dataUsers;
	private BigDecimal[] watermark;
	
	public DataLeaker() {
	}
	
	public DataLeaker(BigDecimal probability, List<Integer> dataUsers) {
		this.probability = probability;
		this.dataUsers = dataUsers;
	}
	
	public DataLeaker(List<Integer> dataUsers, BigDecimal[] watermark) {
		this.dataUsers = dataUsers;
		this.watermark = watermark;
	}
	
	@Override
	public String toString() {
		return "probability: " + probability + "\tdata user: " + dataUsers.toString();
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
		if (dataUsers == null) {
			if (other.dataUsers != null)
				return false;
		} else if (!dataUsers.equals(other.dataUsers))
			return false;
		return true;
	}

	public BigDecimal getProbability() {
		return probability;
	}

	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	public List<Integer> getDataUsers() {
		return dataUsers;
	}

	public void setDataUsers(List<Integer> dataUsers) {
		this.dataUsers = dataUsers;
	}

	public BigDecimal[] getWatermark() {
		return watermark;
	}

	public void setWatermark(BigDecimal[] watermark) {
		this.watermark = watermark;
	}
}
