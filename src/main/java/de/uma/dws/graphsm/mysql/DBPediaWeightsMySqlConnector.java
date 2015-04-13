package de.uma.dws.graphsm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for querying MySQL table containing global information content (IC) values. Tables are created with by
 * {@link de.uma.dws.graphsm.webservice.DBPediaCreatePropertyDistributionTables#fullPropertyObjectCount}. There there
 * also for database schema and more infos. The key method in this class are
 * {@link DBPediaWeightsMySqlConnector#getPropObjCombIC()} and {@link DBPediaWeightsMySqlConnector#getTotalPropObjCnt()},
 * all other methods are actually deprecated. For the new frequency weights computation based on count frequencies see
 * {@link DBPediaCountsMySqlConnector}.
 */

public class DBPediaWeightsMySqlConnector extends MySqlConnector {
	
	final static Logger log = LoggerFactory.getLogger(DBPediaWeightsMySqlConnector.class);
	
	private static final String db = conf.getString("mysql.db.dbpediaweights");
	private static Double totalPropObjCnt = null;
	
	private DBPediaWeightsMySqlConnector(String database) {
		super(database);
	}	
	
	public static DBPediaWeightsMySqlConnector getInstance() {
		if (instance == null)
			instance = new DBPediaWeightsMySqlConnector(db);
		try {
			if (connection.isClosed())
				connect();
		} catch (SQLException e) {e.printStackTrace();}	
		return (DBPediaWeightsMySqlConnector) instance;	
	}

	
	private Double getTotalPropObjCnt() {
		
		String query = "SELECT SUM(cnt) FROM PropObjCombIC;";
		return queryIC(query);
	}

	
	public Integer getPropCnt(String prop) {
		
		Statement stmt 	= null;
		ResultSet rs 	= null;
		ArrayList<Integer> rsValues = new ArrayList<>();
		
		prop = shortener.toPrefixedUri(prop).replace("'", "\\'");
		String query = 
				"SELECT cnt FROM PropCnt WHERE " +
				"prop = '"+prop+"';";

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);
		
		} catch (SQLException e) {
			log.warn("SQL Connection error {}", e.getMessage());
			
		} finally {
			if (rs == null) {
				try {stmt.close();} 
				catch (SQLException e) {e.printStackTrace();}
				return null;
			}
			try {
				while (rs.next()) {
					rsValues.add(rs.getInt("cnt"));
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				log.warn("Reading  MySQL results failed with {} {}", e.getMessage(), e.getSQLState());
			} 
			if (rsValues.size() > 1) {
				log.warn("Duplicate count values found! Check database for inconsistancies");
				return null;
			}
			if (rsValues.size() < 1) {
				log.warn("No count values found!");
				return null;
			}
		}
		
		return rsValues.get(0);
	}
	
	public Double getPropObjIC(String prop, String obj) {
		
		prop = shortener.toPrefixedUri(prop).replace("'", "\\'");
		obj  = shortener.toPrefixedUri(obj).replace("'", "\\'");
		String query = 
				"SELECT IC FROM PropObjIC WHERE " +
				"prop = '"+prop+"' AND " +
				"obj  = '"+obj+"' ;";

		Double infoContentValue = queryIC(query);
		
		if (infoContentValue == null) {
			infoContentValue = Double.NaN;
		}
		return infoContentValue;
	}

	public Double getPropObjCombIC(String prop, String obj) {
		
		if (totalPropObjCnt == null) 
			totalPropObjCnt = getTotalPropObjCnt();
		
		prop = shortener.toPrefixedUri(prop).replace("'", "\\'");
		obj  = shortener.toPrefixedUri(obj).replace("'", "\\'");
		String q1 = 
				"SELECT IC FROM PropObjCombIC WHERE " +
				"prop = '"+prop+"' AND " +
				"obj  = '"+obj+"' ;";

		Double infoContentValue = queryIC(q1);
		
		if (infoContentValue == null) {
			//Always assume <prop, obj> count = 1 if not found in database
			infoContentValue = Math.log(totalPropObjCnt/1d);
			
			//Assume <prop, obj> count = 1, check in database
//			String q2 = 
//					"SELECT EXISTS ( " +
//					"SELECT 1 FROM PropObjCombIC " +
//					"WHERE prop = '"+prop+"' ) ;";
//			
//			if (queryIC(q2) == 1d) {
//				infoContentValue = Math.log(totalPropObjCnt/1d);
//			} 
//			
//			else {
//				infoContentValue = Double.NaN;
//				log.debug("Warning, cannot compute or estimate weight for {} {}.", prop, obj);
//			}	
		}
		return infoContentValue;
	}	
	
	public Double getPropIC(String prop) {
		
		prop = shortener.toPrefixedUri(prop).replace("'", "\\'");
		String query = 
				"SELECT IC FROM PropIC WHERE " +
				"prop = '"+prop+"';";
		
		Double infoContentValue = queryIC(query);
		
		if (infoContentValue == null) {
			log.warn("Property {} not found in Information Content Database. Setting value to NaN", prop);
			infoContentValue = Double.NaN;
		}
		return infoContentValue;
	}
	
	private Double queryIC(String query) {
		
		Statement stmt 	= null;
		ResultSet rs 	= null;
		ArrayList<Double> rsValues = new ArrayList<>();
		
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
					rsValues.add(rs.getDouble(1));
				}
				rs.close();
				stmt.close();
			} catch (SQLException e) {
				log.warn("Reading  MySQL results failed with {} {}", e.getMessage(), e.getSQLState());
			} 
			if (rsValues.size() > 1) {
				log.warn("Duplicate IC values found! Check database for inconsistancies");
				return Double.NaN;
			}
			if (rsValues.size() < 1) {
//				log.debug("No IC values found!"); Thats okay as <prop,obj> counts of 1 have not been recorded in the database
				return null;
			}
		}
		
		return rsValues.get(0);
	}
	
	public static void main(String[] args) {
		
		DBPediaWeightsMySqlConnector dbc = DBPediaWeightsMySqlConnector.getInstance();
		
		System.out.println("IC(dcterms:subject:) = " + dbc.getPropIC("dcterms:subject"));
		System.out.println("IC(rdf:type) = " + dbc.getPropIC("rdf:type"));
		System.out.println(
				"IC(rdf:type, yago:FilmsBasedOnWorksByWilliamFaulkner) = " +
				dbc.getPropObjIC("rdf:type", "yago:FilmsBasedOnWorksByWilliamFaulkner"));
		
		System.out.println(
				"ICCombined(rdf:type, yago:FilmsBasedOnWorksByWilliamFaulkner) = " +
				dbc.getPropObjCombIC("rdf:type", "yago:FilmsBasedOnWorksByWilliamFaulkner"));
		
		System.out.println(
				"ICCombined(rdf:type, someFakeValueObject) = " +
				dbc.getPropObjCombIC("rdf:type", "someFakeValueObject"));
		
		System.out.println(
				"ICCombined(faultyProp, yago:FilmsBasedOnWorksByWilliamFaulkner) = " +
				dbc.getPropObjCombIC("faultyProp", "yago:FilmsBasedOnWorksByWilliamFaulkner"));
		
		dbc.closeConnection();
	}

}
