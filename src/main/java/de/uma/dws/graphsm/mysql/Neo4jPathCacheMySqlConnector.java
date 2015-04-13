package de.uma.dws.graphsm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Tuple;

public class Neo4jPathCacheMySqlConnector extends MySqlConnector {
	
	final static Logger log = LoggerFactory.getLogger(Neo4jPathCacheMySqlConnector.class);
	final static Configuration conf = ConfFactory.getConf();
	
	private static final String db = conf.getString("mysql.db.neo4jcache");
	private String table; //conf.getString("mysql.db.neo4jcache.table");
	
	private Neo4jPathCacheMySqlConnector(String database, String table) {
		super(database);
		this.table = table;
	}
	
	public Neo4jPathCacheMySqlConnector(String table) {
		this(db, table);
	}	
	
	public static Neo4jPathCacheMySqlConnector getInstance() {
		if (instance == null)
			instance = new Neo4jPathCacheMySqlConnector(db);
		try {
			if (connection.isClosed())
				connect();
		} catch (SQLException e) {e.printStackTrace();}	
		return (Neo4jPathCacheMySqlConnector) instance;	
	}
	
	
	public Tuple<Integer, Double> getPath(String node1Id, String node2Id) {
		return getPath(Long.valueOf(node1Id), Long.valueOf(node2Id));
	}
	
	public Tuple<Integer, Double> getPath(Long node1Id, Long node2Id, boolean useDebugTableCache) {
		
		if (useDebugTableCache && !table.contains("-debug"))
			table = table + "-debug";
		
		return getPath(node1Id, node2Id);
	}
	
	public Tuple<Integer, Double> getPath(Long node1Id, Long node2Id) {
		
		Statement stmt 	= null;
		ResultSet rs 	= null;
		ArrayList<Tuple<Integer, Double>> rsValues = new ArrayList<>();
		
		if (node1Id.compareTo(node2Id) < 0) {
			Long tmp = node2Id;
			node2Id = node1Id;
			node1Id = tmp;
		}
		
		String query = "" +
				"SELECT plen, pcost FROM " +
				"`" + table + "` " +
				"WHERE " +
				"node1 = \'"+ node1Id +"\' AND " +
				"node2 = \'"+ node2Id +"\' ;";

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
		
		} catch (SQLException e) {
			log.warn("SQL Connection error: {} {}", e.getMessage(), query);
			
		} finally {
			if (rs == null) {
				try {stmt.close();} 
				catch (SQLException e) {e.printStackTrace();}
				return null;
			}
			try {
				while (rs.next()) {
					
					Integer i = rs.getInt(1);
					Double  d = rs.getDouble(2);
					
					if (rs.wasNull()) {
						rsValues.add(new Tuple<Integer, Double>(null, null));
					} else {
						rsValues.add(new Tuple<Integer, Double>(i, d));
					}
				}
				
				rs.close();
				stmt.close();
				
			} catch (SQLException e) {
				log.warn("Reading MySQL results failed with {} {}", e.getMessage(), e.getSQLState());
			} 
			if (rsValues.size() > 1) {
				log.warn("Duplicate values found! Check database for inconsistancies");
				return null;
			}
			if (rsValues.size() < 1) {
				return null;
			}
		}
		
		return rsValues.get(0);
	}
	
	public int setPath(Long node1Id, Long node2Id, Integer pathLen, Double pastCost) {

		Statement stmt 	= null;

		if (node1Id.compareTo(node2Id) < 0) {
			Long tmp = node2Id;
			node2Id = node1Id;
			node1Id = tmp;
		}

		String query = "" +
				"INSERT INTO " +
				"`" + table + "` " +
				"VALUES ( "  +
				node1Id  +", " +
				node2Id  +", " + 
				pathLen  +", " +
				pastCost +");";

		try {
			stmt = connection.createStatement();
			return stmt.executeUpdate(query);
		
		} catch (SQLException e) {
			log.warn("Storing path in mysql cache failed: {}", e.getMessage());
			return 0;
		}
	}
	
	public int setPath(Long node1Id, Long node2Id, Integer pathLen, Double pastCost, StringBuffer pathSteps) {
		
		if (pathSteps != null) {
			pathSteps = new StringBuffer(pathSteps.toString().replace("'", "\\'"));
			if (!table.contains("-debug"))
					table = table + "-debug";
		}
		
		Statement stmt 	= null;

		if (node1Id.compareTo(node2Id) < 0) {
			Long tmp = node2Id;
			node2Id = node1Id;
			node1Id = tmp;
		}

		String query = "" +
				"INSERT INTO " +
				"`" + table + "` " +
				"VALUES ( " +
				node1Id +", " +
				node2Id +", " + 
				pathLen +", " +
				pastCost+", " + 
				"'"+pathSteps+"');";

		try {
			stmt = connection.createStatement();
			return stmt.executeUpdate(query);
		
		} catch (SQLException e) {
			log.warn("Storing path in mysql cache failed: {}", e.getMessage());
			return 0;
		}
	}
	
	public static void main(String[] args) {
		
		Neo4jPathCacheMySqlConnector dbc = Neo4jPathCacheMySqlConnector.getInstance();
		
		System.out.println("getPath(1, 1) = " + dbc.getPath(1l, 1l));
		System.out.println("getPath(2, 1) = " + dbc.getPath(2l, 1l));
		
		for (long i= 0; i<= 10; i++) {
			System.out.println("dbc.setPath(3, " + i + " , 5, 22.2) = " + dbc.setPath(3l, i, 5, 22.2));
		}
		
		dbc.closeConnection();
	}

}
