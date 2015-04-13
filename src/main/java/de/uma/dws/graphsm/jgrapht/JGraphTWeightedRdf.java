package de.uma.dws.graphsm.jgrapht;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.datamodel.JGraphTNode;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class JGraphTWeightedRdf {

	final static Logger	                                       log	      = LoggerFactory
	                                                                                 .getLogger(JGraphTWeightedRdf.class);

	public WeightedMultigraph<JGraphTNode, DefaultWeightedEdge>	graph;

	String	                                                   graphName	= null;
	Double	                                                   maxPathCost	= 0d;

	public JGraphTWeightedRdf() {
		this.graph = new WeightedMultigraph<JGraphTNode, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	}

	public JGraphTWeightedRdf(String graphName) {
		this();
		this.graphName = graphName;
	}

	public void removeDeadEndNodes() {

		Set<JGraphTNode> nodeSet = graph.vertexSet();

		int cnt = 0;
		int nodesCnt = nodeSet.size();

		LinkedList<JGraphTNode> deadEndNodes = new LinkedList<JGraphTNode>();

		for (JGraphTNode node : graph.vertexSet()) {
			if (!node.sourceNode && graph.degreeOf(node) < 2) {
				deadEndNodes.add(node);
				cnt++;
			}
		}
		graph.removeAllVertices(deadEndNodes);

		log.info("Removed {} deadend out of {} nodes", cnt, nodesCnt);
	}

	public void addGraph(Neo4jRdfGraph g) throws RuntimeException {

		int inputNodeCnt = 0;

		log.debug("Start transforming Neo4jRdfGraph {} into JGraphT", g.actualGraphDBDirectory);

		for (Vertex v : g.getVertices()) {
			inputNodeCnt++;
			if ((long) v.getId() == 0l)
				continue;
			graph.addVertex(new JGraphTNode(v.getProperty("label").toString(), v.getProperty("sourceNode")));
		}

		if (inputNodeCnt <= 1) {
			log.warn("Neo4j input graph {} is empty!", g.actualGraphDBDirectory);
			throw new RuntimeException("Neo4j input graph is empty! " + g.actualGraphDBDirectory);
		}

		for (Edge e : g.getEdges()) {

			JGraphTNode l1 = new JGraphTNode(e.getVertex(Direction.OUT).getProperty("label").toString(), e.getVertex(
			         Direction.OUT).getProperty("sourceNode"));
			JGraphTNode l2 = new JGraphTNode(e.getVertex(Direction.IN).getProperty("label").toString(), e.getVertex(
			         Direction.IN).getProperty("sourceNode"));

			try {
				DefaultWeightedEdge edge = graph.addEdge(l1, l2);
				String cost = e.getProperty("cost").toString();
				if (cost == null)
					throw new RuntimeException("No cost value for edge found: " + l1 + "--" + l2);

				Double costValue = Double.valueOf(cost);

				if (costValue > this.maxPathCost)
					this.maxPathCost = costValue;

				graph.setEdgeWeight(edge, costValue);
			}
			catch (IllegalArgumentException e1) {
				log.debug("Looping edge skiped {} {}", l1, e1);
			}

		}
		log.debug("Neo4jRdfGraph {} successfully transformed into JGraphT {}", g.actualGraphDBDirectory, graph.toString());
	}

	public String toString() {

		StringBuffer bf = new StringBuffer();

		for (DefaultWeightedEdge e : graph.edgeSet()) {
			bf.append(graph.getEdgeSource(e) + "->" + graph.getEdgeTarget(e) + " " + graph.getEdgeWeight(e) + "\n");
		}
		return bf.toString();
	}

	public Double dijkstra(JGraphTNode node1, JGraphTNode node2, Integer maxPathLength, Double maxPathCost)
	         throws IOException {

		return dijkstra(node1, node2, maxPathLength, maxPathCost, null);
	}

	/**
	 * 
	 * @param node1
	 *           Label (prefixed URL) of node1
	 * @param node2
	 *           Label (prefixed URL) of node1
	 * @param maxPathLength
	 *           Maximal path length accepted
	 * @param maxPathCost
	 *           Set maximum path cost to limit search radius and thus improve performance. Set null for unbound search.
	 * @param pathPrinter Provide writer, eg. FileWriter, to get details on path found; provide null to deactive 
	 * @return Cost of cheapest path or Double.POSITIVE_INFINITY if no path was found or if path was of length >
	 *         maxPathLenght
	 * @throws IOException
	 */
	public Double dijkstra(JGraphTNode node1, JGraphTNode node2, Integer maxPathLength, Double maxPathCost,
	         Writer pathPrinter) throws IOException {

		// log.debug("Disjktra: {} -- {} ", node1, node2);

		if (node1.equals(node2))
			return new Double(0d);

		if (maxPathCost == null)
			maxPathCost = Double.POSITIVE_INFINITY;

		DijkstraShortestPath<JGraphTNode, DefaultWeightedEdge> dijkstra = null;

		try {
			dijkstra = new DijkstraShortestPath<JGraphTNode, DefaultWeightedEdge>(graph, node1, node2, maxPathCost);
		}
		catch (java.lang.IllegalArgumentException e) {
			System.err.print(node1 + " " + node2 + "\n");
			e.printStackTrace();
			return null;
		}

		Double pcost = dijkstra.getPathLength();

		// pcost is Double.POSITIVE_INFINITY if no path was found
		if (pcost.isInfinite()) {
			pcost = null;
		}
		else {
			int plen = dijkstra.getPathEdgeList().size();
			// log.debug("Disjktra: {} {} pcost {} plen {}",
			// node1, node2, pcost, plen);
			if (plen > maxPathLength)
				pcost = null;

			if (pathPrinter != null && pcost != null) {
				
				GraphPath<JGraphTNode, DefaultWeightedEdge> path = dijkstra.getPath();
				
				pathPrinter.write(this.graphName + "\t" + node1 + "\t" + node2 + "\t" + pcost + "\t" + plen + "\n");
				pathPrinter.write(path + "\n");
				pathPrinter.flush();
				
			}
		}
		return pcost;
	}

	@SuppressWarnings("unused")
	public static void main(String args[]) {

		JGraphTWeightedRdf g = new JGraphTWeightedRdf();
	}

}
