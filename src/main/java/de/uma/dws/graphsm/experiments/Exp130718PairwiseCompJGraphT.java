package de.uma.dws.graphsm.experiments;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.jgrapht.JGraphTImporter;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class Exp130718PairwiseCompJGraphT {
	
	final static Logger log = LoggerFactory.getLogger(Exp130718PairwiseCompJGraphT.class);
	
	public static void main (String[] args) {
		
		String neo4j = "/var/lib/neo4j/data/graph-leepincombe-2hops.db";
		String graphml = "output/graphml.xml";
		
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(neo4j, false);	
		Neo4jGraphUtils.exportGraphML(graph, graphml);
		graph.shutdown();
		
		Graph<String, DefaultEdge> jgrapht= JGraphTImporter.fromGraphMLFile(graphml);
		
		//TODO GraphComperator
	}

}
