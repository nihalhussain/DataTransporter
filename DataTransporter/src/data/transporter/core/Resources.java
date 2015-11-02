package data.transporter.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Resources {

	public final String databaseDriver = "com.mysql.jdbc.Driver";
	public final String databaseConnectionString = "jdbc:mysql://144.167.117.14:3306/";
	public final String username = "ukraine_user";
	public final String password = "summer2014";
	
	public String getDatabaseDriver() 
	{
		return databaseDriver;
	}
	
	public String getDatabaseConnectionString()
	{
		return databaseConnectionString;
	}
	
	public String getUsername() 
	{
		return username;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	/*
	 * this method inserts the data into database.
	 */
	public void runQuery(String insertQuery)
	{
		try {
			Class.forName(this.getDatabaseDriver());

			String connectionString = this.getDatabaseConnectionString();
			String userName = this.getUsername();
			String password = this.getPassword();
			Connection conn = DriverManager.getConnection(connectionString, userName, password); //r.getDatabaseConnectionString(), r.getUsername(), r.getPassword());
			Statement st = conn.createStatement();
			st.executeUpdate(insertQuery);
			conn.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

	}
}
