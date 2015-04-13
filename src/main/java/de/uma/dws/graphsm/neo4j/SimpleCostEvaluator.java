package de.uma.dws.graphsm.neo4j;

import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCostEvaluator implements CostEvaluator<Double> {
	
    private static final String COSTPROPNAME = "cost";
    final static Logger log = LoggerFactory.getLogger(SimpleCostEvaluator.class);

	public SimpleCostEvaluator() {
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
        
        if (d.isInfinite() | d < -0d) {
            log.debug("Rel: " + relationship.getId() +" ");
            log.debug("StartNode " + relationship.getStartNode().getId() +" ");
            log.debug("EndNode " + relationship.getEndNode().getId() +" ");
            log.debug("Value " + d);
        	return Double.MAX_VALUE;
        }
        	
        return d; 
	}
}
