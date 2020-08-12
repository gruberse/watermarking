package ui;

import java.util.LinkedList;
import java.util.List;

import entities.UsabilityConstraint;
import simulators.DataDetectiveSimulator;
import simulators.DataUserSimulator;
import simulators.PatientSimulator;
import utilities.DatabaseService;
import utilities.FileService;
import utilities.LogService;

public class ConsoleUI {
	public static void main(String[] args) {

		FileService.FOLDER = "C:/temp/files/";

		if (args.length == 0) {
			System.out.println("Commands");
			System.out.println("-reset -table -fragment");
			System.out.println("-reset -table -request");
			System.out.println("-reset -table -usability_constraint");
			System.out.println("-reset -files");
			System.out.println("-reset -log");
			System.out.println("");
			System.out.println("-set -usability_constraint [maximumError] [numberOfRanges]");
			System.out.println("");
			System.out.println("-generate [deviceId] [from] [to] [seed]");
			System.out.println("");
			System.out.println("-store -random [datasetName]");
			System.out.println("-store -one [datasetName]");
			System.out.println("");
			System.out.println("-request -patient [dataUserId] [deviceId] [from] [to]");
			System.out.println("-request -patients [dataUserId] [numberOfDevices] [from] [to]");
			System.out.println("");
			System.out.println("-attack -deletion [datasetName] [frequency]");
			System.out.println("-attack -random [datasetName] [maximumError] [seed]");
			System.out.println("-attack -rounding [datasetName] [decimalDigit]");
			System.out.println("-attack -subset [datasetName] [startIndex] [endIndex]");
			System.out.println("-attack -collusion [datasetName1] ... [datasetNameN]");
			System.out.println("");
			System.out.println(
					"-detect [datasetName] [fragmentSimilarityThreshold] [watermarkSimilarityThreshold] [numberOfColluders]");
			System.out.println("");
			System.out.println("Configurable Parameters");
			System.out.println("[dataUserId]:\t\t\tdata user identifier as integer");
			System.out.println("[datasetName]:\t\t\tdataset name as string");
			System.out.println("[decimalDigit]:\t\t\tdecimal digit as integer");
			System.out.println("[deviceId]:\t\t\tdevice identifier as string");
			System.out.println("[endIndex]:\t\t\tend index as integer");
			System.out.println("[fragmentSimilarityThreshold]:\tfragment similarity threshold as double");
			System.out.println("[frequency]:\t\t\tfrequency as integer");
			System.out.println("[from]:\t\t\t\tstart date as string with the format [yyyy-MM-dd]");
			System.out.println("[maximumError]:\t\t\tmaximum error as double");
			System.out.println("[numberOfColluders]:\t\tnumber of colluders as integer");
			System.out.println("[numberOfDevices]:\t\tnumber of devices as integer");
			System.out.println("[numberOfRanges]:\t\tnumber of ranges as integer");
			System.out.println("[seed]:\t\t\t\tseed as integer");
			System.out.println("[startIndex]:\t\t\tstart index as integer");
			System.out.println("[to]:\t\t\t\tend date as string with the format [yyyy-MM-dd]");
			System.out.println("[watermarkSimilarityThreshold]:\twatermark similarity threshold as double");
		}

		else {
			if (args[0].contentEquals("-reset")) {
				if (args[1].contentEquals("-table")) {
					if (args[2].contentEquals("-fragment")) {
						DatabaseService.deleteTable("fragment");
					}
					if (args[2].contentEquals("-usability_constraint")) {
						DatabaseService.deleteTable("usability_constraint");
					}
					if (args[2].contentEquals("-request")) {
						DatabaseService.deleteTable("request");
					}
				}
				if (args[1].contentEquals("-files")) {
					FileService.deleteFiles();
				}
				if (args[1].contentEquals("-log")) {
					LogService.delete();
				}
			}
			if (args[0].contentEquals("-set")) {
				if (args[1].contentEquals("-usability_constraint")) {
					DatabaseService.insertUsabilityConstraint(
							new UsabilityConstraint(Double.parseDouble(args[2]), Integer.parseInt(args[3])));
				}
			}
			if (args[0].contentEquals("-generate")) {
				PatientSimulator.generateDataset(args[1], args[2], args[3], Long.parseLong(args[4]));
			}
			if (args[0].contentEquals("-store")) {
				if (args[1].contentEquals("-one")) {
					PatientSimulator.storeDataset(false, args[2]);
				}
				if (args[1].contentEquals("-random")) {
					PatientSimulator.storeDataset(true, args[2]);
				}
			}
			if (args[0].contentEquals("-request")) {
				if (args[1].contentEquals("-patient")) {
					DataUserSimulator.requestDataset(Integer.parseInt(args[2]), args[3], args[4], args[5]);
				}
				if (args[1].contentEquals("-patients")) {
					DataUserSimulator.requestDataset(Integer.parseInt(args[2]), Integer.parseInt(args[3]), args[4],
							args[5]);
				}
			}
			if (args[0].contentEquals("-attack")) {
				if (args[1].contentEquals("-deletion")) {
					DataUserSimulator.attackDatasetByDeletion(args[2], Integer.parseInt(args[3]));
				}
				if (args[1].contentEquals("-random")) {
					DataUserSimulator.attackDatasetbyRandom(args[2], Double.parseDouble(args[3]),
							Long.parseLong(args[4]));
				}
				if (args[1].contentEquals("-rounding")) {
					DataUserSimulator.attackDatasetByRounding(args[2], Integer.parseInt(args[3]));
				}
				if (args[1].contentEquals("-subset")) {
					DataUserSimulator.attackDatasetbySubset(args[2], Integer.parseInt(args[3]),
							Integer.parseInt(args[4]));
				}
				if (args[1].contentEquals("-collusion")) {
					List<String> datasetNames = new LinkedList<>();
					for (int i = 2; i < args.length; i++) {
						datasetNames.add(args[i]);
					}
					DataUserSimulator.attackDatasetByCollusion(datasetNames);
				}
			}
			if (args[0].contentEquals("-detect")) {
				DataDetectiveSimulator.detectLeakage(args[1], Double.parseDouble(args[2]), Double.parseDouble(args[3]),
						Integer.parseInt(args[4]));
			}
		}
	}
}
