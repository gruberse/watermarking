package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
	
	private Connection connection;

	public Database() {
		try {
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/watermarking", "admin", "admin");
			if(connection != null) {
				System.out.println("Connection success");
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		
	}
}
