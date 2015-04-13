package de.uma.dws.graphsm.tripleweighter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Triple;

/**
 * @author michaelschuhmacher
 *
 * Sets all edge weights to 1.0
 */
public class TripleWeighterEqualWeights implements TripleWeighter, TripleCostWeighter {
	
	final static Logger log = LoggerFactory.getLogger(TripleWeighterEqualWeights.class);
	
	boolean transformToCost = false;
	private Double maxCostValue = 1.0;
	
	public TripleWeighterEqualWeights(boolean transformToCost) {
		this.transformToCost = transformToCost;
	}
	
	public Double getMaxCostValue() {
		return maxCostValue;
	}
	
	@Override
   public Double compute(Triple t) {
		return 1d;
   }
   	
	@Override
   public String toString() {
	   return "EqualWeights";
   }

	public static void main(String[] args) {
		
		Triple t1 = new Triple("dbpedia:Jimmy_Carter", "dbpediaowl:spouse", "dbpedia:Rosalynn_Carter");
		Triple t2 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "foaf:Person");
		Triple t3 = new Triple("dbpedia:Jimmy_Carter", "rdf:type", "yago:NobelPeacePrizeLaureates");
		
		TripleCostWeighter weighter = new TripleWeighterEqualWeights(true);
		
		System.out.println(weighter.getMaxCostValue());
		
		System.out.print("TripleWeighterJointIC("+t1+") = ");
		System.out.println(weighter.compute(t1));
		
		System.out.print("TripleWeighterJointIC("+t2+") = ");
		System.out.println(weighter.compute(t2));
		
		System.out.print("TripleWeighterJointIC("+t3+") = ");
		System.out.println(weighter.compute(t3));
		
	}

}
