package entities;

import java.math.BigDecimal;

public class UsabilityConstraint {

	private String type;
	private String unit;
	private Integer numberOfRanges;
	private BigDecimal minimumValue;
	private BigDecimal maximumValue;
	private BigDecimal maximumError;

	public UsabilityConstraint(Double maximumError, Integer numberOfRanges) {
		this.type = "cbg";
		this.unit = "mmol/L";
		this.minimumValue = BigDecimal.valueOf(0.0);
		this.maximumValue = BigDecimal.valueOf(55.0);
		this.maximumError = BigDecimal.valueOf(maximumError);
		this.numberOfRanges = numberOfRanges;
	}

	public UsabilityConstraint(String type, String unit, BigDecimal minimumValue, BigDecimal maximumValue,
			BigDecimal maximumError, Integer numberOfRanges) {
		this.type = type;
		this.unit = unit;
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
