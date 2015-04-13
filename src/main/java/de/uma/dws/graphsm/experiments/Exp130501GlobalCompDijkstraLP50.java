package de.uma.dws.graphsm.experiments;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.neo4j.compare.DocCompDijkstraWrapper;

public class Exp130501GlobalCompDijkstraLP50 {

	public final static Logger log = LoggerFactory
			.getLogger(Exp130501GlobalCompDijkstraLP50.class);
	public final static Configuration conf = ConfFactory.getConf();

	public static void main(String[] args) {

		System.out
				.println("REMINDER: Have you set the mysql cache tables in conf file correctly for this run?");
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long start = System.currentTimeMillis();
		String graphDir = args[0];
		Double hops = (args.length == 2) ? Double.parseDouble(args[1])
				: 999999d;

		// runLeePincombeHungarian(graphDir, hops);
		// // runLeePincombe(graphDir, hops);
		System.out
				.println("Processing time "
						+ (System.currentTimeMillis() - start) / (1000 * 60)
						+ " mins.");
	}

	public static void runLeePincombe(String graphDir, Double maxPathLen) {
		System.out.println("MainRunParallel in de.uma.dws.graphsm.neo4j.compare now running...");
		log.info("Parameters: graphDir={} maxPathLen={}", graphDir, maxPathLen);
		System.out.println("Check log files in /log");
		
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);
		
	    ExecutorService pool = Executors.newFixedThreadPool(conf.getInt("system.parallel.threads", 1));
	    ArrayList<Future<Tuple<String, Double>>> simScoreResults = new ArrayList<Future<Tuple<String, Double>>>();
	    
	    AtomicLong globalMaxPathCost = new AtomicLong(0);
		
		try {
			for (int i=1; i<=50; i++) {
				for (int j=i+1; j<=50; j++) {
					log.debug("Compute similarity between snippet {} and {} with dijkstra on weighted graph", i, j);
					
					simScoreResults.add(
							pool.submit(new DocCompDijkstraWrapper(
									graph, 
									new Snippet(0,i,"http://dummy","dummy","dummy"), 
									new Snippet(0,j,"http://dummy","dummy","dummy"),
									maxPathLen,
									globalMaxPathCost)
							)
					);
				}
			}
			
			for (Future<Tuple<String, Double>> simScore : simScoreResults) {
				log.info("PathLength \t {}", simScore.get());
			}
			
			log.info("globalMaxPathCost = {}", Double.longBitsToDouble(globalMaxPathCost.get()));
			System.out.println("MainRunParallel threads finished. Shutting down database and thread pool.");
			
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("{} {}",e.getMessage());
			
		} finally {
			graph.shutdown();
			pool.shutdown();
			System.out.println("MainRunParallel shutdown finished.");
		}
	
	}

}
