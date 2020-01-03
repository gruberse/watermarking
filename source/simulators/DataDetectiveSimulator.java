package simulators;

import java.math.BigDecimal;

import services.DetectionService;
import utils.LogService;
import utils.StopwatchService;

public class DataDetectiveSimulator {

	public static void detectLeakage(String datasetName, String reportName, Double fragmentSimilarityThreshold,
			Double watermarkSimilarityThreshold) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator",
				"detectLeakage(datasetName=" + datasetName + ", reportName=" + reportName
						+ ", fragmentSimilarityThreshold=" + fragmentSimilarityThreshold
						+ ", watermarkSimilarityThreshold=" + watermarkSimilarityThreshold + ")");
		
		StopwatchService stopwatchService = new StopwatchService();
		DetectionService.getReport(datasetName, reportName, BigDecimal.valueOf(fragmentSimilarityThreshold),
				BigDecimal.valueOf(watermarkSimilarityThreshold));
		stopwatchService.stop();
		
		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator", "detectLeakage", stopwatchService.getTime());
	}
}
