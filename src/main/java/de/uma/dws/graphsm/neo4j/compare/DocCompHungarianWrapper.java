package de.uma.dws.graphsm.neo4j.compare;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.DistMatrix;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class DocCompHungarianWrapper implements Callable<DistMatrix<Snippet, Snippet, Double[][]>> {
	
	final static Logger log = LoggerFactory.getLogger(DocCompHungarianWrapper.class);
	
	Neo4jRdfGraph graph;
	Snippet s1 = null;
	Snippet s2 = null;
	Double maxPathLen = null;
	AtomicLong globalMaxPathCost = null;

	public DocCompHungarianWrapper(Neo4jRdfGraph graph, Snippet s1, Snippet s2, Double maxPathLen, AtomicLong globalMaxPathCost) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = maxPathLen;
		this.globalMaxPathCost = globalMaxPathCost;
	}

	@Override
	public DistMatrix<Snippet, Snippet, Double[][]> call() throws Exception {
		
		//Dijkstra-based
		DocCompDijkstraHungarian comp = new DocCompDijkstraHungarian(graph, s1, s2, maxPathLen, globalMaxPathCost);
		Double[][] distMatrix = comp.dijkstra();
		log.debug("Compute similarity with DocCompDijkstraHungarian between snippet {} and {}", s1.getSnippetId(), s2.getSnippetId());
		
		//ShortestPath-based
//		DocCompShortestPathHungarian comp = new DocCompShortestPathHungarian(graph, s1, s2, maxPathLen);
//		Double[][] distMatrix = comp.shortestPath();
//		globalMaxPathCost.set(Double.doubleToLongBits(maxPathLen));
//		log.debug("Compute similarity with DocCompShortestPathHungarian between snippet {} and {}", s1.getSnippetId(), s2.getSnippetId());
		
		return new DistMatrix<Snippet, Snippet, Double[][]>(s1, s2, distMatrix);
	}

}
