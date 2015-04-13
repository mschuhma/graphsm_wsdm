package de.uma.dws.graphsm.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;

public abstract class MySqlConnector {
	
	final static Logger log = LoggerFactory.getLogger(MySqlConnector.class);
	final static Configuration conf = ConfFactory.getConf();
	
	private static final String host 	= conf.getString("mysql.host");
	private static final int    port 	= conf.getInt("mysql.port");
	private static final String user 	= conf.getString("mysql.user");
	private static final String passwd	= conf.getString("mysql.passwd");
	private static String database		= null;

	protected static Connection connection = null;
	protected static LodURI shortener = null;
	
	protected static Double totalPropObjCnt = null;
	
	protected static MySqlConnector instance = null;
	
	protected MySqlConnector(String database) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		MySqlConnector.database = database;
		connect();
		
		shortener = LodURI.getInstance();	
	}
	
	
	protected static void connect() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, passwd);
		} catch (SQLException e) {
			log.warn("Connection to MySQL server {} failed with {} {}", host, e.getMessage(), e.getSQLState());
			throw new RuntimeException("Connection to MySQL server failed " + e.getMessage());
		}
		log.info("MySQL connection establish: host {}, database {}", host, database);
	}
	
	protected void closeConnection() {
		try {
			connection.close();
		} catch (SQLException e) {
			log.warn("Closing connection from  MySQL server {} failed with {} {}", host, e.getMessage(), e.getSQLState());
		}
		log.debug("MySQL Connection closed.");
	}
	
	
	public static void main(String[] args) {
		
		MySqlConnector dbc = DBPediaWeightsMySqlConnector.getInstance();
		dbc.closeConnection();
	}

}
