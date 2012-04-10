package db;

import java.sql.*;
public class MysqlConnect {
	
	public static Connection connect() {
	 	  System.out.println("MySQL Connect Example.--Method called");
		  Connection conn = null;
		  String url = "jdbc:mysql://localhost:3306/";
		  String dbName = "ereader";
		  String driver = "com.mysql.jdbc.Driver";
		  String userName = "root"; 
		  String password = "root";
		  try {
			  Class.forName(driver).newInstance();
			  conn = DriverManager.getConnection(url+dbName,userName,password);
			  System.out.println("Connected to the database");
			 
			  } catch (Exception e) {
			  e.printStackTrace();
			  }
		  return conn;
		  }
		  
		  public static void CloseConnection(Connection conn)
		  {
			  try {
				conn.close();
				 System.out.println("Disconnected from database");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		  }
	
	  
}
