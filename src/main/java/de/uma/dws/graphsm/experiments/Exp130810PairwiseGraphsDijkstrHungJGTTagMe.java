package de.uma.dws.graphsm.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.jgrapht.DijkstraParallel;
import de.uma.dws.graphsm.main.BuildSeparateDocGraphs;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleCostWeighter;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterAddedIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterEqualWeights;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterJointIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterPMIPlusIC;
import de.uma.dws.graphsm.webservice.Annotator;
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;
import de.uma.dws.graphsm.webservice.DBPediaSpotlight;
import de.uma.dws.graphsm.webservice.TagMe;

public class Exp130810PairwiseGraphsDijkstrHungJGTTagMe {

	public final static Logger	       log	= LoggerFactory.getLogger(Exp130810PairwiseGraphsDijkstrHungJGTTagMe.class);
	public final static Configuration	conf	= ConfFactory.getConf();
	
	final static String LP50_TEXT_SAMPLE 			= "src/main/resources/dataset/LeePincombe/documents.development.data";
	final static String LP50_TEXT 					= "src/main/resources/dataset/LeePincombe/documents.cleaned.data";	
	final static String LP50_NEO4J_GRAPH 			= "neo4j/LP50_2Hops_Spotlight/";//"neo4j/LP50_2Hops_TagMe/";
	final static String OUTPUT						= "output/LP50_2Hops_Spotlight_New/";
	
	//Extrinsic Parameters
   static Double  	PATH_COST_FACTOR	= Double.NaN;
   static int     	MAX_PATH_LENGTH 	= 2;
   
   //Computed Parameters
   static Double MAX_EDGE_COST = Double.NaN;
   static Double MAX_PATH_COST = Double.MAX_VALUE; 
//   			MAX_PATH_LENGTH * MAX_EDGE_COST * PATH_COST_FACTOR;
   
   final static TripleCostWeighter weigher = new TripleWeighterEqualWeights(true);

   
	public static void main(String[] args) {

		String input = LP50_TEXT;
		String graphdir = LP50_NEO4J_GRAPH;
		
//		build(input, graphdir);
		
		runExperiment();

	}
	
	public static void runExperiment() {
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("Exp130801OneGraphOneDocDijkstraHungarianJGT running for LeePincombe dataset");   	
		
//		Load graph from disk
		ArrayList<Neo4jRdfGraph> graphs = new ArrayList<>();
		for (int i=0; i< 50; i++) {
			if (true) { //i == 14 || i == 18
				graphs.add(Neo4jRdfGraph.getInstance(LP50_NEO4J_GRAPH + "doc-"+i+".db", false));
			}
			else {
				graphs.add(null);
			}		
		}
		
		for (Neo4jRdfGraph graph : graphs)  {
			Neo4jGraphUtils.updateAllEdgeWeights(graph, (TripleWeighter) weigher,  "cost");
			graph.commit();
		}
		
		System.out.println("Loading networks from disk finished");
		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");

		for (int i = 1; i <= 4; i++) {
			
			MAX_PATH_LENGTH = i;
			MAX_PATH_COST = Double.MAX_VALUE; //weigher.getMaxCostValue() * MAX_PATH_LENGTH;
			
			try {
				runPaths(OUTPUT + weigher.toString() + "-steps-" + MAX_PATH_LENGTH, graphs);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		
		for (Neo4jRdfGraph graph : graphs)
			if (graph != null)
				graph.shutdown();
		
		System.out.println("Exp130801OneGraphOneDocDijkstraHungarianJGT terminated successfully");
		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");
	}
 
   
   public static void runPaths(String resultDir, ArrayList<Neo4jRdfGraph> graphs) 
   			throws IOException {
   	
   	long startTime = System.currentTimeMillis();
   	
		String output = resultDir + ".txt";
   	
		System.out.println("Exp130801OneGraphOneDocDijkstraHungarianJGT running for LeePincombe dataset");   	

//		Compute weights (reminder: dont remove dead end nodes)
//		for (Neo4jRdfGraph graph : graphs)  {
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterJointIC(true),  "cost");
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterAddedIC(true),   "cost");
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterPMIPlusIC(true), "cost");
//			graph.commit();
//		}
//		
//		System.out.println("(Re)computing edge weights finished");
		
		
		//Run parallel Dijkstra with Hungarian 1:1 Mapping
		System.out.println("Running Dijkstra with" +
				" MAX_PATH_COST " + MAX_PATH_COST + 
				" (MAX_PATH_LENGTH " + MAX_PATH_LENGTH +
				" PATH_COST_FACTOR " + PATH_COST_FACTOR + 
				" MAX_EDGE_COST " + MAX_EDGE_COST + ")");
		
		ExecutorService pool = Executors.newFixedThreadPool(
					conf.getInt("system.parallel.threads", 1));
		
		ArrayList<Future<Tuple<String,Double>>> docSimResults = 
					new ArrayList<Future<Tuple<String,Double>>>();
		
		FileWriter pathWriter = new FileWriter(resultDir + "-all-path.txt");
		
		for (int i=0; i < graphs.size(); i++) {
			for (int j=i+1; j < graphs.size(); j++) {
//				if (i > 10 | j > 10)
//					continue;
				docSimResults.add(pool.submit(
										new DijkstraParallel(
													graphs.get(i),
													graphs.get(j),
													MAX_PATH_LENGTH,
													MAX_PATH_COST,
													pathWriter)));
				}
		}
		
		pool.shutdown();
		
		FileWriter w = new FileWriter(output);

		for (Future<Tuple<String,Double>> docSim : docSimResults) {
			Tuple<String, Double> t = null;
         try {
	         t = docSim.get(2, TimeUnit.HOURS);
	         log.info("{} ; {}", t.k, t.v);
	         w.write(t.k + "\t" + t.v + "\n");
         }
         catch (InterruptedException | ExecutionException | TimeoutException | IOException e) {
	         e.printStackTrace();
         }
		}
		
		w.close();
		pathWriter.close();
		
		System.out.println("Results written to ouput file " + output);
		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");
		
		pool.shutdown();

	}
   
	public static ArrayList<Neo4jRdfGraph> build(String input, String graphdir) {

		System.out.println("Creating network from dbpedia with" + " input " + input + " graph output " + graphdir);

		ArrayList<Neo4jRdfGraph> graphs = BuildSeparateDocGraphs.fromDocsTextFile(
					input, 
					graphdir, 
					2, 
					new DBPediaSpotlight(),
		         new DBPediaAllOutgoing(), 
		         null);// new TripleWeighterJointIC());

		System.out.println("Creating networks from dbpedia finished");

//		for (Neo4jRdfGraph graph : graphs) {
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterAddedIC(true), "cost");
//			graph.shutdown();
//		}
		
		return graphs;
	}
}
