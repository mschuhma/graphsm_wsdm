package de.uma.dws.graphsm.tripleweighter;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.webservice.DBPedia;

public class TripleWeighterGlobalSimpleEdge implements TripleWeighter{

	/**
	 * weight = 1 + ln(1 + total_pred_count/(1 + pred_obj_count))
	 */
	
	final static Logger log = LoggerFactory.getLogger(TripleWeighterGlobalSimpleEdge.class);
	
	static HashMap<Integer, Double> weightsCache = new HashMap<Integer, Double>();
	static HashMap<Integer, Double> predCountCache = new HashMap<Integer, Double>();
	
	@Override
	public Double compute(Triple triple) {
		Double weight = weightsCache.get(triple.hashCodePO());
		if (weight == null) {
	
			Double globalEdgeCnt = predCountCache.get(triple.getPred());
			if (globalEdgeCnt == null)
				globalEdgeCnt = Double.valueOf(DBPedia.getPredWeight(triple.getPred()));
			Double specificEdgeCnt = Double.valueOf(DBPedia.getPredWeight(triple.getPred(), triple.getObj()));
	
			weight = 1d + Math.log(1d + globalEdgeCnt/(1d + specificEdgeCnt));
			
			weightsCache.put(triple.hashCodePO(), weight);
			log.debug("{} Weight {} globalEdgeCnt {} specificEdgeCnt {}",
					triple, weight, globalEdgeCnt, specificEdgeCnt);
		}

		return weight;
	}
	
	public String toString() {
		return "DBPediaGlobalEdgeWeight " +
				"(Computes edge specifity weight w based on RDF SPO triple info for complete DBPedia. " +
				"w = 1 + ln(1+cnt_P/(1+cnt_O_P))";
	}
	
	public static void main(String[] args) {
		Triple t = new Triple("http://dbpedia.org/resource/Aida", "rdf:type", "http://dbpedia.org/class/yago/Operas");
		TripleWeighter weighter = new TripleWeighterGlobalSimpleEdge();
		System.out.println(weighter.compute(t));
	}

}
