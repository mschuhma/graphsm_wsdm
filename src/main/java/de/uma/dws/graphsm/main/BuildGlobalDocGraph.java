package de.uma.dws.graphsm.main;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.neo4j.DBPediaDocCollectionGraphBuilder;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterGlobalPredObjIC;
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class BuildGlobalDocGraph {

	final static Logger	log	= LoggerFactory.getLogger(BuildGlobalDocGraph.class);

	/**
	 * Build new neo4j graph (disk-based) from existing dbpedia concepts document
	 * representation
	 * 
	 * @param dbpConceptsFile
	 *          path of file with dbpedia concepts, one document per line, all
	 *          concepts of each doc separated by tab
	 * @param dbdir
	 *          path of for neo4j database dir (to be created, has be be non
	 *          existing)
	 * @param hopsLimit
	 *          number of expansion hops for graph, default should be 2
	 */
	public static Neo4jRdfGraph fromCorpusDBpediaConceptsFile(String dbpConceptsFile,
	    String dbdir, int hopsLimit) {
		log.info("Build new graph from text corpora file");

		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(dbdir, false);

		DBPediaDocCollectionGraphBuilder graphBuilder = new DBPediaDocCollectionGraphBuilder(
		    graph);
		graphBuilder.setEdgeWeight(new TripleWeighterGlobalPredObjIC());

		File f = new File(dbpConceptsFile);

		int cnt = 0;
		for (ArrayList<String> nodes : MyFileReader.readXSVFile(f, "\t", false)) {
			cnt++;
			log.info("Creating DBPedia graph for document {}.", cnt);
			graphBuilder.addExternalSourceNodes(nodes);
			graphBuilder.addExpandedNetwork(new DBPediaAllOutgoing(), hopsLimit);
		}
		return graph;
	}

	/**
	 * Build new neo4j graph (disk-based) from document collection (NL text)
	 * 
	 * @param docsFile
	 *          path of file with document text documents, one document per line
	 * @param dbdir
	 *          path of for neo4j database dir (to be created, has be be non
	 *          existing)
	 * @param hopsLimit
	 *          number of expansion hops for graph, default should be 2
	 */
	public static Neo4jRdfGraph fromDocsTextFile(String docsFile, String dbdir,
	    int hopsLimit) {
		log.info("Build new graph from dbpedia based on spotlight-annotated snippet");

		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(dbdir, false);

		DBPediaDocCollectionGraphBuilder graphBuilder = new DBPediaDocCollectionGraphBuilder(
		    graph);
		graphBuilder.setEdgeWeight(new TripleWeighterGlobalPredObjIC());

		ArrayList<String> docs = MyFileReader.readLinesFile(new File(docsFile));

		int cnt = 0;
		for (String doc : docs) {
			cnt++;
			log.info("Creating DBPedia graph for document {}.", cnt);
			graphBuilder.addSourceNodes(doc);
			graphBuilder.addExpandedNetwork(new DBPediaAllOutgoing(), hopsLimit);
		}
		return graph;
	}

	public static void main(String[] args) {

		int option = 2;

		Neo4jRdfGraph graph;

		switch (option) {

		case 1:
			System.out
			    .println("Creating graph from already existing dbpedia concepts by fromCorpusDBpediaConceptsFile()");

			final String NewsObamaSpotlight = "src/main/resources/dataset/NewsObamaBerlin/newspaper_all_spotlight.txt";
			final String graphDirObama = "/var/lib/neo4j/data/DELET-graph-leepincombe-2hops.db";

			graph = fromCorpusDBpediaConceptsFile(NewsObamaSpotlight, graphDirObama, 2);
			System.out.println("Creating network finished");
			// Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);
			Neo4jGraphUtils.removeDeadEndNodes(graph, true);

			graph.shutdown();
			break;

		case 2:
			System.out
			    .println("Creating neo4j graph from text documents by fromDocsTextFile()");

			final String LP50 = "src/main/resources/dataset/LeePincombe/documents.cleaned.data";
			final String graphDirLP = "/var/lib/neo4j/data/DELET-graph-leepincombe-2hops.db";

			graph = fromDocsTextFile(LP50, graphDirLP, 2);
			System.out.println("Creating network finished");
			// graph = Neo4jRdfGraph.getInstance(graphDir, false);
			Neo4jGraphUtils.removeDeadEndNodes(graph, true);

			graph.shutdown();
			break;

		}

	}

}
