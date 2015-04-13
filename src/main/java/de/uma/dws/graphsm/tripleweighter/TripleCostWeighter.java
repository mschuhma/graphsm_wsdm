package de.uma.dws.graphsm.tripleweighter;

import de.uma.dws.graphsm.datamodel.Triple;

public interface TripleCostWeighter {
	
	public Double compute(Triple triple);
	
	public Double getMaxCostValue();
	
	public String toString(); //Implement understandable description of weighting

}
