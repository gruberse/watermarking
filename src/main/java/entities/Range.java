package entities;

import java.math.BigDecimal;

public class Range<T> implements Comparable<Range<T>> {

	private BigDecimal minimum;
	private BigDecimal maximum;
	private T value;

	public Range(BigDecimal minimum, BigDecimal maximum) {
		this.minimum = minimum;
		this.maximum = maximum;
	}

	public Range(BigDecimal minimum, BigDecimal maximum, T value) {
		this.minimum = minimum;
		this.maximum = maximum;
		this.value = value;
	}

	@Override
	public int compareTo(Range<T> o) {
		if (getMinimum() == null || o.getMinimum() == null) {
			return 0;
		}
		return getMinimum().compareTo(o.getMinimum());
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
