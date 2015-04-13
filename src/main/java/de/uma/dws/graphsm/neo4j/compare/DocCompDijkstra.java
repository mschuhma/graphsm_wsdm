package de.uma.dws.graphsm.neo4j.compare;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PathExpander;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.mysql.Neo4jPathCacheMySqlConnector;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.neo4j.RobustWeightEvaluator;

public class DocCompDijkstra {
	
	final static Logger log = LoggerFactory.getLogger(DocCompDijkstra.class);
	final static Configuration conf = ConfFactory.getConf();
	
	Neo4jRdfGraph graph;
	
	Snippet s1 = null;
	Snippet s2 = null;
	
	Double maxPathLen = null;
	
	boolean findAllPath = false;
	static final Neo4jPathCacheMySqlConnector pathCache = 
			new Neo4jPathCacheMySqlConnector(conf.getString("mysql.db.neo4jcache.dijkstra.table")); //.getInstance();
	
	AtomicLong globalMaxPathCost = null;

	HashSet<Vertex> s1nodes;
	HashSet<Vertex> s2nodes;


	public DocCompDijkstra(Neo4jRdfGraph graph, Snippet s1, Snippet s2, Double maxPathLen, AtomicLong globalMaxPathCost) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = maxPathLen;
		this.globalMaxPathCost = globalMaxPathCost;
		log.debug("Parameters: graph={}, s1={}, s2={}, maxPathLen={}, globalMaxPathCost={}",graph,s1, s2, maxPathLen, globalMaxPathCost);
	}
	
	public DocCompDijkstra(Neo4jRdfGraph graph, Snippet s1, Snippet s2, AtomicLong globalMaxPathCost) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = Double.MAX_VALUE;
		this.globalMaxPathCost = globalMaxPathCost;
		log.debug("Parameters: graph={}, s1={}, s2={}, maxPathLen={}, globalMaxPathCost={}",graph,s1, s2, maxPathLen, globalMaxPathCost);
	}
	
	public DocCompDijkstra(Neo4jRdfGraph graph, Snippet s1, Snippet s2) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = Double.MAX_VALUE;
		this.globalMaxPathCost = null;
		log.debug("Parameters: graph={}, s1={}, s2={}, maxPathLen={}, globalMaxPathCost={}",graph,s1, s2, maxPathLen, globalMaxPathCost);
	}

	public Double dijkstra() {

		s1nodes = Neo4jGraphUtils.getSourceNodes(graph, s1);
		s2nodes = Neo4jGraphUtils.getSourceNodes(graph, s2);

		log.info("Compute pairwise cheapest path: S{} ({} nodes), S{} ({} nodes)",
				s1.getSnippetId(), s1nodes.size(),
				s2.getSnippetId(), s2nodes.size());

		int pathLenghtSum = 0;
		int pathCount = 0;
		int commonNodeCount = 0;
		Double pathWeightSum = 0d;

		int cacheCounter = 0;

		for (Vertex v1 : s1nodes) {
			for (Vertex v2 : s2nodes) {

				Long v1id = (Long) (v1.getId());
				Long v2id = (Long) (v2.getId());

				if (v1id.compareTo(v2id) == 0) {
					commonNodeCount++;
					//pathCount++; 
					//pathLenghtSum += 0
					continue;
				}

				log.debug("Processing Node {} and Node {}", v1id, v2id);

				Node n1 = graph.getRawGraph().getNodeById(v1id);
				Node n2 = graph.getRawGraph().getNodeById(v2id);

				//Get shortest path

				Integer pathLen = null; //p.length();
				Double pathWeight = null; //p.weight()

				Tuple<Integer, Double> path = pathCache.getPath(v1id, v2id, log.isDebugEnabled());

				if (path != null) {

					pathLen = path.k;
					pathWeight = path.v;

					cacheCounter++;

					log.debug("Path between Node{} and Node{} found, Length {}, Weight {} (from mysql cache)",
							v1id, v2id, pathLen, pathWeight);
				}
				else {

					log.debug("Start Dijkstra Node {} and Node {}", v1id, v2id);

					PathFinder<WeightedPath> dijkstra = GraphAlgoFactory.dijkstra(
							(PathExpander<?>) Traversal.expanderForAllTypes(),
							new RobustWeightEvaluator());

					WeightedPath shortestPath = dijkstra.findSinglePath(n1, n2);
					StringBuffer shorestPathSteps = null;

					if (shortestPath != null) {

						pathLen = shortestPath.length();
						pathWeight = shortestPath.weight();

						//Constructing path for debug logging
						if (log.isDebugEnabled()) {

							Iterator<PropertyContainer> iter = shortestPath.iterator();
							shorestPathSteps = new StringBuffer();

							while (iter.hasNext()) {
								Object e = iter.next();
								if (e instanceof NodeProxy) {
									Node n = (Node) e;
									shorestPathSteps.append("("+n.getProperty("label")+")");
								}
								else if (e instanceof RelationshipProxy) {
									Relationship r = (Relationship) e;
									shorestPathSteps.append("-["+r.getType()+"]-");
								}
								else {log.error("ERROR");}
							}

							log.debug("Path between Node{} and Node{} found, Length {}, Weight {}, Path {}",
									v1id, v2id, pathLen, pathWeight, shorestPathSteps);
						}
					}

					else {
						log.debug("Path between Node{} and Node{} not found.", v1id, v2id);
					}

					//Update mysql db cache
					if (log.isDebugEnabled()) {
						pathCache.setPath(v1id, v2id, pathLen, pathWeight, shorestPathSteps);
					} else {
						pathCache.setPath(v1id, v2id, pathLen, pathWeight);
					}
				}

				//Getting shortest path data
				if (pathLen != null) {

					if (pathLen <= maxPathLen) {
						pathLenghtSum += pathLen;
						pathWeightSum += pathWeight;

						pathCount++;

						if (globalMaxPathCost != null && Double.longBitsToDouble(globalMaxPathCost.get()) < pathWeight)
							globalMaxPathCost.set(Double.doubleToLongBits(pathWeight));
					}
				}

			}

		}

		log.info("Similarity measures S{}, S{}: CommonNodes {}, PathCnt {}, MaxTheoPathCnt {}, SumPathLen {}, SumPathWeight {}, CacheCnt {}",
				s1.getSnippetId(),
				s2.getSnippetId(), 
				commonNodeCount,
				pathCount, 
				s1nodes.size()*s2nodes.size(), 
				pathLenghtSum,
				pathWeightSum,
				cacheCounter);

		//log.info("Total path score S{}, S{}: {}", s1.getSnippetId(), s2.getSnippetId(), sim);
		return pathWeightSum;
	}

}