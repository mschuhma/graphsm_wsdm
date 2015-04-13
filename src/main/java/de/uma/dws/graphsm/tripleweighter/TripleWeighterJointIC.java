package de.uma.dws.graphsm.tripleweighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.mysql.DBPediaCountsMySqlConnector;

/**
 * @author michaelschuhmacher
 *
 * Computes the joint information content IC(pred,obj) defined as IC(pred) + IC(obj|pred)
 * where
 * <pre>
 * {@code 
 * IC(Pred=pred) = -ln(P(Pred=pred)) = ln(cnt(allTriple)/cnt(pred))
 * IC(Obj=obj|Pred=pred) = -ln(P(Obj=obj|Pred=pred)) = ln(cnt(pred)/cnt(pred,obj))
 * }
 * </pre>
 * thus resulting into the IC of the joint probability
 * <pre>
 * {@code
 * IC(Obj=obj, Pred=pred) = -ln(P(Obj=obj, Pred=pred)) = ln(cnt(allTriple)/cnt(pred,obj))
 * }
 * </pre>
 * which is actually computed here.
 */
public class TripleWeighterJointIC implements TripleWeighter, TripleCostWeighter{

	final static Logger log = LoggerFactory.getLogger(TripleWeighterJointIC.class);
	final static DBPediaCountsMySqlConnector db = DBPediaCountsMySqlConnector.getInstance();
	
	final static Double 	logBase = Math.E;
	private boolean 		transformToCost;
	private Double 			maxCostValue = null;
	
	public TripleWeighterJointIC() {
		new TripleWeighterJointIC(false);
	}
	
	public TripleWeighterJointIC(boolean transformToCost) {
		this.transformToCost = transformToCost;
		//Compute highest possible Joint IC value as upper bound for cost
		maxCostValue = Math.log(1d * db.getTripleCnt() / 1d) / Math.log(logBase) + 0.00000001;
	}
	
	public Double getMaxCostValue() {
		return maxCostValue;
	}
	
	@Override
   public String toString() {
	   return "JointIC";
   }

	@Override
   public Double compute(Triple t) {
		
		Double w = null;
		
		try {
	      w = Math.log(1d * db.getTripleCnt() / db.getPredObjCnt(t.getPred(), t.getObj())) / Math.log(logBase); //base for log
      }
      catch (Exception e) {
      	log.warn("TripleWeighterJointIC failed for triple {} with {}", t, e.getMessage());
      	throw new RuntimeException(t + e.getMessage());
      }
		return transformToCost ? maxCostValue - w : w;
   }
	
	public static void main(String[] args) {
		
		Triple t1 = new Triple("dbpedia:Jimmy_Carter", "dbpediaowl:spouse", "dbpedia:Rosalynn_Carter");
		Triple t2 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "foaf:Person");
		Triple t3 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "yago:NobelPeacePrizeLaureates");
		
		TripleWeighter weighter = null;
		weighter = new TripleWeighterJointIC(false);
		
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighter.compute(t1));
		
		System.out.print("TripleWeighterJointIC("+t2+") = ");
		System.out.println(weighter.compute(t2));
		
		System.out.print("TripleWeighterJointIC("+t3+") = ");
		System.out.println(weighter.compute(t3));
		
		weighter = new TripleWeighterJointIC(true);
		
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighter.compute(t1));
		
		System.out.print("TripleWeighterJointIC("+t2+") = ");
		System.out.println(weighter.compute(t2));
		
		System.out.print("TripleWeighterJointIC("+t3+") = ");
		System.out.println(weighter.compute(t3));
	}

}
