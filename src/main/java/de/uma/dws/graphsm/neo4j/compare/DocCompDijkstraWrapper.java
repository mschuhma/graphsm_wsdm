package de.uma.dws.graphsm.neo4j.compare;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class DocCompDijkstraWrapper implements Callable<Tuple<String, Double>> {
	
	final static Logger log = LoggerFactory.getLogger(DocCompDijkstraWrapper.class);
	
	Neo4jRdfGraph graph;
	Snippet s1 = null;
	Snippet s2 = null;
	Double maxPathLen = null;
	AtomicLong globalMaxPathCost = null;

	public DocCompDijkstraWrapper(Neo4jRdfGraph graph, Snippet s1, Snippet s2, Double maxPathLen, AtomicLong globalMaxPathCost) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = maxPathLen;
		this.globalMaxPathCost = globalMaxPathCost;
	}

	@Override
	public Tuple<String, Double> call() throws Exception {
		
		DocCompDijkstra comp = new DocCompDijkstra(graph, s1, s2, maxPathLen, globalMaxPathCost);
		Double sim = comp.dijkstra();
		
		return new Tuple<String, Double>(s1.getSnippetId() + "," + s2.getSnippetId(), sim);
	}

}
