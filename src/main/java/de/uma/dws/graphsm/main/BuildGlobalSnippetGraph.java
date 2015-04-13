package de.uma.dws.graphsm.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.datamodel.RunMetadata;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.neo4j.DBPediaDocCollectionGraphBuilder;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterGlobalPredObjIC;
import de.uma.dws.graphsm.webservice.DBPediaAllIngoingOutgoing;

public class BuildGlobalSnippetGraph {

	final static Logger	log	= LoggerFactory.getLogger(BuildGlobalSnippetGraph.class);

	/**
	 * Build neo4j graph from dbpedia graph based on spotlight annotated snippet
	 * text
	 */
	public static Neo4jRdfGraph buildGraphFromSnippets(String dbdir, int hopsLimit) {
		log.info("Build new graph from dbpedia based on spotlight-annotated snippet");

		RunMetadata devRun = new RunMetadata();

		// Skipping: snippet text -> snippet_bow processing (not yet implemented)
		// Loading cleaned snippet bag of words for development directly from file
		// now
		devRun.loadDevQuery();

		// Neo4jRdfGraph graph = Neo4jRdfGraph.getInstanceDefaultDatabase();
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(dbdir, false);

		DBPediaDocCollectionGraphBuilder graphBuilder = 
				new DBPediaDocCollectionGraphBuilder(graph);
		graphBuilder.setEdgeWeight(new TripleWeighterGlobalPredObjIC());

		for (Snippet s : devRun.getQuery(0).getSnippets()) {
			log.info("Creating DBPedia graph for snippet {}.{}: {}", s.getQueryId(),
			    s.getSnippetId(), s.getTextBowAsString());
			graphBuilder.addSourceNodes(s);
			graphBuilder.addExpandedNetwork(new DBPediaAllIngoingOutgoing(), hopsLimit);
		}
		return graph;
	}

	public static void main(String[] args) {

		final String graphDir = "/var/lib/neo4j/data/DELETE-graph-2hops-binary-ic.db";

		// Build new graph from snippets
		Neo4jRdfGraph graph = buildGraphFromSnippets(graphDir, 2);

		// Load existing graph from file system
		// Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);

		Neo4jGraphUtils.removeDeadEndNodes(graph, true);
		graph.shutdown();
	}

}
