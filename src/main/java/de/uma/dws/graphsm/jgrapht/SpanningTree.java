package de.uma.dws.graphsm.jgrapht;

import org.apache.commons.configuration.Configuration;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class SpanningTree {
	
	final static Logger log = LoggerFactory.getLogger(SpanningTree.class);
	final static Configuration conf = ConfFactory.getConf();
	
	
	private ListenableGraph<Vertex, Edge> graph = null;
//	private ListenableDirectedWeightedGraph<Vertex, Edge> graph = null;
	
//	public JGraphT(PairwiseNodeComparator pnc) {
//		graph = new ListenableDirectedGraph(DefaultEdge.class);
//		
//		for (Vertex v : pnc.getGraph().getVertices()) {
//			graph.addVertex(v);
//		}
//		for (Edge e : pnc.getGraph().getEdges()) {
//    		graph.addEdge(e.getVertex(Direction.IN), e.getVertex(Direction.OUT));
//    	}
//	}
	
	
	public SpanningTree(Neo4jGraph neo) {
		graph = new ListenableDirectedGraph(DefaultEdge.class);
		//org.jgrapht.graph.ListenableDirectedWeightedGraph<V,E>
		
		for (Vertex v : neo.getVertices()) {
			graph.addVertex(v);
		}
		for (Edge e : neo.getEdges()) {
    		graph.addEdge(e.getVertex(Direction.IN), e.getVertex(Direction.OUT));
    		System.out.println(e.toString());
    		System.out.println("IN-Node " + e.getVertex(Direction.IN).toString() + e.getVertex(Direction.IN).getProperty("label"));
    		System.out.println("OUT-Node " + e.getVertex(Direction.OUT).toString() + e.getVertex(Direction.OUT).getProperty("label"));
    		break;
    		//addEdge(Graph<V,E> g, V sourceVertex, V targetVertex, double weight)
    		//sourceNode --> targetNode 
    	}
	}
	
	public KruskalMinimumSpanningTree<Vertex, Edge> getKMST() {
		log.debug("Computation of Kruskal Minimum Spanning Tree started...");
		KruskalMinimumSpanningTree<Vertex, Edge> kmst = new KruskalMinimumSpanningTree<Vertex, Edge>(this.graph);
		log.debug("Kruskal Minimum Spanning Tree finisted: Tree cost = {}", kmst.getSpanningTreeCost());
		return kmst;
	}
	
	public static void main(String[] args) {
		
		//PairwiseNodeComparator comp = new PairwiseNodeComparator("/var/lib/neo4j/data/graph-2hops-binary.db");
		//System.out.println("Getting JGraphT");
		
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance("/var/lib/neo4j/data/graph.db", false);
		
		try {
			SpanningTree g = new SpanningTree(graph); 
			
			KruskalMinimumSpanningTree<Vertex, Edge> kmst = g.getKMST();
			
			System.out.println("Spanning tree consts: " + kmst.getSpanningTreeCost());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			graph.shutdown();
		}
	}

}
