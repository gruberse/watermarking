package testdrivers;

import java.math.BigDecimal;
import java.time.LocalDate;

import entities.UsabilityConstraint;
import services.ContainerService;
import services.DataService;
import services.DetectionService;
import utils.DatabaseService;

public class TestDriver {

	public static void main(String[] args) {
		//DatabaseService.deleteTable("request");
		//DatabaseService.deleteTable("fragment");
		//DatabaseService.deleteTable("data_profile");
		DatabaseService.deleteTable("usability_constraint");

		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint("cbg", "mmol/L", new BigDecimal("0.0"),
				new BigDecimal("55.0"), new BigDecimal("0.5"), 256, 10));

		//ContainerService.uploadDataset("files/testdata.json");
		
		//DataService.getDataset(2, "files/request1.json", 1, "DexG5MobRec_SM64305440", "cbg", "mmol/L", LocalDate.of(2017, 2, 4),LocalDate.of(2017, 2, 4));
		//DataService.getDataset(2, "files/request2.json", 1, 100, "cbg", "mmol/L", LocalDate.of(2015, 12, 30), LocalDate.of(2016, 8, 7));

		DetectionService.getLeakageReport("files/request1.json", "files/report1.txt", BigDecimal.valueOf(0.8), BigDecimal.valueOf(0.8));
	}

}
