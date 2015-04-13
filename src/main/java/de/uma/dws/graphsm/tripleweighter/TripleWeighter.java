package de.uma.dws.graphsm.tripleweighter;

import de.uma.dws.graphsm.datamodel.Triple;

public interface TripleWeighter {
	
	public Double compute(Triple triple);
	
	public String toString(); //Implement understandable description of weighting

}
