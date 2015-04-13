package de.uma.dws.graphsm.tripleweighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.mysql.DBPediaCountsMySqlConnector;

/**
 * @author michaelschuhmacher
 *
 * Computes the simple sum of information content IC(pred) and IC(obj)
 * where
 * <pre>
 * {@code 
 * IC(Pred=pred) = -ln(P(Pred=pred)) = ln(cnt(allTriple)/cnt(pred))
 * IC(Obj=obj) = -ln(P(Obj=obj)) = ln(cnt(allTriple)/cnt(obj))
 * }
 * </pre>
 * thus resulting into 
 * <pre>
 * {@code
 * IC(Pred=pred) + IC(Obj=obj) = 2*ln(cnt(allTriple)) - ln(cnt(pred)) - ln(cnt(obj))
 * }
 * </pre>
 * which is actually computed here.
 */
public class TripleWeighterAddedIC implements TripleWeighter, TripleCostWeighter {
	
	final static Logger log = LoggerFactory.getLogger(TripleWeighterAddedIC.class);
	final static DBPediaCountsMySqlConnector db  = DBPediaCountsMySqlConnector.getInstance();;
	
	final static Double 	logBase = Math.E;
	private boolean 		transformToCost;
	private Double 			maxCostValue = null;
	
	public TripleWeighterAddedIC(boolean transformToCost) {
		this.transformToCost = transformToCost;
		//Compute highest possible Added IC value as upper bound for cost
		maxCostValue =   2* (Math.log(db.getTripleCnt()) / Math.log(logBase))
		      	 		- 2* (Math.log(1d) 					/ Math.log(logBase))
		      	 		+ 0.00000001;
	}
	
	public Double getMaxCostValue() {
		return maxCostValue;
	}
	
	@Override
   public Double compute(Triple t) {
		Double w = null;
		try {
	      w = 2*(Math.log(db.getTripleCnt())          / Math.log(logBase))
	      	 - (Math.log(db.getPredCnt(t.getPred())) / Math.log(logBase))
	      	 - (Math.log(db.getObjCnt(t.getObj()))   / Math.log(logBase));
      }
      catch (Exception e) {
      	log.warn("TripleWeighterAddedIC failed for triple {} with {}", t, e.getMessage());
      	if (t.getPred().contains("dbpprop:website"))
      		return maxCostValue;
      	throw new RuntimeException(t + e.getMessage());
      }
		return transformToCost ? maxCostValue - w : w;
   }
	
   public Double computeICPred(Triple t) {
		Double w = (Math.log(1d * db.getTripleCnt() / db.getPredCnt(t.getPred())) / Math.log(logBase));
		return w;
   }
   
   public Double computeICObj(Triple t) {
   	Double w = (Math.log(1d * db.getTripleCnt() / db.getObjCnt(t.getObj())) / Math.log(logBase));
		return w;
   }
   
   	
	@Override
   public String toString() {
	   return "AddedIC";
   }

	public static void main(String[] args) {
		
		Triple t1 = new Triple("dbpedia:Jimmy_Carter", "dbpediaowl:spouse", "dbpedia:Rosalynn_Carter");
		Triple t2 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "foaf:Person");
		Triple t3 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "yago:NobelPeacePrizeLaureates");
		
		TripleCostWeighter weighter = new TripleWeighterAddedIC(true);
		
		System.out.println(weighter.getMaxCostValue());
		
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighter.compute(t1));
		
		System.out.print("TripleWeighterJointIC("+t2+") = ");
		System.out.println(weighter.compute(t2));
		
		System.out.print("TripleWeighterJointIC("+t3+") = ");
		System.out.println(weighter.compute(t3));
		
		//Debug check
		TripleWeighterAddedIC weighterDebug = new TripleWeighterAddedIC(true);
		System.out.println();
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighterDebug.compute(t1));
		System.out.print("ICPred " + weighterDebug.computeICPred(t1));
		System.out.print(" + ICObj " + weighterDebug.computeICObj(t1));
		System.out.println(" = " + (weighterDebug.computeICPred(t1) + weighterDebug.computeICObj(t1)));
	}

}
