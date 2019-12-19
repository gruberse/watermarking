package testdriver;

import java.util.Arrays;

import generators.AttackGenerator;
import generators.DataGenerator;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.EnvironmentSimulator;
import simulators.PatientSimulator;

public class Test {
	
	public static void main(String[] args) {
		
		EnvironmentSimulator.reset();
		EnvironmentSimulator.setUsabilityConstraint(0.5, 256, 10);
		
		//PatientSimulator.storeDataset();
		
		DataGenerator.generateDataset("files/generated_dataset.json", 10, "2019-01-01", "2019-01-10");
		PatientSimulator.storeDataset("files/generated_dataset.json");
		
		DataUserSimulator.requestDataset("files/requested_dataset_1.json", 1, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("files/requested_dataset_2.json", 2, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("files/requested_dataset_3.json", 3, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("files/requested_dataset_4.json", 4, "Device0", "2019-01-01", "2019-01-30");
		DataUserSimulator.requestDataset("files/requested_dataset_5.json", 5, "Device0", "2019-01-01", "2019-01-30");
		
		DataDetectiveSimulator.detectLeakage("files/requested_dataset_1.json", "files/requested_dataset_1_report.txt", 0.8, 0.8);
		
		AttackGenerator.performDeletionAttack("files/requested_dataset_1.json", "files/requested_dataset_1_deletion.json", 2);
		DataDetectiveSimulator.detectLeakage("files/requested_dataset_1_deletion.json", "files/requested_dataset_1_deletion_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performRandomDistortionAttack("files/requested_dataset_1.json", "files/requested_dataset_1_random.json", 0.5);
		DataDetectiveSimulator.detectLeakage("files/requested_dataset_1_random.json", "files/requested_dataset_1_random_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performRoundingDistortionAttack("files/requested_dataset_1.json", "files/requested_dataset_1_rounding.json", 2);
		DataDetectiveSimulator.detectLeakage("files/requested_dataset_1_rounding.json", "files/requested_dataset_1_rounding_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performSubsetAttack("files/requested_dataset_1.json", "files/requested_dataset_1_subset.json", 50, 150);
		DataDetectiveSimulator.detectLeakage("files/requested_dataset_1_subset.json", "files/requested_dataset_1_subset_report.txt", 
				0.8, 0.8);
		
		AttackGenerator.performMeanCollusionAttack(Arrays.asList(
				"files/requested_dataset_1.json", 
				"files/requested_dataset_2.json"), "files/collusion_dataset_1.json");
		DataDetectiveSimulator.detectLeakage("files/collusion_dataset_1.json", "files/collusion_dataset_1_report.txt", 
				0.8, 0.8);
	}

}
