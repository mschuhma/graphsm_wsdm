package de.uma.dws.graphsm.jgrapht;

import org.jgrapht.Graph;

/**
 * Compares two {@link Graph}s by computing their similarity
 * 
 * @author ponzetto
 *
 */
public interface GraphComparator<V,T> {
	
	double computeSimilarity(Graph<V, T> g1, Graph<V, T> g2);

}
