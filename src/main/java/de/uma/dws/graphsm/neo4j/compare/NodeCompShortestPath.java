package de.uma.dws.graphsm.neo4j.compare;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.kernel.Traversal;

import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class NodeCompShortestPath {
	
	int  maxPathLen = 99;
	Neo4jRdfGraph graph = null;
	
	public NodeCompShortestPath(int maxPathLen, Neo4jRdfGraph graph) {
		this.maxPathLen = maxPathLen;
		this.graph = graph;
	}
	
	public String compute(String uri1, String uri2) {
		
		Node n1 = graph.getRawGraph().getNodeById( 
					(Long) graph.getVertices("uri", uri1).iterator().next().getId());
		Node n2 = graph.getRawGraph().getNodeById( 
					(Long) graph.getVertices("uri", uri2).iterator().next().getId());
		
		PathFinder<Path> spath = GraphAlgoFactory.shortestPath(
					(PathExpander<?>) Traversal.expanderForAllTypes(), 
					maxPathLen);
		
		Path shortestPath = spath.findSinglePath(n1, n2);
		
		if (shortestPath == null)
			return "" + Integer.MAX_VALUE;
		
		return "" + shortestPath.length();
	}

}
