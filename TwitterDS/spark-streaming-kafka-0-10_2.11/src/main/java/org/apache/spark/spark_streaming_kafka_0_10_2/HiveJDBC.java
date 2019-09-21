package org.apache.spark.spark_streaming_kafka_0_10_2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class HiveJDBC {
	private static String driverName = "org.apache.hive.jdbc.HiveDriver";

	/**
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		String filepath = "/user/cloudera/output/-1569009990000.seq/part-00000";
		String tableName = "twitter";
		HiveJDBC.loadData(tableName ,filepath);
	}
	
	public static void loadData(String tableName, String filepath) throws SQLException{
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		Connection con = DriverManager.getConnection(
				"jdbc:hive2://localhost:10000/default", "", "");
		Statement stmt = con.createStatement();
		//String tableName = "testHiveDriverTable";
		//stmt.execute("drop table " + tableName);
		String ct = HiveJDBC.getcreateExternalTable(tableName);
		boolean res = stmt.execute(ct);
		
		// show tables
		String sql = "show tables '" + tableName + "'";
		System.out.println("Running: " + sql);
		res = stmt.execute(sql);
//		if (res.next()) {
//			System.out.println(res.getString(1));
//		}
		
		// describe table
		sql = "describe " + tableName;
		System.out.println("Running: " + sql);
		res = stmt.execute(sql);
//		while (res.next()) {
//			System.out.println(res.getString(1) + "\t" + res.getString(2));
//		}

		// load data into table
		// NOTE: filepath has to be local to the hive server
		// NOTE: /tmp/a.txt is a ctrl-A separated file with two fields per line
		
		sql = "load data inpath '" + filepath + "' into table "
				+ tableName;
		System.out.println("Running: " + sql);
		res = stmt.execute(sql);
		
		System.out.print(res);
//
//		// select * query
//		sql = "select * from " + tableName;
//		System.out.println("Running: " + sql);
//		res = stmt.executeQuery(sql);
//		while (res.next()) {
//			System.out.println(String.valueOf(res.getInt(1)) + "\t"
//					+ res.getString(2));
//		}
//
//		// regular hive query
//		sql = "select count(1) from " + tableName;
//		System.out.println("Running: " + sql);
//		res = stmt.executeQuery(sql);
//		while (res.next()) {
//			System.out.println(res.getString(1));
//		}
	}
	
    public static String getcreateExternalTable(String tableName) {
    	return ("CREATE TABLE IF NOT EXISTS twitter " +
                "(id_str String, " +
                "created_at BIGINT, " +
                "favorite_count INT, " +
                "user_id String, " +
                "user_name String, " +
                "user_location String, " +
                "text String) " +
//                "PARTITIONED BY (id_str STRING) " +
                "ROW FORMAT DELIMITED FIELDS TERMINATED BY ';' LINES TERMINATED BY '\\n'");
    }
}