package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

	private Connection connection;

	public DBConnection() {
		String url = "jdbc:postgresql://localhost:5432/watermarking";
		Properties props = new Properties();
		props.setProperty("user", "postgres");
		props.setProperty("password", "admin");
		try {
			this.connection = DriverManager.getConnection(url, props);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void insertFragments() {
		
	}

	public void resetFragmentTable() {
		String sql = "DELETE FROM fragment";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}
	
	public void resetLogTable() {
		String sql = "DELETE FROM log";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sql);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
	}

}
