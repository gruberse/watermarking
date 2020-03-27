package ui;

import java.util.Arrays;

import entities.UsabilityConstraint;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.PatientSimulator;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.LogService;

public class ProgramUI {

	public static void main(String[] args) {

		DatabaseService.deleteTable("fragment");
		DatabaseService.deleteTable("request");
		DatabaseService.deleteTable("usability_constraint");
		FileService.deleteFiles("Dataset");
		LogService.delete();

		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(0.5, 10));

		PatientSimulator.storeDataset(false, "testdata.json");
		
		DataUserSimulator.requestDataset(1, "DexG5MobRec_SM64305440", "2017-02-04", "2017-02-05");
		DataUserSimulator.requestDataset(2, "DexG5MobRec_SM64305440", "2017-02-04", "2017-02-04");
		DataDetectiveSimulator.detectLeakage("requestedDataset_by1_DexG5MobRec_SM64305440_2017-02-04_2017-02-05.json",
				0.01, 0.01, 2);
	}
}
