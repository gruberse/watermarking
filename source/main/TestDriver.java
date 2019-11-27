package main;

import java.time.LocalDate;

import services.ContainerService;
import services.DataService;
import services.DetectionService;
import utils.DatabaseService;
import utils.FileService;

public class TestDriver {

	public static void main(String[] args) {
		DatabaseService.cleanupTable(DatabaseService.Tables.fragments);
		DatabaseService.cleanupTable(DatabaseService.Tables.requests);
		DatabaseService.cleanupTable(DatabaseService.Tables.ucs);
		
		FileService.cleanupFiles();
		
		ContainerService.uploadDataset("files/testdata.json");
		
		DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 15),
				LocalDate.of(2015, 12, 15));
		DataService.getDataset("files/", 2, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 15),
				LocalDate.of(2015, 12, 15));
		DataService.getDataset("files/", 1, "DexG4Rec_SM45143452", "cbg", "mmol/L", LocalDate.of(2015, 12, 1),
				LocalDate.of(2015, 12, 31));
		
		DetectionService.detectDataLeakage("files/dataUser_1_request_1.json", "files/");
		DetectionService.detectDataLeakage("files/dataUser_1_request_1.json", "files/");
		DetectionService.detectDataLeakage("files/dataUser_2_request_1.json", "files/");
	}

}
