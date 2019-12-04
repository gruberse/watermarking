package entities;

import java.math.BigDecimal;

public class Bin<T> implements Comparable<Bin<T>> {

	private BigDecimal minimum;
	private BigDecimal maximum;
	private T value;
	
	public Bin(BigDecimal minimum, BigDecimal maximum) {
		super();
		this.minimum = minimum;
		this.maximum = maximum;
	}
	
	public Bin(BigDecimal minimum, BigDecimal maximum, T value) {
		super();
		this.minimum = minimum;
		this.maximum = maximum;
		this.value = value;
	}
	
	@Override
	public int compareTo(Bin<T> o) {
		if (getMinimum() == null || o.getMinimum() == null) {
			return 0;
		}
		return getMinimum().compareTo(o.getMinimum());
	}
	
	@Override
	public String toString() {
		return "[" + this.minimum + "," + this.maximum + "): " + value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bin other = (Bin) obj;
		if (maximum == null) {
			if (other.maximum != null)
				return false;
		} else if (!maximum.equals(other.maximum))
			return false;
		if (minimum == null) {
			if (other.minimum != null)
				return false;
		} else if (!minimum.equals(other.minimum))
			return false;
		return true;
	}

	public BigDecimal getMinimum() {
		return minimum;
	}

	public void setMinimum(BigDecimal minimum) {
		this.minimum = minimum;
	}

	public BigDecimal getMaximum() {
		return maximum;
	}

	public void setMaximum(BigDecimal maximum) {
		this.maximum = maximum;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}
