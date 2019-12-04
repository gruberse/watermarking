package entities;

import java.math.BigDecimal;

public class UsabilityConstraint {

	private String type;
	private String unit;
	private Integer frequency;
	private BigDecimal minimumValue;
	private BigDecimal maximumValue;
	private BigDecimal maximumError;
	private Integer numberOfRanges;
	
	public UsabilityConstraint(String type, String unit, Integer frequency, BigDecimal minimumValue, BigDecimal maximumValue,
			BigDecimal maximumError, Integer numberOfRanges) {
		super();
		this.type = type;
		this.unit = unit;
		this.frequency = frequency;
		this.minimumValue = minimumValue;
		this.maximumValue = maximumValue;
		this.maximumError = maximumError;
		this.numberOfRanges = numberOfRanges;
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

	public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

	public BigDecimal getMinimumValue() {
		return minimumValue;
	}

	public void setMinimumValue(BigDecimal minimumValue) {
		this.minimumValue = minimumValue;
	}

	public BigDecimal getMaximumValue() {
		return maximumValue;
	}

	public void setMaximumValue(BigDecimal maximumValue) {
		this.maximumValue = maximumValue;
	}

	public BigDecimal getMaximumError() {
		return maximumError;
	}

	public void setMaximumError(BigDecimal maximumError) {
		this.maximumError = maximumError;
	}

	public Integer getNumberOfRanges() {
		return numberOfRanges;
	}

	public void setNumberOfRanges(Integer numberOfRanges) {
		this.numberOfRanges = numberOfRanges;
	}
	
}
