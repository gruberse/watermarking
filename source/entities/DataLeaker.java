package entities;

import java.math.BigDecimal;
import java.util.List;

public class DataLeaker implements Comparable<DataLeaker> {

	private BigDecimal probability;
	private List<Integer> dataUserIds;
	
	public DataLeaker() {
	}

	public DataLeaker(BigDecimal probability, List<Integer> dataUserIds) {
		this.probability = probability;
		this.dataUserIds = dataUserIds;
	}
	
	@Override
	public String toString() {
		return "probability: " + probability + ", data user: " + dataUserIds;
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
		if (dataUserIds == null) {
			if (other.dataUserIds != null)
				return false;
		} else if (!dataUserIds.equals(other.dataUserIds))
			return false;
		return true;
	}

	public BigDecimal getProbability() {
		return probability;
	}

	public void setProbability(BigDecimal probability) {
		this.probability = probability;
	}

	public List<Integer> getDataUserIds() {
		return dataUserIds;
	}

	public void setDataUserIds(List<Integer> dataUserIds) {
		this.dataUserIds = dataUserIds;
	}

}
