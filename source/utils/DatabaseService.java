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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import entities.Fragment;
import entities.Measurement;
import utils.FragmentationService.From;

public class DatabaseService {

	public static void updateRequest(int dataUserId, String deviceId, LocalDateTime timestamp, String type, String unit,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

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

	public static void insertRequest(int dataUserId, String deviceId, LocalDateTime timestamp, String type, String unit,
			LocalDate date, Double[] watermark) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO requests (\"dataUserId\", \"deviceId\", timestamps, type, unit, date, watermark) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			Timestamp[] timestamps = new Timestamp[1];
			timestamps[0] = Timestamp.valueOf(timestamp);

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

	public static Double[] getWatermark(String deviceId, String type, String unit, LocalDate date, int datUserId) {
		Double[] watermark = null;

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT watermark FROM requests WHERE \"deviceId\" = ? AND \"dataUserId\" = ? AND type = ? AND unit = ? "
					+ "AND date = ?";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			preparedStatement.setString(1, deviceId);
			preparedStatement.setInt(2, datUserId);
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

	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> list = new LinkedList<>();
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
				List<Measurement> measurements = FragmentationService.getMeasurementsFromJson(From.String,
						resultSet.getString("measurements"));
				Collections.sort(measurements);
				fragment.setMeasurements(measurements);
				fragment.setSecretKey(resultSet.getInt("secretKey"));
				list.add(fragment);
			}
			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public static void uploadFragments(List<Fragment> fragments) {
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
				Collections.sort(fragment.getMeasurements());
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

	public enum Tables {
		fragments, requests, ucs
	}

	public static void cleanupTable(Tables table) {
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
