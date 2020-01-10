package testdrivers;

import java.util.LinkedList;
import java.util.List;

import entities.UsabilityConstraint;
import generators.AttackGenerator;
import generators.DataGenerator;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.PatientSimulator;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.LogService;

public class Controller {

	public static void main(String[] args) {

		if (args.length > 0) {

			FileService.FOLDER = "C:/temp/files/";
			LogService.FOLDER = "C:/temp/files/";

			if (args[0].contentEquals("help")) {
				System.out.println("reset");
				System.out.println("\t-table fragment");
				System.out.println("\t-table request");
				System.out.println("\t-table usability_constraint");
				System.out.println("\t-files [term]");

				System.out.println("set");
				System.out.println("\t-usability_constraint [maximumError] [numberOfWatermarks] [numberOfRanges]");

				System.out.println("log");
				System.out.println("\t-start");
				System.out.println("\t-stop");

				System.out.println("DataGenerator");
				System.out.println("\t[datasetName] [deviceId] [from] [to]");

				System.out.println("AttackGenerator");
				System.out.println("\t-deletion [datasetName] [newDatasetName] [n]");
				System.out.println("\t-random [datasetName] [newDatasetName] [maxError]");
				System.out.println("\t-rounding [datasetName] [newDatasetName] [decimalDigits]");
				System.out.println("\t-subset [datasetName] [newDatasetName] [startIndex] [endIndex]");
				System.out.println("\t-collusion [datasetName1] ... [datasetNameN] [newDatasetName]");

				System.out.println("PatientSimulator");
				System.out.println("\t[]");
				System.out.println("\t[datasetName]");

				System.out.println("DataUserSimulator");
				System.out.println("\t-p [datasetName] [dataUserId] [deviceId] [from] [to]");
				System.out.println("\t-r [datasetName] [dataUserId] [noOfDevices] [from] [to]");

				System.out.println("DataDetectiveSimulator");
				System.out.println(
						"\t[datasetName] [reportName] [fragmentSimilarityThreshold] [watermarkSimilarityThreshold]");
			}

			if (args[0].contentEquals("reset")) {
				if (args[1].contentEquals("-table")) {
					DatabaseService.deleteTable(args[2]);
				}
				if (args[1].contentEquals("-files")) {
					FileService.deleteFiles(args[2]);
				}
			}

			if (args[0].contentEquals("set")) {
				if (args[1].contentEquals("-usability_constraint")) {
					DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(Double.parseDouble(args[2]),
							Integer.parseInt(args[3]), Integer.parseInt(args[4])));
				}
			}

			if (args[0].contentEquals("log")) {
				if (args[1].contentEquals("-start")) {
					LogService.startLogging();
				}
				if (args[1].contentEquals("-stop")) {
					LogService.stopLogging();
				}
			}

			if (args[0].contentEquals("DataDetectiveSimulator")) {
				DataDetectiveSimulator.detectLeakage(args[1], args[2], Double.parseDouble(args[3]),
						Double.parseDouble(args[4]));
			}

			if (args[0].contentEquals("DataUserSimulator")) {
				if (args[1].contentEquals("-p")) {
					DataUserSimulator.requestDataset(args[2], Integer.parseInt(args[3]), args[4], args[5], args[6]);
				}
				if (args[1].contentEquals("-r")) {
					DataUserSimulator.requestDataset(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]),
							args[5], args[6]);
				}
			}

			if (args[0].contentEquals("PatientSimulator")) {
				if (args.length == 1) {
					PatientSimulator.storeDataset();
				} else {
					PatientSimulator.storeDataset(args[1]);
				}
			}

			if (args[0].contentEquals("AttackGenerator")) {
				if (args[1].contentEquals("-deletion")) {
					AttackGenerator.performDeletionAttack(args[2], args[3], Integer.parseInt(args[4]));
				}
				if (args[1].contentEquals("-random")) {
					AttackGenerator.performRandomDistortionAttack(args[2], args[3], Double.parseDouble(args[4]));
				}
				if (args[1].contentEquals("-rounding")) {
					AttackGenerator.performRoundingDistortionAttack(args[2], args[3], Integer.parseInt(args[4]));
				}
				if (args[1].contentEquals("-subset")) {
					AttackGenerator.performSubsetAttack(args[2], args[3], Integer.parseInt(args[4]),
							Integer.parseInt(args[5]));
				}
				if (args[1].contentEquals("-collusion")) {
					List<String> datasetNames = new LinkedList<>();
					for (int i = 2; (i + 1) < args.length; i++) {
						datasetNames.add(args[i]);
					}
					AttackGenerator.performMeanCollusionAttack(datasetNames, args[args.length - 1]);
				}
			}

			if (args[0].contentEquals("DataGenerator")) {
				DataGenerator.generateDataset(args[1], args[2], args[3], args[4]);
			}
		}

		else {
			DatabaseService.deleteTable("fragment");
			DatabaseService.deleteTable("request");
			DatabaseService.deleteTable("usability_constraint");
			FileService.deleteFiles("dataset");

			DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(0.5, 256, 10));

			LogService.startLogging();

			/*
			PatientSimulator.storeDataset();
			DataUserSimulator.requestDataset("requested_dataset_1.json", 1, "DexG5MobRec_SM64305440", "2017-02-04",
					"2017-02-04");
			DataDetectiveSimulator.detectLeakage("requested_dataset_1.json", "requested_dataset_1_report.txt", 0.8,
					0.8);
					*/
			DataGenerator.generateDataset("generated_dataset.json", "Device_1", "2019-01-01", "2019-01-30");

			LogService.stopLogging();
		}
	}
}
