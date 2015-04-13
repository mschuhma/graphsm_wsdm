package de.uma.dws.graphsm.main;

import java.io.File;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.neo4j.DBPediaSingleDocGraphBuilder;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterGlobalPredObjIC;
import de.uma.dws.graphsm.webservice.Annotator;
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;
import de.uma.dws.graphsm.webservice.DBPediaEdgeSelector;
import de.uma.dws.graphsm.webservice.DBPediaSpotlight;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class BuildSeparateDocGraphs {

	final static Logger	log	= LoggerFactory.getLogger(BuildSeparateDocGraphs.class);

	/**
	 * Build one new neo4j graph (disk-based) for each document from doc collection file (NL text)
	 * 
	 * @param docsFile
	 *           path of file with text documents, one document per line
	 * @param dbdir
	 *           path in which each new neo4j database will be created (has be empty)
	 * @param hopsLimit
	 *           number of expansion hops for graph, default should be 2
	 */
	
	
	public static ArrayList<Neo4jRdfGraph> fromDocsTextFile(
				String docsFile, 
				String dbdir, 
				int hopsLimit) {
		
		return fromDocsTextFile(
					docsFile, 
					dbdir, 
					hopsLimit, 
					new DBPediaSpotlight(), 
					new DBPediaAllOutgoing(), 
					new TripleWeighterGlobalPredObjIC());
	}
	
	@Deprecated
	public static ArrayList<Neo4jRdfGraph> fromDocsTextFile(
				String docsFile, 
				String dbdir, 
				int hopsLimit,
				DBPediaEdgeSelector edgeSelector,
				TripleWeighter tripleWeighter){
		
		return fromDocsTextFile(
					docsFile, 
					dbdir, 
					hopsLimit, 
					new DBPediaSpotlight(), 
					edgeSelector, 
					tripleWeighter);
	}
	
	
	public static ArrayList<Neo4jRdfGraph> fromDocsTextFile(
				String docsFile, 
				String dbdir, 
				int hopsLimit,
				Annotator annotator,
				DBPediaEdgeSelector edgeSelector,
				TripleWeighter tripleWeighter) {

		File baseDir = new File(dbdir);
		if (baseDir.isDirectory())
			if (baseDir.list().length > 0)
				log.warn("Output directory {} is not empty, keeping existing graphs.", baseDir.getAbsolutePath());

		ArrayList<String> docs = MyFileReader.readLinesFile(new File(docsFile));
		ArrayList<Neo4jRdfGraph> graphs = new ArrayList<Neo4jRdfGraph>();
		
		log.info("Start building {} doc graphs from text corpora file", docs.size());

		for (int cnt = 0; cnt < docs.size(); cnt++) {

			String graphDir = dbdir + "doc-" + cnt + ".db/";
			File graphDirFile = new File(graphDir); 
			
			if (graphDirFile.exists()) {
				log.warn("Skipping document {}, directory remains unchaineged {}", cnt, graphDirFile.getAbsolutePath());
				graphs.add(Neo4jRdfGraph.getInstance(graphDir, false));
				continue;
			}

			Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);

			DBPediaSingleDocGraphBuilder graphBuilder = 
						new DBPediaSingleDocGraphBuilder(graph, tripleWeighter, annotator);

			log.info("Creating DBPedia graph in {} for doc {} ", graphDir, cnt);
			
			graphBuilder.addSourceNodes(docs.get(cnt));
			graphBuilder.addExpandedNetwork(edgeSelector, hopsLimit);

			graphs.add(graph);

		}
		return graphs;
	}

	public static void main(String[] args) {

		int option = 2;

		// Neo4jRdfGraph graph;

		switch (option) {

		case 1:
			// System.out.println("Creating graph from already existing dbpedia concepts by fromCorpusDBpediaConceptsFile()");
			//
			// final String NewsObamaSpotlight = "src/main/resources/dataset/NewsObamaBerlin/newspaper_all_spotlight.txt";
			// final String graphDirObama = "/var/lib/neo4j/data/DELET-graph-leepincombe-2hops.db";
			//
			// graph = fromCorpusDBpediaConceptsFile(NewsObamaSpotlight, graphDirObama, 2);
			// System.out.println("Creating network finished");
			// // Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);
			// Neo4jGraphUtils.removeDeadEndNodes(graph, true);
			//
			// graph.shutdown();
			break;

		case 2:
			System.out.println("Creating neo4j graph from text documents by fromDocsTextFile()");

			final String LP50 = "src/main/resources/dataset/LeePincombe/documents.cleaned.data";
			final String graphDirLP = "neo4j/graphs-leepincombe-2hops/";

			ArrayList<Neo4jRdfGraph> graphs = fromDocsTextFile(LP50, graphDirLP, 2);
			System.out.println("Creating network finished");

			for (Neo4jRdfGraph graph : graphs) {
				System.out.println(graph);
				graph.shutdown();
			}

			break;

		}

	}

}
