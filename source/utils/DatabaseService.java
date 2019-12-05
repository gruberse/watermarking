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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import entities.DataProfile;
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

			String sql = "INSERT INTO usability_constraint (type, unit, frequency, minimum_value, maximum_value, maximum_error, number_of_watermarks) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, usabilityConstraint.getType());
			preparedStatement.setString(2, usabilityConstraint.getUnit());
			preparedStatement.setInt(3, usabilityConstraint.getFrequency());
			preparedStatement.setBigDecimal(4, usabilityConstraint.getMinimumValue());
			preparedStatement.setBigDecimal(5, usabilityConstraint.getMaximumValue());
			preparedStatement.setBigDecimal(6, usabilityConstraint.getMaximumError());
			preparedStatement.setInt(7, usabilityConstraint.getNumberOfWatermarks());
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
						resultSet.getInt("number_of_watermarks"));
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

	public static List<Fragment> getFragments(String deviceId, String type, String unit, int frequency, LocalDate from,
			LocalDate to) {
		List<Fragment> fragments = new LinkedList<>();

		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT date, measurements, secret_key, mean, dataset_id FROM fragment "
					+ "WHERE device_id = ? AND type = ? AND unit = ? AND frequency = ? AND date BETWEEN ? AND ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setInt(4, frequency);
			preparedStatement.setDate(5, Date.valueOf(from));
			preparedStatement.setDate(6, Date.valueOf(to));

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				Fragment fragment = new Fragment(deviceId, type, unit, frequency, LocalDate.parse(resultSet.getString("date")),
								resultSet.getLong("secret_key"), resultSet.getString("dataset_id"));
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

	public static void insertProfiles(List<DataProfile> dataProfiles) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO data_profile (dataset_id, device_id, type, unit, frequency, "
					+ "value_bin_size, slope_bin_size, curvature_bin_size, "
					+ "relative_value_distribution, relative_slope_distribution, relative_curvature_distribution) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			for (DataProfile dataProfile : dataProfiles) {
				preparedStatement.setString(1, dataProfile.getDatasetId());
				preparedStatement.setString(2, dataProfile.getDeviceId());
				preparedStatement.setString(3, dataProfile.getType());
				preparedStatement.setString(4, dataProfile.getUnit());
				preparedStatement.setInt(5, dataProfile.getFrequency());
				preparedStatement.setBigDecimal(6, dataProfile.getValueBinSize());
				preparedStatement.setBigDecimal(7, dataProfile.getSlopeBinSize());
				preparedStatement.setBigDecimal(8, dataProfile.getCurvatureBinSize());
				preparedStatement.setArray(9,
						connection.createArrayOf("text", dataProfile.getRelativeValueDistributionAsStringArray()));
				preparedStatement.setArray(10,
						connection.createArrayOf("text", dataProfile.getRelativeSlopeDistributionAsStringArray()));
				preparedStatement.setArray(11,
						connection.createArrayOf("text", dataProfile.getRelativeCurvatureDistributionAsStringArray()));
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static DataProfile getDataProfile(String datasetId, String deviceId, String type, String unit,
			int frequency) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT value_bin_size, slope_bin_size, curvature_bin_size, "
					+ "relative_value_distribution, relative_slope_distribution, relative_curvature_distribution FROM data_profile "
					+ "WHERE dataset_id = ? AND device_id = ? AND type = ? AND unit = ? AND frequency = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, datasetId);
			preparedStatement.setString(2, deviceId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setInt(5, frequency);

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				DataProfile dataProfile = new DataProfile(datasetId, deviceId, type, unit, frequency);
				dataProfile.setValueBinSize(resultSet.getBigDecimal("value_bin_size"));
				dataProfile.setSlopeBinSize(resultSet.getBigDecimal("slope_bin_size"));
				dataProfile.setCurvatureBinSize(resultSet.getBigDecimal("curvature_bin_size"));
				dataProfile.setRelativeValueDistributionFromStringArray(
						(String[]) resultSet.getArray("relative_value_distribution").getArray());
				dataProfile.setRelativeSlopeDistributionFromStringArray(
						(String[]) resultSet.getArray("relative_slope_distribution").getArray());
				dataProfile.setRelativeCurvatureDistributionFromStringArray(
						(String[]) resultSet.getArray("relative_curvature_distribution").getArray());
				return dataProfile;
			}

			resultSet.close();
			preparedStatement.close();
			connection.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	public static void insertRequest(Request request) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO request (device_id, data_user_id, type, unit, frequency, date, number_of_watermark, timestamps) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, request.getDeviceId());
			preparedStatement.setInt(2, request.getDataUserId());
			preparedStatement.setString(3, request.getType());
			preparedStatement.setString(4, request.getUnit());
			preparedStatement.setInt(5, request.getFrequency());
			preparedStatement.setDate(6, Date.valueOf(request.getDate()));
			preparedStatement.setInt(7, request.getNumberOfWatermark());
			preparedStatement.setArray(8, connection.createArrayOf("timestamp", request.getTimestamps().toArray()));
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static Request getRequest(int dataUserId, String deviceId, String type, String unit, int frequency,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT number_of_watermark, timestamps FROM request WHERE device_id = ? AND data_user_id = ? AND type = ? "
					+ "AND unit = ? AND frequency = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setInt(2, dataUserId);
			preparedStatement.setString(3, type);
			preparedStatement.setString(4, unit);
			preparedStatement.setInt(5, frequency);
			preparedStatement.setDate(6, Date.valueOf(date));

			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				Timestamp[] timestampArray = (Timestamp[]) resultSet.getArray("timestamps").getArray();
				ArrayList<LocalDateTime> timestamps = new ArrayList<LocalDateTime>();
				for(int i = 0; i < timestampArray.length; i++) {
					timestamps.add(timestampArray[i].toLocalDateTime());
				}
				return new Request(deviceId, dataUserId, type, unit, frequency, date,
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

	public static void updateRequest(Request request) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "UPDATE request SET timestamps = ? WHERE device_id = ? AND data_user_id = ? AND type = ? "
					+ "AND unit = ? AND frequency = ? AND date = ? AND number_of_watermark = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setArray(1, connection.createArrayOf("timestamp", request.getTimestamps().toArray()));
			preparedStatement.setString(2, request.getDeviceId());
			preparedStatement.setInt(3, request.getDataUserId());
			preparedStatement.setString(4, request.getType());
			preparedStatement.setString(5, request.getUnit());
			preparedStatement.setInt(6, request.getFrequency());
			preparedStatement.setDate(7, Date.valueOf(request.getDate()));
			preparedStatement.setInt(8, request.getNumberOfWatermark());
			preparedStatement.executeUpdate();

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public static Integer getNumberOfWatermark(String deviceId, String type, String unit, int frequency,
			LocalDate date) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT max(number_of_watermark) FROM request WHERE device_id = ? AND type = ? "
					+ "AND unit = ? AND frequency = ? AND date = ?";

			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.setString(1, deviceId);
			preparedStatement.setString(2, type);
			preparedStatement.setString(3, unit);
			preparedStatement.setInt(4, frequency);
			preparedStatement.setDate(5, Date.valueOf(date));

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
			fragments.addAll(getFragments(deviceIds.get(i), type, unit, 5, from, to));
		}

		return fragments;
	}

}