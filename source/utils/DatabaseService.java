package utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import entities.Fragment;

public class DatabaseService {

	public static void uploadFragments(List<Fragment> fragments) {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {

			String sql = "INSERT INTO fragments (\"deviceId\", type, unit, date, data) VALUES "
					+ "(?, ?, ?, ?, ?::JSON)";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			for (Fragment fragment : fragments) {
				preparedStatement.setString(1, fragment.getDeviceId());
				preparedStatement.setString(2, fragment.getType());
				preparedStatement.setString(3, fragment.getUnit());
				preparedStatement.setDate(4, Date.valueOf(fragment.getDate()));
				preparedStatement.setObject(5, fragment.getData().toJSONString());
				preparedStatement.executeUpdate();
			}

			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

	public static void cleanupFragments() {
		try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking",
				"postgres", "admin")) {
			String sql = "DELETE FROM fragments";
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.executeUpdate();
			preparedStatement.close();
			connection.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
}
