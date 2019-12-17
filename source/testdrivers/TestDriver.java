package testdrivers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import entities.UsabilityConstraint;
import services.ContainerService;
import services.DataService;
import services.DetectionService;
import utils.DatabaseService;
import utils.FileService;

public class TestDriver {
	
	public static void main(String[] args) {
		
		FileService.deleteFiles("dataset");
		
		//DatabaseService.deleteTable("request");
		//DatabaseService.deleteTable("fragment");
		//DatabaseService.deleteTable("data_profile");
		DatabaseService.deleteTable("usability_constraint");
		DatabaseService.insertUsabilityConstraint(new UsabilityConstraint("cbg", "mmol/L", new BigDecimal("0.0"),
				new BigDecimal("55.0"), new BigDecimal("0.5"), 256, 10));
		
		//ContainerService.uploadDataset("files/testdata.json");
		//int noOfDevices = 5;
		//LocalDate from = LocalDate.of(2017, 2, 1);
		//LocalDate to = LocalDate.of(2017, 2, 10);
		//DataGenerator.generateDataset("files/generated_dataset.json", noOfDevices, from, to);
		
		DataService.getDataset(2, "files/dataset1.json", 1, "DexG5MobRec_SM64305440", "cbg", "mmol/L", LocalDate.of(2017, 2, 4),LocalDate.of(2017, 2, 4));
		DataService.getDataset(2, "files/dataset2.json", 2, "DexG5MobRec_SM64305440", "cbg", "mmol/L", LocalDate.of(2017, 2, 4),LocalDate.of(2017, 2, 4));
		DataService.getDataset(2, "files/dataset3.json", 3, "DexG5MobRec_SM64305440", "cbg", "mmol/L", LocalDate.of(2017, 2, 4),LocalDate.of(2017, 2, 4));
		//UsageSimulator.performResearcherRequest("files/requested_dataset1.json", 2);

		// single user attacks
		AttackSimulator.performNoAttack("files/dataset1.json");
		AttackSimulator.performDeletionAttack("files/dataset1.json", 2);
		AttackSimulator.performDistortionAttack_rounding("files/dataset1.json", 2);
		AttackSimulator.performDistortionAttack_random("files/dataset1.json", BigDecimal.valueOf(0.01));
		AttackSimulator.performSubsetAttack("files/dataset1.json", BigDecimal.valueOf(0.5));
		
		// collusion attacks
		List<String> datasets4Collusion = new LinkedList<>();
		datasets4Collusion.add("files/dataset1.json");
		datasets4Collusion.add("files/dataset2.json");
		AttackSimulator.performMeanCollusionAttack(datasets4Collusion);
		datasets4Collusion = new LinkedList<>();
		datasets4Collusion.add("files/dataset1.json");
		datasets4Collusion.add("files/dataset3.json");
		AttackSimulator.performMeanCollusionAttack(datasets4Collusion);
	}

}
