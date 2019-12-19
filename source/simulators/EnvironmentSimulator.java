package simulators;

import java.math.BigDecimal;

import entities.UsabilityConstraint;
import utils.DatabaseService;
import utils.FileService;

public class EnvironmentSimulator {

	public static void reset() {
		FileService.deleteFiles("dataset");
		DatabaseService.deleteTable("request");
		DatabaseService.deleteTable("fragment");
		DatabaseService.deleteTable("usability_constraint");
	}
	
	public static void setUsabilityConstraint(Double maxError, int numberOfWatermarks, int numberOfRanges) {
		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint("cbg", "mmol/L", new BigDecimal("0.0"),
				new BigDecimal("55.0"), BigDecimal.valueOf(maxError), numberOfWatermarks, numberOfRanges));
	}
}
