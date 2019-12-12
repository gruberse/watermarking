package entities;

public class IndexMap {

	private Integer suspiciousIndex;
	private Integer originalIndex;
	
	public IndexMap(Integer suspiciousIndex, Integer originalIndex) {
		this.suspiciousIndex = suspiciousIndex;
		this.originalIndex = originalIndex;
	}
	
	@Override
	public String toString() {
		return "IndexMap [suspiciousIndex=" + suspiciousIndex + ", originalIndex=" + originalIndex + "]";
	}

	public Integer getSuspiciousIndex() {
		return suspiciousIndex;
	}

	public void setSuspiciousIndex(Integer suspiciousIndex) {
		this.suspiciousIndex = suspiciousIndex;
	}

	public Integer getOriginalIndex() {
		return originalIndex;
	}

	public void setOriginalIndex(Integer originalIndex) {
		this.originalIndex = originalIndex;
	}
}
