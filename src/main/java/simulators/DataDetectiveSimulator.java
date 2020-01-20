package simulators;

import java.math.BigDecimal;

import services.DetectionService;
import utilities.LogService;
import utilities.TimeService;

public class DataDetectiveSimulator {

	public static void detectLeakage(String datasetName, String reportName, Double fragmentSimilarityThreshold,
			Double watermarkSimilarityThreshold) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator",
				"detectLeakage(datasetName=" + datasetName + ", reportName=" + reportName
						+ ", fragmentSimilarityThreshold=" + fragmentSimilarityThreshold
						+ ", watermarkSimilarityThreshold=" + watermarkSimilarityThreshold + ")");
		
		TimeService timeService = new TimeService();
		DetectionService.detectLeakage(datasetName, reportName, BigDecimal.valueOf(fragmentSimilarityThreshold),
				BigDecimal.valueOf(watermarkSimilarityThreshold));
		timeService.stop();
		
		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator", "detectLeakage", timeService.getTime());
	}
}
