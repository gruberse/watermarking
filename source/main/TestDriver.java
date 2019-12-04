package main;

import java.math.BigDecimal;

import entities.UsabilityConstraint;
import services.ContainerService;
import utils.DatabaseService;
import utils.FileService;

public class TestDriver {

	public static void main(String[] args) {
		FileService.deleteFiles("files/", "request");
		FileService.deleteFiles("files/", "report");
		DatabaseService.deleteTable("fragment");
		DatabaseService.deleteTable("request");
		DatabaseService.deleteTable("data_profile");
		DatabaseService.deleteTable("usability_constraint");

		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint("cbg", "mmol/L", 5, new BigDecimal("0.0"),
				new BigDecimal("55.0"), new BigDecimal("0.5"), 10));

		// ContainerService.uploadDataset("files/testdata.json");
		ContainerService.uploadDataset("files/cbg_2017-02-04.json");

		// DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L",
		// LocalDate.of(2015, 12, 15),LocalDate.of(2015, 12, 15));
		// DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L",
		// LocalDate.of(2015, 12, 15),LocalDate.of(2015, 12, 15));
		// DataService.getDataset("files/", 2, "DexG4Rec_SM45143452", "cbg", "mmol/L",
		// LocalDate.of(2015, 12, 1),LocalDate.of(2015, 12, 31));
		// DataService.getDataset("files/", 3, 100, "cbg", "mmol/L", LocalDate.of(2015,
		// 12, 30), LocalDate.of(2016, 8, 7));

		// DetectionService.getLeakageReport("files/dataUser1request1.json", "files/",
		// 0.8, 0.8);
		// DetectionService.getLeakageReport("files/dataUser1request1.json", "files/",
		// 0.8, 0.8);
		// DetectionService.getLeakageReport("files/dataUser2request1.json", "files/",
		// 0.8, 0.8);
	}

}
