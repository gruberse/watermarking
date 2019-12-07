package entities;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

public class DataProfile {

	private String datasetId;
	private String deviceId;
	private String type;
	private String unit;
	private BigDecimal valueBinSize;
	private BigDecimal slopeBinSize;
	private BigDecimal curvatureBinSize;
	private List<Measurement> measurements;
	private List<Bin<BigDecimal>> relativeValueDistribution;
	private List<Bin<BigDecimal>> relativeSlopeDistribution;
	private List<Bin<BigDecimal>> relativeCurvatureDistribution;

	public DataProfile(String datasetId, String deviceId, String type, String unit) {
		super();
		this.datasetId = datasetId;
		this.deviceId = deviceId;
		this.type = type;
		this.unit = unit;
		this.measurements = new LinkedList<>();
		this.relativeValueDistribution = new LinkedList<>();
		this.relativeSlopeDistribution = new LinkedList<>();
		this.relativeCurvatureDistribution = new LinkedList<>();
	}

	public void setRelativeValueDistributionFromStringArray(String[] array) {
		this.relativeValueDistribution = new LinkedList<>();
		for (int i = 0; i < array.length; i++) {
			BigDecimal minimum = new BigDecimal(array[i].split(",")[0].replace("[", ""));
			BigDecimal maximum = new BigDecimal(array[i].split(",")[1].split("\\)")[0]);
			BigDecimal value = new BigDecimal(array[i].split(": ")[1]);
			this.relativeValueDistribution.add(new Bin<BigDecimal>(minimum, maximum, value));
		}
	}

	public void setRelativeSlopeDistributionFromStringArray(String[] array) {
		this.relativeSlopeDistribution = new LinkedList<>();
		for (int i = 0; i < array.length; i++) {
			BigDecimal minimum = new BigDecimal(array[i].split(",")[0].replace("[", ""));
			BigDecimal maximum = new BigDecimal(array[i].split(",")[1].split("\\)")[0]);
			BigDecimal value = new BigDecimal(array[i].split(": ")[1]);
			this.relativeSlopeDistribution.add(new Bin<BigDecimal>(minimum, maximum, value));
		}
	}

	public void setRelativeCurvatureDistributionFromStringArray(String[] array) {
		this.relativeCurvatureDistribution = new LinkedList<>();
		for (int i = 0; i < array.length; i++) {
			BigDecimal minimum = new BigDecimal(array[i].split(",")[0].replace("[", ""));
			BigDecimal maximum = new BigDecimal(array[i].split(",")[1].split("\\)")[0]);
			BigDecimal value = new BigDecimal(array[i].split(": ")[1]);
			this.relativeCurvatureDistribution.add(new Bin<BigDecimal>(minimum, maximum, value));
		}
	}

	public String[] getRelativeValueDistributionAsStringArray() {
		String[] stringArray = new String[getRelativeValueDistribution().size()];
		for (int i = 0; i < getRelativeValueDistribution().size(); i++) {
			stringArray[i] = getRelativeValueDistribution().get(i).toString();
		}
		return stringArray;
	}

	public List<Bin<BigDecimal>> getRelativeValueDistributionBins(BigDecimal minimum, BigDecimal maximum) {
		List<Bin<BigDecimal>> bins = new LinkedList<>();
		for (Bin<BigDecimal> bin : getRelativeValueDistribution()) {
			if (bin.getMinimum().compareTo(minimum) >= 0 && bin.getMaximum().compareTo(maximum) <= 0) {
				bins.add(bin);
			}
		}
		return bins;
	}

	public String[] getRelativeSlopeDistributionAsStringArray() {
		String[] stringArray = new String[getRelativeSlopeDistribution().size()];
		for (int i = 0; i < getRelativeSlopeDistribution().size(); i++) {
			stringArray[i] = getRelativeSlopeDistribution().get(i).toString();
		}
		return stringArray;
	}
	
	public List<Bin<BigDecimal>> getRelativeSlopeDistributionBins(BigDecimal minimum, BigDecimal maximum) {
		List<Bin<BigDecimal>> bins = new LinkedList<>();
		for (Bin<BigDecimal> bin : getRelativeSlopeDistribution()) {
			if (bin.getMinimum().compareTo(minimum) >= 0 && bin.getMaximum().compareTo(maximum) <= 0) {
				bins.add(bin);
			}
		}
		return bins;
	}

	public String[] getRelativeCurvatureDistributionAsStringArray() {
		String[] stringArray = new String[getRelativeCurvatureDistribution().size()];
		for (int i = 0; i < getRelativeCurvatureDistribution().size(); i++) {
			stringArray[i] = getRelativeCurvatureDistribution().get(i).toString();
		}
		return stringArray;
	}
	
	public List<Bin<BigDecimal>> getRelativeCurvatureDistributionBins(BigDecimal minimum, BigDecimal maximum) {
		List<Bin<BigDecimal>> bins = new LinkedList<>();
		for (Bin<BigDecimal> bin : getRelativeCurvatureDistribution()) {
			if (bin.getMinimum().compareTo(minimum) >= 0 && bin.getMaximum().compareTo(maximum) <= 0) {
				bins.add(bin);
			}
		}
		return bins;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datasetId == null) ? 0 : datasetId.hashCode());
		result = prime * result + ((deviceId == null) ? 0 : deviceId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataProfile other = (DataProfile) obj;
		if (datasetId == null) {
			if (other.datasetId != null)
				return false;
		} else if (!datasetId.equals(other.datasetId))
			return false;
		if (deviceId == null) {
			if (other.deviceId != null)
				return false;
		} else if (!deviceId.equals(other.deviceId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

	public String getDatasetId() {
		return datasetId;
	}

	public void setDatasetId(String datasetId) {
		this.datasetId = datasetId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

	public List<Measurement> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(List<Measurement> measurements) {
		this.measurements = measurements;
	}

	public List<Bin<BigDecimal>> getRelativeValueDistribution() {
		return relativeValueDistribution;
	}

	public void setRelativeValueDistribution(List<Bin<BigDecimal>> relativeValueDistribution) {
		this.relativeValueDistribution = relativeValueDistribution;
	}

	public List<Bin<BigDecimal>> getRelativeSlopeDistribution() {
		return relativeSlopeDistribution;
	}

	public void setRelativeSlopeDistribution(List<Bin<BigDecimal>> relativeSlopeDistribution) {
		this.relativeSlopeDistribution = relativeSlopeDistribution;
	}

	public List<Bin<BigDecimal>> getRelativeCurvatureDistribution() {
		return relativeCurvatureDistribution;
	}

	public void setRelativeCurvatureDistribution(List<Bin<BigDecimal>> relativeCurvatureDistribution) {
		this.relativeCurvatureDistribution = relativeCurvatureDistribution;
	}

	public BigDecimal getValueBinSize() {
		return valueBinSize;
	}

	public void setValueBinSize(BigDecimal valueBinSize) {
		this.valueBinSize = valueBinSize;
	}

	public BigDecimal getSlopeBinSize() {
		return slopeBinSize;
	}

	public void setSlopeBinSize(BigDecimal slopeBinSize) {
		this.slopeBinSize = slopeBinSize;
	}

	public BigDecimal getCurvatureBinSize() {
		return curvatureBinSize;
	}

	public void setCurvatureBinSize(BigDecimal curvatureBinSize) {
		this.curvatureBinSize = curvatureBinSize;
	}
}
