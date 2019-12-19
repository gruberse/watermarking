package simulators;

import java.math.BigDecimal;

import services.DetectionService;

public class DataDetectiveSimulator {

	public static void detectLeakage(String datasetName, String reportName, Double fragmentSimilarityThreshold,
			Double watermarkSimilarityThreshold) {
		DetectionService.getReport(datasetName, reportName, BigDecimal.valueOf(fragmentSimilarityThreshold),
				BigDecimal.valueOf(watermarkSimilarityThreshold));
	}
}
