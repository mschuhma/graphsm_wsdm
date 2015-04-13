package de.uma.dws.graphsm.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for querying mysql table containing global triple counts. Table Schema:
 * 1) PredCnt (pred, cnt) 
 * 2) ObjCnt (obj, cnt) 
 * 3) PredObjCnt (pred, obj, cnt) 
 * Tables are created with {@link de.uma.dws.graphsm.webservice.DBPediaCreateTripleCountsTables}
 */

public class DBPediaCountsMySqlConnector extends MySqlConnector {

	final static Logger	 log	         = LoggerFactory.getLogger(DBPediaCountsMySqlConnector.class);
	private static String	db	         = conf.getString("mysql.db.dbpediacounts");
	private static Double	tripleCnt	= null;

	// Private constructor for singleton
	private DBPediaCountsMySqlConnector(String database) {
		super(database);
	}

	public static DBPediaCountsMySqlConnector getInstance() {
		if (instance == null)
			instance = new DBPediaCountsMySqlConnector(db);
		try {
			if (connection.isClosed())
				connect();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return (DBPediaCountsMySqlConnector) instance;
	}

	public Double getTripleCnt() {
		if (tripleCnt == null) {
			String q = "SELECT SUM(cnt) FROM PredCnt";
			tripleCnt = queryForSingleResult(q);
		}

		return tripleCnt;
	}

	public Double getPredCnt(String pred) {

//		pred = shortener.toPrefixedUri(pred).replace("'", "\\'");

		String q = "SELECT cnt FROM PredCnt WHERE " + "pred = \"" + pred + "\";";
		Double cnt = queryForSingleResult(q);

		if (cnt == null)
			log.warn("Predicate count not found, check MySQL database! ", q);

		return cnt;
	}

	public Double getObjCnt(String obj) {

//		obj = shortener.toPrefixedUri(obj).replace("'", "\\'");

		String q = "SELECT cnt FROM ObjCnt WHERE " + "obj = \"" + obj + "\";";
		Double cnt = queryForSingleResult(q);

		if (cnt == null)
			log.warn("Object count not found, check MySQL database! Query=", q);

		return cnt;
	}
	
	public Double getPredObjCnt(String pred, String obj) {
		
//		pred = shortener.toPrefixedUri(pred).replace("'", "\\'");
//		obj = shortener.toPrefixedUri(obj).replace("'", "\\'");
		
		String q = "SELECT cnt FROM PredObjCnt WHERE " + "pred = \"" + pred + "\" AND " + "obj  = \"" + obj + "\" ;";

		Double cnt = queryForSingleResult(q);
		
		//Tables contains only <pred,obj> counts > 1
		//Assuming count of 1 for <pred,obj> combinations not found
		if (cnt == null)
			cnt = 1.0;

		return cnt;
	}

	
	/**
	 * Standard executor for queries that result in exactly one numeral value
	 */
	private Double queryForSingleResult(String query) {

		Statement stmt = null;
		ResultSet rs = null;
		ArrayList<Double> rsValues = new ArrayList<>();

		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery(query);

		}
		catch (SQLException e) {
			log.warn("SQL Connection error: {} {}", e.getMessage(), query);
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		finally {
			if (rs == null) {
				try {
					stmt.close();
				}
				catch (SQLException e) {
					log.warn("SQL Connection error: {} {}", e.getMessage(), query);
					e.printStackTrace();
					throw new RuntimeException();
				}
				return null;
			}
			try {
				while (rs.next()) {
					rsValues.add(rs.getDouble(1));
				}
				rs.close();
				stmt.close();
			}
			catch (SQLException e) {
				log.warn("Reading MySQL results failed with {} {}", e.getMessage(), e.getSQLState());
				e.printStackTrace();
			}
			if (rsValues.size() > 1) {
				log.warn("More than one value found for your query {}", query);
				return Double.NaN;
			}
			if (rsValues.size() < 1) {
				//no value found for query (makes sense in some cases)
				return null;
			}
		}

		return rsValues.get(0);
	}

	public static void main(String[] args) {

		DBPediaCountsMySqlConnector dbc = DBPediaCountsMySqlConnector.getInstance();

		System.out.println("getPredCnt(rdf:type) = " + dbc.getPredCnt("rdf:type"));
		
		System.out.println("getObjCnt(yago:FilmsBasedOnWorksByWilliamFaulkner)         = " 
					+ dbc.getObjCnt("yago:FilmsBasedOnWorksByWilliamFaulkner"));
		
		System.out.println("getPredObjCnt(rdf:type, yago:FilmsBasedOnWorksByWilliamFaulkner) = "
		         + dbc.getPredObjCnt("rdf:type", "yago:FilmsBasedOnWorksByWilliamFaulkner"));
		
		System.out.println("");
		
		System.out.println("getPredCnt(rdf:type) = " + dbc.getPredCnt("rdf:type"));
		
		System.out.println("getObjCnt(dbpedia:Rosalynn_Carter) = " 
					+ dbc.getObjCnt("dbpedia:Rosalynn_Carter"));
		
		System.out.println("getPredObjCnt(rdf:type, dbpedia:Rosalynn_Carter) = "
		         + dbc.getPredObjCnt("dbpprop:founder", "dbpedia:Rosalynn_Carter"));

		dbc.closeConnection();
	}

}
