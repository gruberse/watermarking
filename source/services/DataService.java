package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import entities.Fragment;
import entities.Measurement;
import utils.DatabaseService;
import utils.FileService;

public class DataService {

	/**
	 * retrieves requested fragments from database. embeds watermarks into the
	 * requested fragments. provides the dataset (watermarked fragments) to the
	 * folder location.
	 * 
	 * @param folderLocation folder location where the data should be provided to
	 * @param dataUserId     identification of the requesting data user
	 * @param deviceId       the requested fragments' device
	 * @param type           the requested fragments' type
	 * @param unit           the requested fragments' unit
	 * @param from           the beginning of the requested time period
	 * @param to             the end of the requested time period
	 */
	public static void getDataset(String folderLocation, int dataUserId, String deviceId, String type, String unit,
			LocalDate from, LocalDate to) {
		// provides the dataset (watermarked fragments) to the folder
		provideDataset(folderLocation, dataUserId,
				// embeds watermarks into the requested fragments
				watermarkEmbedding(dataUserId,
						// retrieves requested fragments from database
						DatabaseService.getFragments(deviceId, type, unit, from, to)));
	}

	/**
	 * retrieve multiple devices' fragments from database. embeds watermarks into
	 * the requested fragments. provides the dataset (watermarked fragments) to the
	 * folder location.
	 * 
	 * @param folderLocation folder location where the data should be provided to
	 * @param dataUserId     identification of the requesting data user
	 * @param noOfDevices    the requested number of devices
	 * @param type           the requested fragments' type
	 * @param unit           the requested fragments' unit
	 * @param from           the beginning of the requested time period
	 * @param to             the end of the requested time period
	 */
	public static void getDataset(String folderLocation, int dataUserId, int noOfDevices, String type, String unit,
			LocalDate from, LocalDate to) {
		// provides the dataset (watermarked fragments) to the folder
		provideDataset(folderLocation, dataUserId,
				// embeds watermarks into the requested fragments
				watermarkEmbedding(dataUserId,
						// retrieves multiple devices' fragments from database
						DatabaseService.getFragments(noOfDevices, type, unit, from, to)));
	}

	/*
	 * provides dataset to the folder location.
	 */
	private static void provideDataset(String folderLocation, int dataUserId, List<Fragment> fragments) {
		try (FileWriter file = new FileWriter(
				FileService.getFileName(folderLocation, "dataUser" + dataUserId, "request"))) {
			String json = "[";
			for (int i = 0; i < fragments.size(); i++) {
				json = json + fragments.get(i).getMeasurementsAsJsonString();
				if (i + 1 < fragments.size())
					json = json + ",";
			}
			json = json + "\n]";
			file.write(json);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/*
	 * embeds watermarks into fragments. no consideration of noOfWatermarks, actual
	 * noOfWatermark and usability constraints yet.
	 */
	private static List<Fragment> watermarkEmbedding(int dataUserId, List<Fragment> originalFragments) {
		LocalDateTime timestamp = LocalDateTime.now();
		List<Fragment> watermarkedFragments = new LinkedList<>();

		Collections.sort(originalFragments);
		for (Fragment originalFragment : originalFragments) {
			Fragment watermarkedFragment = new Fragment();
			watermarkedFragment.setDeviceId(originalFragment.getDeviceId());
			watermarkedFragment.setType(originalFragment.getType());
			watermarkedFragment.setUnit(watermarkedFragment.getUnit());
			watermarkedFragment.setDate(originalFragment.getDate());
			watermarkedFragment.setMeasurements(new LinkedList<>());

			// retrieve watermark from database if fragment was already requested by data
			// user
			Double[] watermark = DatabaseService.getWatermark(dataUserId, originalFragment.getDeviceId(),
					originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());

			// watermark generation
			if (watermark == null) {
				// generate new watermark
				watermark = new Double[originalFragment.getMeasurements().size()];
				Random prng = new Random(Long.parseLong(dataUserId + "" + originalFragment.getSecretKey()));
				for (int i = 0; i < watermark.length; i++) {
					watermark[i] = prng.nextDouble() - 0.5;
				}

				// insert new request into the database
				DatabaseService.insertRequest(dataUserId, timestamp, watermark, originalFragment.getDeviceId(),
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			} else {
				// update request in the database and use the same watermark
				DatabaseService.updateRequest(dataUserId, timestamp, originalFragment.getDeviceId(),
						originalFragment.getType(), originalFragment.getUnit(), originalFragment.getDate());
			}

			// watermark embedding
			Collections.sort(originalFragment.getMeasurements());
			for (int i = 0; i < originalFragment.getMeasurements().size(); i++) {
				Measurement measurement = originalFragment.getMeasurements().get(i);
				measurement.setValue(measurement.getValue().doubleValue() + watermark[i]);
				watermarkedFragment.getMeasurements().add(measurement);
			}
			watermarkedFragments.add(watermarkedFragment);
		}
		return watermarkedFragments;
	}
}
