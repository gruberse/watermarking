package utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import entities.Fragment;
import utils.JsonService.From;

public class DatabaseService {

	public static List<Fragment> getFragments(String deviceId, String type, String unit, LocalDate from, LocalDate to) {
		List<Fragment> list = new LinkedList<>();
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "SELECT \"deviceId\", type, unit, date, measurements FROM fragments "
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
						JsonService.getMeasurementsFromJson(From.String, resultSet.getString("measurements")));
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

			String sql = "INSERT INTO fragments (\"deviceId\", type, unit, date, measurements) VALUES "
					+ "(?, ?, ?, ?, ?::JSON)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			for (Fragment fragment : fragments) {
				preparedStatement.setString(1, fragment.getDeviceId());
				preparedStatement.setString(2, fragment.getType());
				preparedStatement.setString(3, fragment.getUnit());
				preparedStatement.setDate(4, Date.valueOf(fragment.getDate()));
				preparedStatement.setObject(5, fragment.getMeasurementsJsonString(true));
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
