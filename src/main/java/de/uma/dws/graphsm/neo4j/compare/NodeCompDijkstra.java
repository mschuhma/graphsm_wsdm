package de.uma.dws.graphsm.neo4j.compare;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.kernel.Traversal;

import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.neo4j.SimpleCostEvaluator;

public class NodeCompDijkstra {
	
	int  maxPathLen = 99;
	Neo4jRdfGraph graph = null;
	
	public NodeCompDijkstra(int maxPathLen, Neo4jRdfGraph graph) {
		this.maxPathLen = maxPathLen;
		this.graph = graph;
	}
	
	public String compute(String uri1, String uri2) {
		
		Node n1 = graph.getRawGraph().getNodeById( 
					(Long) graph.getVertices("uri", uri1).iterator().next().getId());
		Node n2 = graph.getRawGraph().getNodeById( 
					(Long) graph.getVertices("uri", uri2).iterator().next().getId());
		
		PathFinder<WeightedPath> dijkstra = GraphAlgoFactory.dijkstra(
					(PathExpander<?>) Traversal.expanderForAllTypes(),
					new SimpleCostEvaluator());
		
		WeightedPath cheapestPath = dijkstra.findSinglePath(n1, n2);
		
		if (cheapestPath == null)
			return Double.MAX_VALUE + "\t" + Integer.MAX_VALUE;
		
		return cheapestPath.weight() + "\t" + cheapestPath.length();
	}

}
