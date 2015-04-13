package de.uma.dws.graphsm.tripleweighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.mysql.DBPediaCountsMySqlConnector;

/**
 * @author michaelschuhmacher
 *
 * Computes weight as information content IC(pred) + point-wise mutual information PMI(pred,obj)
 * where
 * <pre>
 * {@code 
 * IC(Pred=pred) = 
 * -ln(P(Pred=pred)) = 
 * ln(cnt(allTriple)/cnt(pred))
 * 
 * PMI(Pred=pred,Obj=obj) = 
 * ln(P(Pred=pred,Obj=obj) / (P(Pred=pred) * P(Obj=obj))) =
 * ln( cnt(allTriple) ) + ln( cnt(pred,obj) / cnt(pred) * cnt(obj) )
 * }
 * </pre>
 */
public class TripleWeighterPMIPlusIC implements TripleWeighter, TripleCostWeighter {
	
	final static Logger log = LoggerFactory.getLogger(TripleWeighterPMIPlusIC.class);
	final static DBPediaCountsMySqlConnector db = DBPediaCountsMySqlConnector.getInstance();
	
	final static Double 	logBase = Math.E;
	private boolean 		transformToCost;
	private Double 			maxCostValue = null;
	
	public TripleWeighterPMIPlusIC(boolean transformToCost) {
		this.transformToCost = transformToCost;
		//Compute highest possible Added IC value as upper bound for cost
		maxCostValue = 2 * (Math.log(db.getTripleCnt()) / Math.log(logBase))
	                  //+ (Math.log(1d) 					/ Math.log(logBase)) 
	                - 2 * (Math.log(1d) 					/ Math.log(logBase))
	                  //- (Math.log(1d) 					/ Math.log(logBase))
		      	  		+ 0.00000001;
	}
	
	public Double getMaxCostValue() {
		return maxCostValue;
	}	
	
	/**
	 * Computes effectively
	 * <pre>
	 * {@code
	 * PMI + IC = 
	 * ln(cnt(allTriple)) + ln(cnt(pred,obj)/cnt(pred)*cnt(obj)) + ln(cnt(allTriple)/cnt(pred)) =
	 * 2*ln(cnt(allTriple)) + ln(cnt(pred,obj)) - 2*ln(cnt(pred)) - ln(cnt(obj))
	 * }
	 * </pre>
	 */
	
	@Override
   public Double compute(Triple t) {
		
//		System.out.println("\n===" + t + "===");
//		System.out.println("2a: " + db.getPredObjCnt(t.getPred(), t.getObj()));
//		System.out.println("4a: " + db.getObjCnt(t.getObj()));
//		System.out.println("---");
//		System.out.println("1: " + 2*(Math.log(db.getTripleCnt()) / Math.log(logBase)));
//		System.out.println("2: " + (Math.log(db.getPredObjCnt(t.getPred(), t.getObj())) / Math.log(logBase)));
//		System.out.println("3: " + 2*(Math.log(db.getPredCnt(t.getPred())) / Math.log(logBase)));
//		System.out.println("4: " + (Math.log(db.getObjCnt(t.getObj())) / Math.log(logBase)));
//		System.out.println("=====");
		
		Double w = null;
		try {
	      w = 2 * (Math.log(db.getTripleCnt()) / 								Math.log(logBase))
	            + (Math.log(db.getPredObjCnt(t.getPred(), t.getObj())) / Math.log(logBase)) 
	        - 2 * (Math.log(db.getPredCnt(t.getPred())) / 					Math.log(logBase))
	            - (Math.log(db.getObjCnt(t.getObj())) / 						Math.log(logBase));
      }
      catch (Exception e) {
	      log.warn("TripleWeighterPMIPlusIC failed for triple {} with {}", t, e.getMessage());
      	if (t.getPred().contains("dbpprop:website"))
      		return maxCostValue;
	      throw new RuntimeException(t + e.getMessage());
      }
		return transformToCost ? maxCostValue - w : w;
   }
	
	@Override
   public String toString() {
	   return "PMIPlusIC";
   }

	public static void main(String[] args) {
			
		Triple t1 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "foaf:Person");
		Triple t2 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "yago:NobelPeacePrizeLaureates");
		
		Triple t3 = new Triple("dbpedia:Jimmy_Carter", "dbpediaowl:spouse", "dbpedia:Rosalynn_Carter");
		Triple t4 = new Triple("dbpedia:Jimmy_Carter", "dbpediaowl:spouse", "dbpedia:Rosalynn_Carter");
		
		
		TripleWeighter weighter = new TripleWeighterPMIPlusIC(true);
		
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighter.compute(t1));
		
		System.out.print("TripleWeighterJointIC("+t2+") = ");
		System.out.println(weighter.compute(t2));
		
		System.out.print("TripleWeighterJointIC("+t3+") = ");
		System.out.println(weighter.compute(t3));
		
		System.out.print("TripleWeighterJointIC("+t4+") = ");
		System.out.println(weighter.compute(t4));
	}

}
