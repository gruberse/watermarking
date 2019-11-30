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

import entities.Fragment;
import utils.FragmentationService.Source;

public class DatabaseService {

	/**
	 * updates existing request.
	 * 
	 * @param dataUserId identification of the requesting data user
	 * @param timestamp  the request's new timestamp
	 * @param deviceId   the requested fragment's device
	 * @param type       the requested fragment's type
	 * @param unit       the requested fragment's unit
	 * @param date       the requested fragment's date
	 */
	public static void updateRequest(int dataUserId, LocalDateTime timestamp, String deviceId, String type, String unit,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			// get the request's timestamps
			ArrayList<Timestamp> timestamps = new ArrayList<>();
			String sql = "SELECT timestamps FROM requests "
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

			// adds the new timestamp
			timestamps.add(Timestamp.valueOf(timestamp));
			sql = "UPDATE requests SET timestamps = ? "
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

	/**
	 * inserts new request.
	 * 
	 * @param dataUserId identification of the requesting data user
	 * @param timestamp  the request's new timestamp
	 * @param watermark  the generated watermark
	 * @param deviceId   the requested fragment's device
	 * @param type       the requested fragment's type
	 * @param unit       the requested fragment's unit
	 * @param date       the requested fragment's date
	 */
	public static void insertRequest(int dataUserId, LocalDateTime timestamp, Double[] watermark, String deviceId,
			String type, String unit, LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO requests (\"dataUserId\", \"deviceId\", timestamps, type, unit, date, watermark) "
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
			preparedStatement.setArray(7, connection.createArrayOf("float8", watermark));
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * get a request's watermark.
	 * 
	 * @param watermark the embedded watermark
	 * @param deviceId  the fragment's device
	 * @param type      the fragment's type
	 * @param unit      the fragment's unit
	 * @param date      the fragment's date
	 * @return dataUserId
	 */
	public static int getDataUserId(Double[] watermark, String deviceId, String type, String unit, LocalDate date) {
		int dataUserId = 0;

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"dataUserId\" FROM requests WHERE watermark = ? AND \"deviceId\" = ? AND type = ? AND unit = ? "
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

	/**
	 * get a request's watermark.
	 * 
	 * @param dataUserId identification of the requesting data user
	 * @param deviceId   the requested fragment's device
	 * @param type       the requested fragment's type
	 * @param unit       the requested fragment's unit
	 * @param date       the requested fragment's date
	 * @return watermark
	 */
	public static Double[] getWatermark(int dataUserId, String deviceId, String type, String unit, LocalDate date) {
		Double[] watermark = null;

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT watermark FROM requests WHERE \"deviceId\" = ? AND \"dataUserId\" = ? AND type = ? AND unit = ? "
					+ "AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setInt(2, dataUserId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setDate(5, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				watermark = (Double[]) resultSet.getArray("watermark").getArray();
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return watermark;
	}

	/**
	 * get a fragment's watermarks.
	 * 
	 * @param deviceId the requested fragment's device
	 * @param type     the requested fragment's type
	 * @param unit     the requested fragment's unit
	 * @param date     the requested fragment's date
	 * @return list of watermarks
	 */
	public static List<Double[]> getWatermarks(String deviceId, String type, String unit, LocalDate date) {
		List<Double[]> watermarks = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT watermark FROM requests WHERE \"deviceId\" = ? AND type = ? AND unit = ? "
					+ "AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				watermarks.add((Double[]) resultSet.getArray("watermark").getArray());
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return watermarks;
	}

	/**
	 * get requested fragments.
	 * 
	 * @param deviceId the requested fragments' device
	 * @param type     the requested fragments' type
	 * @param unit     the requested fragments' unit
	 * @param from     the beginning of the requested time period
	 * @param to       the end of the requested time period
	 * @return list of fragments
	 */
	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"deviceId\", type, unit, date, measurements, \"secretKey\" FROM fragments "
					+ "WHERE \"deviceId\" = ? AND type = ? AND unit = ? " + "AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(from));
			preparedStatement.setDate(5, Date.valueOf(to));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment();
				fragment.setDeviceId(resultSet.getString("deviceId"));
				fragment.setType(resultSet.getString("type"));
				fragment.setUnit(resultSet.getString("unit"));
				fragment.setDate(LocalDate.parse(resultSet.getString("date")));
				fragment.setMeasurements(
						// converting from json string to list of measurements
						FragmentationService.getMeasurements(Source.String, resultSet.getString("measurements")));
				fragment.setSecretKey(resultSet.getInt("secretKey"));
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

	/**
	 * get requested fragments.
	 * 
	 * @param deviceId the fragments' device
	 * @param type     the fragments' type
	 * @param unit     the fragments' unit
	 * @param date     the fragment's date
	 * @return list of fragments
	 */
	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate date) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"deviceId\", type, unit, date, measurements, \"secretKey\" FROM fragments "
					+ "WHERE \"deviceId\" = ? AND type = ? AND unit = ? " + "AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment();
				fragment.setDeviceId(resultSet.getString("deviceId"));
				fragment.setType(resultSet.getString("type"));
				fragment.setUnit(resultSet.getString("unit"));
				fragment.setDate(LocalDate.parse(resultSet.getString("date")));
				fragment.setMeasurements(
						// converting from json string to list of measurements
						FragmentationService.getMeasurements(Source.String, resultSet.getString("measurements")));
				fragment.setSecretKey(resultSet.getInt("secretKey"));
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

	/**
	 * get requested fragments.
	 * 
	 * @param noOfDevices the requested number of devices
	 * @param type        the requested fragments' type
	 * @param unit        the requested fragments' unit
	 * @param from        the beginning of the requested time period
	 * @param to          the end of the requested time period
	 * @return list of fragments
	 */
	public static List<Fragment> getFragments(int noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();
		List<String> deviceIds = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			// get all deviceIds
			String sql = "SELECT distinct \"deviceId\" FROM fragments "
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

	/**
	 * inserts list of fragments.
	 * 
	 * @param fragments list of fragments
	 */
	public static void insertFragments(List<Fragment> fragments) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO fragments (\"deviceId\", type, unit, date, measurements, \"secretKey\") VALUES "
					+ "(?, ?, ?, ?, ?::JSON, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (Fragment fragment : fragments) {
				preparedStatement.setString(1, fragment.getDeviceId());
				preparedStatement.setString(2, fragment.getType());
				preparedStatement.setString(3, fragment.getUnit());
				preparedStatement.setDate(4, Date.valueOf(fragment.getDate()));
				preparedStatement.setObject(5, fragment.getMeasurementsAsJsonArrayString());
				preparedStatement.setInt(6, fragment.getSecretKey());
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * the database tables
	 */
	public enum Tables {
		fragments, requests, ucs
	}

	/**
	 * deletes the content from a table
	 * 
	 * @param table the database table
	 */
	public static void deleteTable(Tables table) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "DELETE FROM ";
			if (table == Tables.fragments) {
				sql = sql + "fragments";
			} else if (table == Tables.requests) {
				sql = sql + "requests";
			} else if (table == Tables.ucs) {
				sql = sql + "ucs";
			} else {
				return;
			}

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
