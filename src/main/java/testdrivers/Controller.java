package testdrivers;

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

			DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(0.5, 256, 10));

			PatientSimulator.storeDataset();
			DataUserSimulator.requestDataset("requested_dataset_1.json", 1, "DexG5MobRec_SM64305440", "2017-02-04",
					"2017-02-05");
			DataUserSimulator.requestDataset("requested_dataset_2.json", 2, "DexG5MobRec_SM64305440", "2017-02-04",
					"2017-02-04");
			DataDetectiveSimulator.detectLeakage("requested_dataset_1.json", "requested_dataset_1_report.txt", 0.8,
					0.8);
		} else {

			FileService.FOLDER = "C:/temp/files/";

			if (args.length == 0) {
				System.out.println("reset");
				System.out.println("\t-table fragment");
				System.out.println("\t-table request");
				System.out.println("\t-table usability_constraint");
				System.out.println("\t-files [term]");
				System.out.println("\t-log");

				System.out.println("set");
				System.out.println("\t-usability_constraint [maximumError] [numberOfWatermarks] [numberOfRanges]");

				System.out.println("PatientSimulator");
				System.out.println("\t-generate [datasetName] [deviceId] [from] [to]");
				System.out.println("\t-store [datasetName]");
				System.out.println("\t-store");

				System.out.println("DataUserSimulator");
				System.out.println("\t-request -patient [datasetName] [dataUserId] [deviceId] [from] [to]");
				System.out.println("\t-request -patients [datasetName] [dataUserId] [noOfDevices] [from] [to]");
				System.out.println("\t-attack -deletion [datasetName] [newDatasetName] [n]");
				System.out.println("\t-attack -random [datasetName] [newDatasetName] [maxError]");
				System.out.println("\t-attack -rounding [datasetName] [newDatasetName] [decimalDigits]");
				System.out.println("\t-attack -subset [datasetName] [newDatasetName] [startIndex] [endIndex]");
				System.out.println("\t-attack -collusion [datasetName1] ... [datasetNameN] [newDatasetName]");

				System.out.println("DataDetectiveSimulator");
				System.out.println("\t-detect "
						+ "[datasetName] [reportName] [fragmentSimilarityThreshold] [watermarkSimilarityThreshold]");
			}

			else {
				if (args[0].contentEquals("reset")) {
					if (args[1].contentEquals("-table")) {
						DatabaseService.deleteTable(args[2]);
					}
					if (args[1].contentEquals("-files")) {
						FileService.deleteFiles(args[2]);
					}
					if(args[1].contentEquals("-log")) {
						LogService.delete();
					}
				}

				if (args[0].contentEquals("set")) {
					if (args[1].contentEquals("-usability_constraint")) {
						DatabaseService.insertUsabilityConstraint(new UsabilityConstraint(Double.parseDouble(args[2]),
								Integer.parseInt(args[3]), Integer.parseInt(args[4])));
					}
				}

				if (args[0].contentEquals("PatientSimulator")) {
					if (args[1].contentEquals("-generate")) {
						PatientSimulator.generateDataset(args[2], args[3], args[4], args[5]);
					}
					if (args[1].contentEquals("-store")) {
						if (args.length == 2) {
							PatientSimulator.storeDataset();
						} else {
							PatientSimulator.storeDataset(args[2]);
						}
					}
				}

				if (args[0].contentEquals("DataUserSimulator")) {
					if (args[1].contentEquals("-request")) {
						if (args[2].contentEquals("-patient")) {
							DataUserSimulator.requestDataset(args[3], Integer.parseInt(args[4]), args[5], args[6],
									args[7]);
						}
						if (args[2].contentEquals("-patients")) {
							DataUserSimulator.requestDataset(args[3], Integer.parseInt(args[4]),
									Integer.parseInt(args[5]), args[6], args[7]);
						}
					}
					if (args[1].contentEquals("-attack")) {
						if (args[2].contentEquals("-deletion")) {
							DataUserSimulator.attackDatasetByDeletion(args[3], args[4], Integer.parseInt(args[5]));
						}
						if (args[2].contentEquals("-random")) {
							DataUserSimulator.attackDatasetbyRandom(args[3], args[4], Double.parseDouble(args[5]));
						}
						if (args[2].contentEquals("-rounding")) {
							DataUserSimulator.attackDatasetByRounding(args[3], args[4], Integer.parseInt(args[5]));
						}
						if (args[2].contentEquals("-subset")) {
							DataUserSimulator.attackDatasetbySubset(args[3], args[4], Integer.parseInt(args[5]),
									Integer.parseInt(args[6]));
						}
						if (args[2].contentEquals("-collusion")) {
							List<String> datasetNames = new LinkedList<>();
							for (int i = 3; (i + 1) < args.length; i++) {
								datasetNames.add(args[i]);
							}
							DataUserSimulator.attackDatasetByCollusion(datasetNames, args[args.length - 1]);
						}
					}
				}

				if (args[0].contentEquals("DataDetectiveSimulator")) {
					if (args[1].contentEquals("-detect")) {
						DataDetectiveSimulator.detectLeakage(args[2], args[3], Double.parseDouble(args[4]),
								Double.parseDouble(args[5]));
					}
				}

			}
		}
	}
}
