package de.uma.dws.graphsm.neo4j.compare;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class DocCompShortestPathWrapper implements Callable<Tuple<String, Double>> {
	
	final static Logger log = LoggerFactory.getLogger(DocCompShortestPathWrapper.class);
	
	Neo4jRdfGraph graph;
	Snippet s1 = null;
	Snippet s2 = null;
	Double maxPathLen = null;

	public DocCompShortestPathWrapper(Neo4jRdfGraph graph, Snippet s1, Snippet s2, Double maxPathLen) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = maxPathLen;
	}

	@Override
	public Tuple<String, Double> call() throws Exception {
		
		DocCompShortestPath comp = new DocCompShortestPath(graph, s1, s2, maxPathLen);
		Double sim = comp.shortestPath();
		
		return new Tuple<String, Double>(s1.getSnippetId() + "," + s2.getSnippetId(), sim);
	}

}
