package testdriver;

import java.util.Arrays;

import generators.AttackGenerator;
import generators.DataGenerator;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.PatientSimulator;

public class Test {
	
	public static void main(String[] args) {
		
		EnvironmentSetup.reset();
		//EnvironmentSetup.resetFiles();
		//EnvironmentSetup.deleteLogs();
		EnvironmentSetup.setUsabilityConstraint(0.5, 256, 10);
		EnvironmentSetup.startLogging("testcase1.txt");
		
		//PatientSimulator.storeDataset();
		
		DataGenerator.generateDataset("generated_dataset.json", 10, "2019-01-01", "2019-01-30");
		PatientSimulator.storeDataset("generated_dataset.json");
		
		DataUserSimulator.requestDataset("requested_dataset_1.json", 1, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("requested_dataset_2.json", 2, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("requested_dataset_3.json", 3, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("requested_dataset_4.json", 4, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("requested_dataset_5.json", 5, "Device0", "2019-01-01", "2019-01-30");
		
		DataDetectiveSimulator.detectLeakage("requested_dataset_1.json", "requested_dataset_1_report.txt", 0.8, 0.8);
		
		AttackGenerator.performDeletionAttack("requested_dataset_1.json", "requested_dataset_1_deletion.json", 2);
		DataDetectiveSimulator.detectLeakage("requested_dataset_1_deletion.json", "requested_dataset_1_deletion_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performRandomDistortionAttack("requested_dataset_1.json", "requested_dataset_1_random.json", 0.25);
		DataDetectiveSimulator.detectLeakage("requested_dataset_1_random.json", "requested_dataset_1_random_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performRoundingDistortionAttack("requested_dataset_1.json", "requested_dataset_1_rounding.json", 2);
		DataDetectiveSimulator.detectLeakage("requested_dataset_1_rounding.json", "requested_dataset_1_rounding_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performSubsetAttack("requested_dataset_1.json", "requested_dataset_1_subset.json", 50, 150);
		DataDetectiveSimulator.detectLeakage("requested_dataset_1_subset.json", "requested_dataset_1_subset_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performMeanCollusionAttack(Arrays.asList("requested_dataset_1.json", "requested_dataset_2.json"), 
				"collusion_dataset_1.json");
		DataDetectiveSimulator.detectLeakage("collusion_dataset_1.json", "collusion_dataset_1_report.txt", 
				0.8, 0.8);
		
		EnvironmentSetup.stopLogging();
	}

}
