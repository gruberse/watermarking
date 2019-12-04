package utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import entities.DataProfile;
import entities.Fragment;
import entities.UsabilityConstraint;

public class DatabaseService {

	public static void deleteTable(String table) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "DELETE FROM " + table;

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void insertUsabilityConstraint(UsabilityConstraint usabilityConstraint) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO usability_constraint (type, unit, frequency, minimum_value, maximum_value, maximum_error, number_of_ranges) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, usabilityConstraint.getType());
			preparedStatement.setString(2, usabilityConstraint.getUnit());
			preparedStatement.setInt(3, usabilityConstraint.getFrequency());
			preparedStatement.setBigDecimal(4, usabilityConstraint.getMinimumValue());
			preparedStatement.setBigDecimal(5, usabilityConstraint.getMaximumValue());
			preparedStatement.setBigDecimal(6, usabilityConstraint.getMaximumError());
			preparedStatement.setInt(7, usabilityConstraint.getNumberOfRanges());
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static UsabilityConstraint getUsabilityConstraint(String type, String unit, int frequency) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT * FROM usability_constraint WHERE type = ? AND unit = ? AND frequency  = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, unit);
			preparedStatement.setInt(3, frequency);

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return new UsabilityConstraint(type, unit, frequency, resultSet.getBigDecimal("minimum_value"),
						resultSet.getBigDecimal("maximum_value"), resultSet.getBigDecimal("maximum_error"),
						resultSet.getInt("number_of_ranges"));
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static void insertFragments(List<Fragment> fragments) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO fragment (device_id, type, unit, frequency, date, measurements, secret_key, mean, dataset_id) "
					+ "VALUES (?, ?, ?, ?, ?, ?::JSON, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (Fragment fragment : fragments) {
				preparedStatement.setString(1, fragment.getDeviceId());
				preparedStatement.setString(2, fragment.getType());
				preparedStatement.setString(3, fragment.getUnit());
				preparedStatement.setInt(4, fragment.getFrequency());
				preparedStatement.setDate(5, Date.valueOf(fragment.getDate()));
				preparedStatement.setObject(6, fragment.getMeasurementsAsJsonArrayString());
				preparedStatement.setLong(7, fragment.getSecretKey());
				preparedStatement.setBigDecimal(8, fragment.getMean());
				preparedStatement.setString(9, fragment.getDatasetId());
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void insertProfiles(List<DataProfile> dataProfiles) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO data_profile (dataset_id, device_id, type, unit, frequency, "
					+ "relative_value_distribution, relative_slope_distribution, relative_curvature_distribution) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (DataProfile dataProfile : dataProfiles) {
				preparedStatement.setString(1, dataProfile.getDatasetId());
				preparedStatement.setString(2, dataProfile.getDeviceId());
				preparedStatement.setString(3, dataProfile.getType());
				preparedStatement.setString(4, dataProfile.getUnit());
				preparedStatement.setInt(5, dataProfile.getFrequency());
				preparedStatement.setArray(6, connection.createArrayOf("text", dataProfile.getRelativeValueDistributionAsStringArray()));
				preparedStatement.setArray(7, connection.createArrayOf("text", dataProfile.getRelativeSlopeDistributionAsStringArray()));
				preparedStatement.setArray(8, connection.createArrayOf("text", dataProfile.getRelativeCurvatureDistributionAsStringArray()));
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// TODO
	public static void updateRequest(int dataUserId, LocalDateTime timestamp, String deviceId, String type, String unit,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			// get the request's timestamps
			ArrayList<Timestamp> timestamps = new ArrayList<>();
			String sql = "SELECT timestamps FROM request "
					+ "WHERE \"dataUserId\" = ? AND \"deviceId\" = ? AND type = ? AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, dataUserId);
			preparedStatement.setString(2, deviceId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setDate(5, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				timestamps = new ArrayList<Timestamp>(
						Arrays.asList((Timestamp[]) resultSet.getArray("timestamps").getArray()));
			}

			// add new timestamp
			timestamps.add(Timestamp.valueOf(timestamp));
			sql = "UPDATE request SET timestamps = ? "
					+ "WHERE \"dataUserId\" = ? AND \"deviceId\" = ? AND type = ? AND unit = ? AND date = ?";

			preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setArray(1, connection.createArrayOf("timestamp", timestamps.toArray()));
			preparedStatement.setInt(2, dataUserId);
			preparedStatement.setString(3, deviceId);
			preparedStatement.setString(4, type);
			preparedStatement.setString(5, unit);
			preparedStatement.setDate(6, Date.valueOf(date));
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// TODO
	public static void insertRequest(int dataUserId, LocalDateTime timestamp, Integer noOfWatermark, String deviceId,
			String type, String unit, LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO request (\"dataUserId\", \"deviceId\", timestamps, type, unit, date, \"noOfWatermark\") "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			Timestamp[] timestamps = new Timestamp[1];
			timestamps[0] = Timestamp.valueOf(timestamp);

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setInt(1, dataUserId);
			preparedStatement.setString(2, deviceId);
			preparedStatement.setArray(3, connection.createArrayOf("timestamp", timestamps));
			preparedStatement.setString(4, type);
			preparedStatement.setString(5, unit);
			preparedStatement.setDate(6, Date.valueOf(date));
			preparedStatement.setInt(7, noOfWatermark);
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	// TODO
	public static int getDataUserId(Double[] watermark, String deviceId, String type, String unit, LocalDate date) {
		int dataUserId = 0;

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"dataUserId\" FROM request WHERE watermark = ? AND \"deviceId\" = ? AND type = ? AND unit = ? "
					+ "AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setArray(1, connection.createArrayOf("float8", watermark));
			preparedStatement.setString(2, deviceId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setDate(5, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				dataUserId = resultSet.getInt("dataUserId");
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return dataUserId;
	}

	// TODO
	public static Integer getWatermark(int dataUserId, String deviceId, String type, String unit, LocalDate date) {
		Integer noOfWatermark = null;

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"noOfWatermark\" FROM request WHERE \"deviceId\" = ? AND \"dataUserId\" = ? AND type = ? "
					+ "AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setInt(2, dataUserId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setDate(5, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				noOfWatermark = resultSet.getInt("noOfWatermark");
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return noOfWatermark;
	}

	// TODO
	public static List<Integer> getWatermarks(String deviceId, String type, String unit, LocalDate date) {
		List<Integer> noOfWatermarks = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"noOfWatermark\" FROM request WHERE \"deviceId\" = ? AND type = ? AND unit = ? "
					+ "AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				noOfWatermarks.add(resultSet.getInt("watermark"));
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return noOfWatermarks;
	}

	// TODO
	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT device_id, type, unit, date, measurements, secret_key, mean, dataset_id FROM fragment "
					+ "WHERE device_id = ? AND type = ? AND unit = ? " + "AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(from));
			preparedStatement.setDate(5, Date.valueOf(to));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment();
				fragment.setDeviceId(resultSet.getString("device_id"));
				fragment.setType(resultSet.getString("type"));
				fragment.setUnit(resultSet.getString("unit"));
				fragment.setDate(LocalDate.parse(resultSet.getString("date")));
				fragment.setMeasurements(
						// converting from json string to list of measurements
						FragmentationService.getMeasurements("string", resultSet.getString("measurements")));
				fragment.setSecretKey(resultSet.getInt("secret_key"));
				fragment.setDatasetId("dataset_id");
				fragments.add(fragment);
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return fragments;
	}

	// TODO
	public static List<Fragment> getFragments(int noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();
		List<String> deviceIds = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			// get all deviceIds
			String sql = "SELECT distinct \"deviceId\" FROM fragment "
					+ "WHERE type = ? AND unit = ? AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, unit);
			preparedStatement.setDate(3, Date.valueOf(from));
			preparedStatement.setDate(4, Date.valueOf(to));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				deviceIds.add(resultSet.getString("deviceId"));
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// in case less devices then requested are available
		if (noOfDevices > deviceIds.size()) {
			noOfDevices = deviceIds.size();
		}

		// get fragments for each requested device
		for (int i = 0; i < noOfDevices; i++) {
			fragments.addAll(getFragments(deviceIds.get(i), type, unit, from, to));
		}

		return fragments;
	}

}