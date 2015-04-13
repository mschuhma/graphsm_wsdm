package de.uma.dws.graphsm.neo4j;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobustWeightEvaluator implements CostEvaluator<Double> {
	
    private static final String COSTPROPNAME = "weight";
    final static Logger log = LoggerFactory.getLogger(RobustWeightEvaluator.class);

	public RobustWeightEvaluator() {
	    super();
	}

	@Override
	public Double getCost(Relationship relationship, Direction direction) {
        
		Object costProp = relationship.getProperty(COSTPROPNAME);
        
		Double d;
        try {    	
	        if(costProp instanceof Double) {
	            d = (Double)costProp;
	        } 
	        else if (costProp instanceof Integer) {
	            d = (double)(Integer)costProp;
	        } 
	        else {
	            d = Double.parseDouble(costProp.toString());
	        }
        }
	    catch (Exception e) {
	    	 log.warn("Weight extraction error for {}: {}", relationship.getNodes(), e);
	    	 return Double.MAX_VALUE;
	    }
        
        if (d.isNaN())
        	return Double.MAX_VALUE;
        
        if (d.isInfinite() | d <= 0) {
            log.debug("Rel: " + relationship.getId() +" ");
            log.debug("StartNode " + relationship.getStartNode().getId() +" ");
            log.debug("EndNode " + relationship.getEndNode().getId() +" ");
            log.debug("Value " + d);
        	return Double.MAX_VALUE;
        }
//        TODO make shift value (20d) dynamic via
//        SELECT MAX( PropIC.IC + PropObjIC.IC ) AS icsum
//        FROM PropObjIC, PropIC
//        WHERE PropIC.prop = PropObjIC.prop
        d = 20.483751785654782 + -1d*d;
        if (d.isInfinite() | d.isNaN() | d < 0) {
        	log.warn("Weight {}, Fatal weighting error: {}", d, new ArithmeticException("Invalide edge weights computed."));
        }
        	
        return d; 
	}
}
