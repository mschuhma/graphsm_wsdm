package de.uma.dws.graphsm.tripleweighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.mysql.DBPediaWeightsMySqlConnector;

public class TripleWeighterGlobalPredObjIC implements TripleWeighter{

	/**
	 * Get InformationContent (IC) from pre-computed mysql table
	 */
	
	final static Logger log = LoggerFactory.getLogger(TripleWeighterGlobalPredObjIC.class);

	private static DBPediaWeightsMySqlConnector dbc;
	
	@Override
	public Double compute(Triple triple) {
		
		Double aggregatedIC = dbc.getPropObjCombIC(triple.getPred(), triple.getObj());
//		Double propIC 	 = dbc.getPropIC(triple.getPred());
//		Double propObjIC = dbc.getPropObjIC(triple.getPred(), triple.getObj());

		return aggregatedIC;
	}
	
//	public Double compute(String pred) {
//		return dbc.getPropIC(pred);
//	}
	
	//TODO Checken! Hier ist was falsch!
//	public Double compute(String pred, String obj) {
//		Double propObjIC = dbc.getPropObjIC(pred, obj);
//		if (propObjIC.isNaN()) {
//			Integer cnt = dbc.getPropCnt(pred);
//			if (cnt == null) {
//				log.debug("Property {} /Object {} not found in Information Content Database. Setting value to NaN", pred, obj);
//				return Double.NaN;
//			}
//			propObjIC = Math.log(cnt/1d);
//		}
//		return propObjIC;
//	}
	
	public TripleWeighterGlobalPredObjIC() {
		dbc = DBPediaWeightsMySqlConnector.getInstance();
		
	}
	
	public String toString() {
		return "DBPediaGlobalEdgeWeight " +
				"(Take Information Content (IC) from mysql database.";
	}
	
	public static void main(String[] args) {
		
		Triple t = new Triple("http://dbpedia.org/resource/Aida", "rdf:type", "http://dbpedia.org/class/yago/Operas");
		TripleWeighter weighter = new TripleWeighterGlobalPredObjIC();
		
		System.out.println(weighter.compute(t));
	}

}
