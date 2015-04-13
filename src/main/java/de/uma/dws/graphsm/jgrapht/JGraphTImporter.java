package de.uma.dws.graphsm.jgrapht;

import java.io.File;
import java.util.HashMap;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.ClassBasedVertexFactory;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.aaue.sna.ext.graphml.GraphMLImporter;

public class JGraphTImporter {
	
	final static Logger log = LoggerFactory.getLogger(JGraphTImporter.class);

	public static Graph<String, DefaultEdge> fromGraphMLFile(String inputFile) {

		Graph<String, DefaultEdge> graph = new DefaultDirectedWeightedGraph
				<String, DefaultEdge>(DefaultEdge.class);
		VertexFactory<String> vFactory = new ClassBasedVertexFactory<String>(
				String.class);
		
		GraphMLImporter<String, DefaultEdge> importer = GraphMLImporter
				.createFromFile(new File(inputFile));
		importer.useNodeIDAsNode();
		importer.generateGraph(graph, vFactory, new HashMap<String, String>());
		
		log.info("GraphML file successfully imported with {} nodes and {} edges.",
				graph.vertexSet().size(), graph.edgeSet().size());
		
		return graph;
	}

	public static void main(String[] args) {
		
//		String neo4j = "/home/mschuhma/graph-leepincombe-2hops.db";
//				
//		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(neo4j, false);	
//		Neo4jGraphUtils.exportGraphML(graph, graphml);
//		graph.shutdown();
		
		String graphml = "output/graphmlfix.xml";
		
		Graph<String, DefaultEdge> jgrapht= JGraphTImporter.fromGraphMLFile(graphml);
		
		for (String vertex : jgrapht.vertexSet()) {
			System.out.println(vertex);
		}

	}

}
