package simulators;

import java.math.BigDecimal;

import services.DetectionService;
import utilities.LogService;
import utilities.TimeService;

public class DataDetectiveSimulator {

	public static void detectLeakage(String datasetName, Double fragmentSimilarityThreshold,
			Double watermarkSimilarityThreshold) {
		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator",
				"detectLeakage(datasetName=" + datasetName + ", fragmentSimilarityThreshold="
						+ fragmentSimilarityThreshold + ", watermarkSimilarityThreshold=" + watermarkSimilarityThreshold
					 + ")");

		TimeService timeService = new TimeService();
		String reportName = datasetName.substring(0, datasetName.indexOf(".json")) + "_report.txt";
		DetectionService.detectLeakage(datasetName, reportName, BigDecimal.valueOf(fragmentSimilarityThreshold),
				BigDecimal.valueOf(watermarkSimilarityThreshold));
		timeService.stop();

		LogService.log(LogService.SIMULATOR_LEVEL, "DataDetectiveSimulator", "detectLeakage", timeService.getTime());
	}
}
