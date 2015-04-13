package de.uma.dws.graphsm.jgrapht;

import java.io.Writer;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.datamodel.JGraphTNode;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tools.NodeDistMatrix;
import de.unima.alcomox.algorithms.HungarianMethod;

public class DijkstraParallel implements Callable<Tuple<String,Double>> {
	
	final static Logger log = LoggerFactory.getLogger(DijkstraParallel.class);	
	
	Neo4jRdfGraph graph1 = null;
	Neo4jRdfGraph graph2 = null;
	
	int maxPathLength;
	Double maxPathCost = null;

	String pairId;
	
	Writer out = null;
	
	public DijkstraParallel(Neo4jRdfGraph graph1, Neo4jRdfGraph graph2, int maxPathLength, Double maxPathCost, Writer writer) {
		
		this.graph1 = graph1;
	   this.graph2 = graph2;
	   	   
	   this.maxPathLength 	= maxPathLength;
	   this.maxPathCost 		= maxPathCost;
	      
	   this.pairId = 
	   			(Integer.valueOf(graph1.actualGraphDBDirectory.getName().
	   						replace("doc-", "").replace(".db", "")) +1)
	   			+ "\t" +
	   			(Integer.valueOf(graph2.actualGraphDBDirectory.getName().
	   						replace("doc-", "").replace(".db", "")) +1) ;
	   
	   this.out = writer;
   }
	
	public DijkstraParallel(Neo4jRdfGraph graph1, Neo4jRdfGraph graph2, int maxPathLength, Double maxPathCost) {
		this(graph1, graph2, maxPathLength, maxPathCost, null);
	}

	@Override
	public Tuple<String,Double> call() throws Exception {
		
		//Join two neo4j graphs into one jgrapht graph
		JGraphTWeightedRdf graph = new JGraphTWeightedRdf(pairId);

		graph.addGraph(graph1);
		graph.addGraph(graph2);
		
		graph.removeDeadEndNodes();
		
		//Work around for gml:_Feature with gets a very high 
//		if (graph.maxPathCost > 1000) {
//			log.warn("graph.maxPathCost={}", graph.maxPathCost);
//			graph.maxPathCost = 1000d;
//		}
		
//		System.out.println(graph);

		LinkedList<Vertex> sNodes1 = new LinkedList<Vertex>(Neo4jGraphUtils.getSourceNodes(graph1));
		LinkedList<Vertex> sNodes2 = new LinkedList<Vertex>(Neo4jGraphUtils.getSourceNodes(graph2));

		//Ensure that snodes1 node cnt >= snode2 node cnt 
		if (sNodes1.size() < sNodes2.size()) {
			LinkedList<Vertex> tmp = sNodes1;
			sNodes1 = sNodes2;
			sNodes2 = tmp;
		}

		int sNode1Cnt = sNodes1.size();
		int sNode2Cnt = sNodes2.size();

		//Compute distance matrix of cheapest paths between all pairs of source nodes with dijkstra
		//Create artificial distances to ensure square form matrix
		Double mostExpensivePath = 0d;
		
		Double[][] distMatrix = new Double[sNode1Cnt][sNode1Cnt];

		for (int i = 0; i < sNode1Cnt; i++) {
			for (int j = 0; j < sNode1Cnt; j++) {

				if (j >= sNode2Cnt) {
					distMatrix[i][j] = null;
				}
				else {
					Double cost = graph.dijkstra(
								new JGraphTNode(sNodes1.get(i).getProperty("label").toString(), true),
								new JGraphTNode(sNodes2.get(j).getProperty("label").toString(), true),
					         maxPathLength,
					         maxPathCost,
					         out);
					         //maxPathLength * graph.maxPathCost);

					if (cost != null && cost > mostExpensivePath)
						mostExpensivePath = cost;

					distMatrix[i][j] = cost;
				}
			}
		}
		
//		mostExpensivePath = mostExpensivePath * 0.9;//28.10429968;

		int commonNodesCnt = NodeDistMatrix.getCntOfValues(distMatrix, 0d);
		int unconnectedNodesCnt = NodeDistMatrix.getCntOfNullValues(distMatrix);
		
		log.info("DijkstraResult {} DistMatrixSize={} MaxPathLength={} mostExpensivePath={} commonNodesCnt={} unconnectedNodesCnt={}", 
					pairId, distMatrix.length*distMatrix[0].length, maxPathLength, mostExpensivePath, commonNodesCnt, unconnectedNodesCnt);
		log.debug(NodeDistMatrix.printMatrix(distMatrix));
				
		//If no paths were found at all, skip further processing and return norm sim score of 0d
		if (NodeDistMatrix.allValuesNull(distMatrix)) {
			log.info("Dijkstra computation finished: Pair {} normalizedSimilarity {}", pairId, 0d);
			return new Tuple<String, Double>(pairId, 0d);
		}
		
		//Set cost of those paths with length > maxPathLength to 1 and normalize values 
		double[][] normDistMatrix = NodeDistMatrix.normalizeValues(distMatrix, mostExpensivePath, true);
		
		log.debug(NodeDistMatrix.printMatrix(normDistMatrix));
		
		//Use hungrian method to find best match
		HungarianMethod hungarianMethod = new HungarianMethod();
		
		hungarianMethod.setInputMatrix(normDistMatrix);
		double hungarianMin = hungarianMethod.getMinimum();
		
		log.debug("Results from hungarianMethod.getMinimum() {}", hungarianMin);

		Double normalizedSimilarity = 
					(sNode1Cnt - hungarianMin) //sNode1Cnt * 1 (which is max posibily distance)
				 / (sNode1Cnt + sNode2Cnt - commonNodesCnt); //was before (m.length + m[0].length - commonNodesCnt);
		
		log.info("Dijkstra computation finished: Pair {} normalizedSimilarity {}", pairId, normalizedSimilarity);
		
		return new Tuple<String, Double>(pairId, normalizedSimilarity);
	}

}
