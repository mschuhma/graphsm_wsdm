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
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterAddedIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterJointIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterPMIPlusIC;

public class Exp130801OneGraphOneDocDijkstraHungarianJGT {

	public final static Logger	       log	= LoggerFactory.getLogger(Exp130801OneGraphOneDocDijkstraHungarianJGT.class);
	public final static Configuration	conf	= ConfFactory.getConf();
	
	final static String LP50_TEXT_SAMPLE 			= "src/main/resources/dataset/LeePincombe/documents.development.data";
	final static String LP50_TEXT 					= "src/main/resources/dataset/LeePincombe/documents.cleaned.data";	
	final static String LP50_NEO4J_GRAPH 			= "neo4j/LP50_2Hops/";
	
	//Extrinsic Parameters
   static Double  	PATH_COST_FACTOR	= 0.7;
   static int     	MAX_PATH_LENGTH 	= 2;
   
   //Computed Parameters
   static Double MAX_EDGE_COST = Math.log(69250920/1);
   static Double MAX_PATH_COST = 
   			MAX_PATH_LENGTH * MAX_EDGE_COST * PATH_COST_FACTOR;

   
	public static void main(String[] args) {

		if (args.length == 2) {
			String in = args[0].trim();
			String out = args[1].trim();
			System.out.println("Using console input params. input " + in + " output " + out);
		}
		
		else {
	
			ArrayList<Neo4jRdfGraph> graphs = new ArrayList<>();
			for (int i=0; i< 50; i++) {
				if (true) { //i == 14 || i == 18
					graphs.add(Neo4jRdfGraph.getInstance(LP50_NEO4J_GRAPH + "doc-"+i+".db", false));
				}
				else {
					graphs.add(null);
				}		
			}
			
			
			for (int i = 1; i <= 9; i++) {
				PATH_COST_FACTOR = i / 10d;
				MAX_PATH_COST = MAX_PATH_LENGTH * MAX_EDGE_COST * PATH_COST_FACTOR;
				try {
					run(LP50_NEO4J_GRAPH, "output/LP50_2Hops/JointIC-costfactor-"+PATH_COST_FACTOR, graphs);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			for (Neo4jRdfGraph graph : graphs)
				if (graph != null)
					graph.shutdown();
		}
	}
 
   
   public static void run(String graphDirLP, String resultDir, ArrayList<Neo4jRdfGraph> graphs) throws IOException {
   	
   	long startTime = System.currentTimeMillis();
   	
		String output = resultDir + "current_output.txt";
   	
		System.out.println("Exp130801OneGraphOneDocDijkstraHungarianJGT running for LeePincombe dataset");   	
   	
//		Option A) Build graph from input
//		System.out.println("Creating network from dbpedia with" +
//					" input " + input + 
//					" output " + output +
//					" graphDirLP " + graphDirLP);
//		ArrayList<Neo4jRdfGraph> graphs = BuildSeparateDocGraphs.
//					fromDocsTextFile(
//								input, graphDirLP, 
//								3, 
//								new DBPediaAllOutgoing(), 
//								new TripleWeighterJointIC()); //set null for not adding weights and do it later below
//		
//		System.out.println("Creating networks from dbpedia finished");
		
//		Option B Load graph from disk
//		ArrayList<Neo4jRdfGraph> graphs = new ArrayList<>();
//		for (int i=0; i< 50; i++) {
//			if (true) { //i == 14 || i == 18
//				graphs.add(Neo4jRdfGraph.getInstance(graphDirLP + "doc-"+i+".db", false));
//			}
//			else {
//				graphs.add(null);
//			}		
//		}
		
//		System.out.println("Loading networks from disk finished");
//		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");

//		Compute weights (reminder: dont remove dead end nodes)
//		for (Neo4jRdfGraph graph : graphs)  {
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterJointIC(true),  "cost");
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterAddedIC(true),   "cost");
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterPMIPlusIC(true), "cost");
//			graph.commit();
//		}
		
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
		
		for (int i=0; i < graphs.size(); i++) {
			for (int j=i+1; j < graphs.size(); j++) {
//				if (i > 10 | j > 10)
//					continue;
				docSimResults.add(pool.submit(
										new DijkstraParallel(
													graphs.get(i),
													graphs.get(j),
													MAX_PATH_LENGTH,
													MAX_PATH_COST)));
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
		System.out.println("Results written to ouput file " + output);
		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");
		
		pool.shutdown();
		
		System.out.println("Exp130801OneGraphOneDocDijkstraHungarianJGT terminated successfully");
		System.out.println("Processing time " + (System.currentTimeMillis() - startTime) / (1000d * 60d) + " mins.");

	}
}
