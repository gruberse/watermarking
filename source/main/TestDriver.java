package main;

import java.time.LocalDate;
import java.util.Arrays;

import generators.DataGenerator;
import services.ContainerService;
import services.DataService;
import services.DetectionService;
import simulators.EnvironmentSimulator;
import utils.DatabaseService;
import utils.FileService;

public class TestDriver {

	public static void main(String[] args) {
		
		EnvironmentSimulator.resetEnvironment();
		
		DataGenerator.addDataset(100, "cbg", "mmol/L", LocalDate.of(2019, 1, 1), LocalDate.of(2019, 12, 31));
		
		//ContainerService.uploadDataset("files/testdata.json");

		//DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 15),LocalDate.of(2015, 12, 15));
		//DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 15),LocalDate.of(2015, 12, 15));
		//DataService.getDataset("files/", 2, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 1),LocalDate.of(2015, 12, 31));
		//DataService.getDataset("files/", 3, 100, "cbg", "mmol/L", LocalDate.of(2015, 12, 30), LocalDate.of(2016, 8, 7));
		
		//DetectionService.getLeakageReport("files/dataUser1request1.json", "files/", 0.8, 0.8);
		//DetectionService.getLeakageReport("files/dataUser1request1.json", "files/", 0.8, 0.8);
		//DetectionService.getLeakageReport("files/dataUser2request1.json", "files/", 0.8, 0.8);
	}

}
