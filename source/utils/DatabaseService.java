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
import java.util.LinkedList;
import java.util.List;

import entities.Fragment;
import entities.Request;
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

			String sql = "INSERT INTO usability_constraint (type, unit, minimum_value, maximum_value, maximum_error, number_of_watermarks, number_of_ranges) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, usabilityConstraint.getType());
			preparedStatement.setString(2, usabilityConstraint.getUnit());
			preparedStatement.setBigDecimal(3, usabilityConstraint.getMinimumValue());
			preparedStatement.setBigDecimal(4, usabilityConstraint.getMaximumValue());
			preparedStatement.setBigDecimal(5, usabilityConstraint.getMaximumError());
			preparedStatement.setInt(6, usabilityConstraint.getNumberOfWatermarks());
			preparedStatement.setInt(7, usabilityConstraint.getNumberOfRanges());
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static UsabilityConstraint getUsabilityConstraint(String type, String unit) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT * FROM usability_constraint WHERE type = ? AND unit = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, unit);

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return new UsabilityConstraint(type, unit, resultSet.getBigDecimal("minimum_value"),
						resultSet.getBigDecimal("maximum_value"), resultSet.getBigDecimal("maximum_error"),
						resultSet.getInt("number_of_watermarks"), resultSet.getInt("number_of_ranges"));
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

			String sql = "INSERT INTO fragment (device_id, type, unit, date, measurements, secret_key) "
					+ "VALUES (?, ?, ?, ?, ?::JSON, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (Fragment fragment : fragments) {
				preparedStatement.setString(1, fragment.getDeviceId());
				preparedStatement.setString(2, fragment.getType());
				preparedStatement.setString(3, fragment.getUnit());
				preparedStatement.setDate(4, Date.valueOf(fragment.getDate()));
				preparedStatement.setObject(5, fragment.getMeasurementsAsJsonArrayString());
				preparedStatement.setLong(6, fragment.getSecretKey());
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static Fragment getFragment(String deviceId, String type, String unit, LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT date, measurements, secret_key FROM fragment "
					+ "WHERE device_id = ? AND type = ? AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				Fragment fragment = new Fragment(deviceId, type, unit,
						LocalDate.parse(resultSet.getString("date")), resultSet.getLong("secret_key"));
				fragment.setMeasurementsFromJsonArrayString(resultSet.getString("measurements"));
				return fragment;
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate from,
			LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT date, measurements, secret_key FROM fragment "
					+ "WHERE device_id = ? AND type = ? AND unit = ? AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(from));
			preparedStatement.setDate(5, Date.valueOf(to));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment(deviceId, type, unit,
						LocalDate.parse(resultSet.getString("date")), resultSet.getLong("secret_key"));
				fragment.setMeasurementsFromJsonArrayString(resultSet.getString("measurements"));
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
	
	public static List<Fragment> getFragments(String type, String unit) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT * FROM fragment WHERE type = ? AND unit = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, unit);

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment(resultSet.getString("device_id"), type, unit,
						LocalDate.parse(resultSet.getString("date")), resultSet.getLong("secret_key"));
				fragment.setMeasurementsFromJsonArrayString(resultSet.getString("measurements"));
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

	public static List<Fragment> getFragments(int noOfDevices, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT distinct device_id FROM fragment "
					+ "WHERE type = ? AND unit = ? AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, type);
			preparedStatement.setString(2, unit);

			ResultSet resultSet = preparedStatement.executeQuery();
			int counter = 0;
			while (resultSet.next() && counter < noOfDevices) {
				fragments.addAll(getFragments(resultSet.getString("device_id"), type, unit, from, to));
				counter = counter + 1;
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return fragments;
	}

	public static void insertRequest(Request request) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO request (device_id, data_user_id, type, unit, date, number_of_watermark, timestamps) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, request.getDeviceId());
			preparedStatement.setInt(2, request.getDataUserId());
			preparedStatement.setString(3, request.getType());
			preparedStatement.setString(4, request.getUnit());
			preparedStatement.setDate(5, Date.valueOf(request.getDate()));
			preparedStatement.setInt(6, request.getNumberOfWatermark());
			preparedStatement.setArray(7, connection.createArrayOf("timestamp", request.getTimestamps().toArray()));
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static Request getRequest(int dataUserId, String deviceId, String type, String unit,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT number_of_watermark, timestamps FROM request WHERE device_id = ? AND data_user_id = ? AND type = ? "
					+ "AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setInt(2, dataUserId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setDate(5, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				Timestamp[] timestampArray = (Timestamp[]) resultSet.getArray("timestamps").getArray();
				ArrayList<LocalDateTime> timestamps = new ArrayList<LocalDateTime>();
				for (int i = 0; i < timestampArray.length; i++) {
					timestamps.add(timestampArray[i].toLocalDateTime());
				}
				return new Request(deviceId, dataUserId, type, unit, date,
						resultSet.getInt("number_of_watermark"), timestamps);
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static List<Request> getRequests(String deviceId, String type, String unit, LocalDate date) {
		List<Request> requests = new LinkedList<>();
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT data_user_id, number_of_watermark, timestamps FROM request "
					+ "WHERE device_id = ? AND type = ? AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Timestamp[] timestampArray = (Timestamp[]) resultSet.getArray("timestamps").getArray();
				ArrayList<LocalDateTime> timestamps = new ArrayList<LocalDateTime>();
				for (int i = 0; i < timestampArray.length; i++) {
					timestamps.add(timestampArray[i].toLocalDateTime());
				}
				requests.add(new Request(deviceId, resultSet.getInt("data_user_id"), type, unit, date,
						resultSet.getInt("number_of_watermark"), timestamps));
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return requests;
	}
	
	public static void updateRequest(Request request) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "UPDATE request SET timestamps = ? WHERE device_id = ? AND data_user_id = ? AND type = ? "
					+ "AND unit = ? AND date = ?";
			
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setArray(1, connection.createArrayOf("timestamp", request.getTimestamps().toArray()));
			preparedStatement.setString(2, request.getDeviceId());
			preparedStatement.setInt(3, request.getDataUserId());
			preparedStatement.setString(4, request.getType());
			preparedStatement.setString(5, request.getUnit());
			preparedStatement.setDate(6, Date.valueOf(request.getDate()));
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static Integer getNumberOfWatermark(String deviceId, String type, String unit, LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT max(number_of_watermark) FROM request WHERE device_id = ? AND type = ? "
					+ "AND unit = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setDate(4, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getInt("max");
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}
}