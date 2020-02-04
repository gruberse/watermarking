package testdrivers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import entities.UsabilityConstraint;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.PatientSimulator;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.LogService;

public class Controller {

	public static void main(String[] args) {

		if (Controller.class.getResource("Controller.class").toString().contains("jar") == false) {
			DatabaseService.deleteTable("fragment");
			DatabaseService.deleteTable("request");
			DatabaseService.deleteTable("usability_constraint");
			FileService.deleteFiles("dataset");
			LogService.delete();

			DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(0.5, 256, 10));

			PatientSimulator.storeDataset();

			DataUserSimulator.requestDataset("requested_dataset.json", 1, "DexG5MobRec_SM64305440", "2017-02-04",
					"2017-02-04");
			DataUserSimulator.requestDataset("requested_dataset.json", 2, "DexG5MobRec_SM64305440", "2017-02-04",
					"2017-02-04");
			DataDetectiveSimulator.detectLeakage("requested_dataset.json", "requested_dataset_report.txt", 0.8, 0.8);
		} else {

			FileService.FOLDER = "C:/temp/files/";

			if (args.length == 0) {
				System.out.println("-reset -table fragment");
				System.out.println("-reset -table request");
				System.out.println("-reset -table usability_constraint");
				System.out.println("-reset -files [term]");
				System.out.println("-reset -log");
				System.out.println("");
				System.out.println("-log -section");
				System.out.println("-log -message [message]");
				System.out.println("");
				System.out.println("-set -usability_constraint [maximumError] [numberOfWatermarks] [numberOfRanges]");
				System.out.println("");
				System.out.println("---PatientSimulator---");
				System.out.println("-generate [datasetName] [deviceId] [from] [to]");
				System.out.println("-store [datasetName]");
				System.out.println("-store");
				System.out.println("");
				System.out.println("---DataUserSimulator---");
				System.out.println("-request -patient [datasetName] [dataUserId] [deviceId] [from] [to]");
				System.out.println("-request -patients [datasetName] [dataUserId] [noOfDevices] [from] [to]");
				System.out.println("-attack -deletion [datasetName] [newDatasetName] [frequency]");
				System.out.println("-attack -random [datasetName] [newDatasetName] [maxError] [seed]");
				System.out.println("-attack -rounding [datasetName] [newDatasetName] [decimalDigit]");
				System.out.println("-attack -subset [datasetName] [newDatasetName] [startIndex] [endIndex]");
				System.out.println("-attack -collusion [datasetName1] ... [datasetNameN] [newDatasetName]");
				System.out.println("");
				System.out.println("---DataDetectiveSimulator---");
				System.out.println(
						"-detect [datasetName] [reportName] [fragmentSimilarityThreshold] [watermarkSimilarityThreshold]");
			}

			else {
				if (args[0].contentEquals("-reset")) {
					if (args[1].contentEquals("-table")) {
						DatabaseService.deleteTable(args[2]);
					}
					if (args[1].contentEquals("-files")) {
						FileService.deleteFiles(args[2]);
					}
					if (args[1].contentEquals("-log")) {
						LogService.delete();
					}
				}
				if (args[0].contentEquals("-log")) {
					if (args[1].contentEquals("-section")) {
						LogService.log("----------------------------------------------------");
					}
					if (args[1].contentEquals("-message")) {
						String message = "";
						for (int i = 2; i < args.length; i++) {
							message = message + args[i];
							if (i + 1 < args.length) {
								message = message + " ";
							}
						}
						LogService.log(message);
					}
				}
				if (args[0].contentEquals("-set")) {
					if (args[1].contentEquals("-usability_constraint")) {
						DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(Double.parseDouble(args[2]),
								Integer.parseInt(args[3]), Integer.parseInt(args[4])));
					}
				}
				if (args[0].contentEquals("-generate")) {
					PatientSimulator.generateDataset(args[1], args[2], args[3], args[4]);
				}
				if (args[0].contentEquals("-store")) {
					if (args.length == 1) {
						PatientSimulator.storeDataset();
					} else {
						PatientSimulator.storeDataset(args[1]);
					}
				}
				if (args[0].contentEquals("-request")) {
					if (args[1].contentEquals("-patient")) {
						DataUserSimulator.requestDataset(args[2], Integer.parseInt(args[3]), args[4], args[5], args[6]);
					}
					if (args[1].contentEquals("-patients")) {
						DataUserSimulator.requestDataset(args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]),
								args[5], args[6]);
					}
				}
				if (args[0].contentEquals("-attack")) {
					if (args[1].contentEquals("-deletion")) {
						DataUserSimulator.attackDatasetByDeletion(args[2], args[3], Integer.parseInt(args[4]));
					}
					if (args[1].contentEquals("-random")) {
						DataUserSimulator.attackDatasetbyRandom(args[2], args[3], Double.parseDouble(args[4]), Long.parseLong(args[5]));
					}
					if (args[1].contentEquals("-rounding")) {
						DataUserSimulator.attackDatasetByRounding(args[2], args[3], Integer.parseInt(args[4]));
					}
					if (args[1].contentEquals("-subset")) {
						DataUserSimulator.attackDatasetbySubset(args[2], args[3], Integer.parseInt(args[4]),
								Integer.parseInt(args[5]));
					}
					if (args[1].contentEquals("-collusion")) {
						List<String> datasetNames = new LinkedList<>();
						for (int i = 2; (i + 1) < args.length; i++) {
							datasetNames.add(args[i]);
						}
						DataUserSimulator.attackDatasetByCollusion(datasetNames, args[args.length - 1]);
					}
				}
				if (args[0].contentEquals("-detect")) {
					DataDetectiveSimulator.detectLeakage(args[1], args[2], Double.parseDouble(args[3]),
							Double.parseDouble(args[4]));
				}

			}
		}
	}
}
