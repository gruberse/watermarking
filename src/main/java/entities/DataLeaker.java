package entities;

import java.math.BigDecimal;

public class DataLeaker implements Comparable<DataLeaker> {

	private BigDecimal probability;
	private Integer dataUserId;
	
	public DataLeaker() {
	}

	public DataLeaker(BigDecimal probability, Integer dataUserId) {
		this.probability = probability;
		this.dataUserId = dataUserId;
	}
	
	@Override
	public String toString() {
		return "data user: " + dataUserId + ", probability: " + probability;
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
		if (dataUserId == null) {
			if (other.dataUserId != null)
				return false;
		} else if (!dataUserId.equals(other.dataUserId))
			return false;
		return true;
	}

	public BigDecimal getProbability() {
		return probability;
	}

	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	public Integer getDataUserId() {
		return dataUserId;
	}

	public void setDataUserId(Integer dataUserId) {
		this.dataUserId = dataUserId;
	}

}
