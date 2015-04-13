package de.uma.dws.graphsm.neo4j.compare;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uni_mannheim.informatik.dws.dwslib.Collection;

public class PairwiseNodeComparator implements DocComparator{

	final static Logger log = LoggerFactory.getLogger(PairwiseNodeComparator.class);
	
	Neo4jRdfGraph graph;

	StringBuffer s1nodesLabels;
	StringBuffer s2nodesLabels;
	HashSet<Vertex> s1nodes;
	HashSet<Vertex> s2nodes;
	
	public PairwiseNodeComparator(Neo4jRdfGraph graph) {
		this.graph = graph;
	}

	
	public String shortestPathCypher(Snippet s1, Snippet s2) {
		
		this.getSourceNodes(s1, s2);
		Integer length = 0;
		int cnt = 0;
		
//		Query ALTERNATIVE
//		START n1=node(*), n2=node(*)
//				MATCH p=shortestPath(n1-[r*]-n2)
//				WHERE has(n1.sourceNode) AND has(n2.sourceNode) AND
//				n1<>n2 AND 
//				any(s in n1.snippetId where s=1) AND
//				any(s in n2.snippetId where s=1)
//				RETURN n1, n1.label, n2, n2.label, length(p) as len
//				ORDER BY len
		
		for (Vertex v1 : s1nodes) {
			for (Vertex v2 : s2nodes) {
				if (v1.getId() == v2.getId())
					continue;
				//log.debug("Query: shortestPath(node"+v1.getId()+"), node"+v2.getId()+")");
				ExecutionResult res = graph.executeCypher(
						//TODO use execute(String query, Map<String,Object> params) 
						"START n1=node("+v1.getId()+"), n2=node("+v2.getId()+") " +
						"MATCH p=shortestPath((n1)-[r*]-(n2))" +
						"RETURN p as path, nodes(p) as nodes, length(p) as len, n1.label as node1, n2.label as node2");
				for (Map<String, Object> m : res) {
					cnt++;
					Integer len = (Integer) m.get("len");
					length += len;
					System.out.print("Path of "+len+" steps found from Node " + m.get("node1")+"--");
					System.out.print(" to Node "+ m.get("node2") + "\n");
					System.out.println(m.get("path"));
				}
			}
		}
		return (
				"Snippet"+s1.getSnippetId() + " (" +s1nodes.size() +" nodes), " +
				"Snippet"+s2.getSnippetId() + " (" +s2nodes.size() +"nodes) : " +
				"Number of connections/pathways "+cnt+", Average pathlength "+length.floatValue()/cnt);
	}
	
	
	public Double shortestPath(Snippet s1, Snippet s2, boolean allowMultiPath, int maxPathLenght) {
		
		this.getSourceNodes(s1, s2);
		int pathLenghtSum = 0;
		int pathCount = 0;
		int commonNodeCount = 0;
		log.debug("Compute pairwise shortest path between following nodes:");
		log.debug("{}", s1nodesLabels);
		log.debug("{}", s2nodesLabels);
		
		for (Vertex v1 : s1nodes) {
			for (Vertex v2 : s2nodes) {
				if (v1.getId() == v2.getId()) {
					commonNodeCount++;
					pathCount++; //pathLenghtSum += 0
					continue;
				}
				Node n1 = graph.getRawGraph().getNodeById((Long)v1.getId());
				Node n2 = graph.getRawGraph().getNodeById((Long)v2.getId());
				PathExpander<?> expander = (PathExpander<?>) Traversal.expanderForAllTypes();
				StringBuffer bf = null;
				for(Path p: GraphAlgoFactory.shortestPath(expander, maxPathLenght).findAllPaths(n1, n2)) {
					log.debug("Path between Node{} and Node{} found, length {}", n1.getId(), n2.getId(), p.length());
					pathCount++;
					pathLenghtSum += p.length();
					Iterator<PropertyContainer> iter = p.iterator();
					bf = new StringBuffer();
					while (iter.hasNext()) {
						Object e = iter.next();
						if (e instanceof NodeProxy) {
							Node n = (Node) e;
							bf.append("Node("+n.getProperty("label")+")");
						}
						else if (e instanceof RelationshipProxy) {
							Relationship r = (Relationship) e;
							bf.append("-["+r.getType()+"]-");
						}
						else {log.error("ERROR");}
					}
					log.debug("{}", bf);
					if (!allowMultiPath) break;
				}	
			}
		}
		log.debug("Similarity measures Snippet {}, Snippet {}: Common Nodes {}, Path Count {}, Max theo Pathes {}, Avg Path Length {}",
				s1.getSnippetId(), s2.getSnippetId(), commonNodeCount, pathCount, s1nodes.size()*s2nodes.size(), (1.0*pathLenghtSum)/pathCount);
		double sim = pathCount == 0 ? 0 : (1.0*pathCount/(1.0*s1nodes.size()*s2nodes.size()))/(1.0+(1.0*pathLenghtSum)/pathCount);
		log.info("Final similarity score (Snippet {}, Snippet {}): {}", s1.getSnippetId(), s2.getSnippetId(), sim);
		return sim;
	}
	
	void getSourceNodes(Snippet s1, Snippet s2) {

		s1nodes = Neo4jGraphUtils.getSourceNodes(graph, s1);
		s2nodes = Neo4jGraphUtils.getSourceNodes(graph, s2);
	}
	
	
	public static void main(String[] args) {
//		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance("/var/lib/neo4j/data/graph-2hops.db", false);
//		Node n1 = graph.getRawGraph().getNodeById(931);
//		Node n2 = graph.getRawGraph().getNodeById(4886);
//		PathFinder<WeightedPath> dijkstra = GraphAlgoFactory.dijkstra(
//				(PathExpander<?>) Traversal.expanderForAllTypes(),
//				new RobustWeightEvaluator());
//
//		int pathCount = 0;
//		int pathLenghtSum = 0;
//		StringBuffer bf = new StringBuffer();
//		
//		System.out.println("Start Dijkstra...");
//		WeightedPath p = null;// = dijkstra.findSinglePath(n1, n2);
//		ExecutorService executor = Executors.newSingleThreadExecutor();
//		Future<WeightedPath> d = executor.submit(new RobustDijkstra(dijkstra, n2, n2));
//		try {p = d.get(10, TimeUnit.SECONDS);}
//		catch (TimeoutException | InterruptedException | ExecutionException e) {
//			System.out.println("RobustDijkstra killed after timeout" + e);
//			} finally {
//				executor.shutdown();
//			}
//		graph.shutdown();

	}

}
