package de.uma.dws.graphsm.experiments;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.DistMatrix;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.neo4j.compare.DocCompHungarianWrapper;
import de.unima.alcomox.algorithms.HungarianMethod;

public class Exp130601GlobalCompHungaDijkstraLP50 {
	
	public final static Logger log = LoggerFactory.getLogger(Exp130601GlobalCompHungaDijkstraLP50.class);
	public final static Configuration conf = ConfFactory.getConf();
	
	public static void main(String[] args) {
		
		System.out.println("REMINDER: Have you set the mysql cache tables in conf file correctly for this run?");
		try {Thread.sleep(10*1000);} catch (InterruptedException e) {e.printStackTrace();}
		
		long start = System.currentTimeMillis();
		String graphDir = args[0];
		Double hops = (args.length == 2) ? Double.parseDouble(args[1]) : 999999d;
		
		runLeePincombeHungarian(graphDir, hops);
//		runLeePincombe(graphDir, hops);
		System.out.println("Processing time " + (System.currentTimeMillis() - start)/(1000*60) + " mins.");
	}

	public static void runLeePincombeHungarian(String graphDir, Double maxPathLen) {
		System.out.println("MainRunParallel in de.uma.dws.graphsm.neo4j.compare now running...");
		log.info("Parameters: graphDir={} maxPathLen={}", graphDir, maxPathLen);
		System.out.println("Check log files in /log");
		
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);
		
	    ExecutorService pool = Executors.newFixedThreadPool(conf.getInt("system.parallel.threads", 1));
	    
	    ArrayList<Future<DistMatrix<Snippet, Snippet, Double[][]>>> resultsList = 
	    		new ArrayList<Future<DistMatrix<Snippet, Snippet, Double[][]>>>();
	    
	    AtomicLong globalMaxPathCost = new AtomicLong(0);
	    
		
		try {
			for (int i=1; i<=50; i++) {
				for (int j=i+1; j<=50; j++) {
					
					resultsList.add(
							pool.submit(new DocCompHungarianWrapper(
									graph, 
									new Snippet(0,i,"http://dummy","dummy","dummy"), 
									new Snippet(0,j,"http://dummy","dummy","dummy"),
									maxPathLen,
									globalMaxPathCost)
							)
					);
				}
			}
			
			ArrayList<DistMatrix<Snippet, Snippet, Double[][]>> distMatrixList = 
					new ArrayList<DistMatrix<Snippet, Snippet, Double[][]>>();
			
			for (Future<DistMatrix<Snippet, Snippet, Double[][]>> res : resultsList) {
				distMatrixList.add(res.get());
			}
			
			pool.shutdown();
			log.info("Thread pool has complete all DocCompHungarian tasks: {}", pool.awaitTermination(10, TimeUnit.HOURS));
			System.out.println("MainRunParallel threads finished. Continue with Hungarian Method.");
			
			Double upperBoundPathCost = Double.longBitsToDouble(globalMaxPathCost.get());
			log.info("globalMaxPathCost = {}", upperBoundPathCost);
			
			for (DistMatrix<Snippet, Snippet, Double[][]> distMatrix : distMatrixList) {
				
				Double[][] m = distMatrix.distanceMatrix;
					
				for (int i = 0; i < m.length; i++) {
					for (int j = 0; j < m[i].length; j++)
						if (m[i][j] == null)
							m[i][j] = upperBoundPathCost;
				}
				
				int commonNodesCnt = 0;
				
				int max = Math.max(m.length, m[0].length);
				double[][] hm = new double[max][max];
				
				for (int k = 0; k < max; k++) {
					for (int l = 0; l < max; l++) {
						if (k < m.length && l < m[k].length) {
							if (m[k][l] != null) {
								hm[k][l] = m[k][l] / upperBoundPathCost ;
								if (m[k][l].compareTo(0d) == 0)
									commonNodesCnt++;
							} else {
								hm[k][l] = 1d ; //upperBoundPathCost / (1d * upperBoundPathCost * max);
							}
						} else {
							hm[k][l] = 1d ; //upperBoundPathCost / (1d * upperBoundPathCost * max);
						}	
					}
				}
				
				log.info("Document S{}, Nodes {}, S{}, Nodes {}, CommonNodes {}",
						distMatrix.obj1.getSnippetId(),
						m.length,
						distMatrix.obj2.getSnippetId(),
						m[0].length,
						commonNodesCnt);
				log.debug("NormalizedTotalDistMatrix {}", hm);
				
				HungarianMethod hungarianMethod = new HungarianMethod();
				hungarianMethod.setInputMatrix(hm);
				hungarianMethod.solve();
				Double normalizedSimilarity = (max - hungarianMethod.getMinimum()) / (m.length + m[0].length - commonNodesCnt);
				
				//TODO Normalize Hungarian Res
				
				log.info("Document S{}, S{}, NormalizedSimilarity {}", 
						distMatrix.obj1.getSnippetId(),
						distMatrix.obj2.getSnippetId(),
						normalizedSimilarity);
			}
			
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
