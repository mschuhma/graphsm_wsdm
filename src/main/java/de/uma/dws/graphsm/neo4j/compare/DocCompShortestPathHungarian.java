package de.uma.dws.graphsm.neo4j.compare;

import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
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

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.mysql.Neo4jPathCacheMySqlConnector;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;

public class DocCompShortestPathHungarian {
	
	final static Logger log = LoggerFactory.getLogger(DocCompShortestPathHungarian.class);
	final static Configuration conf = ConfFactory.getConf();
	
	Neo4jRdfGraph graph;
	
	Snippet s1 = null;
	Snippet s2 = null;
	
	Double maxPathLen = null;
	
	boolean findAllPath = false;
	static final Neo4jPathCacheMySqlConnector pathCache = 
			new Neo4jPathCacheMySqlConnector(conf.getString("mysql.db.neo4jcache.shortestpath.table")); //.getInstance();

	HashSet<Vertex> s1nodes;
	HashSet<Vertex> s2nodes;


	public DocCompShortestPathHungarian(Neo4jRdfGraph graph, Snippet s1, Snippet s2, Double maxPathLen) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = (maxPathLen == null ? Double.MAX_VALUE : maxPathLen);
	}
	
	
	public DocCompShortestPathHungarian(Neo4jRdfGraph graph, Snippet s1, Snippet s2) {
		this.graph = graph;
		this.s1 = s1;
		this.s2 = s2;
		this.maxPathLen = Double.MAX_VALUE;
	}

	public Double[][] shortestPath() {

		s1nodes = Neo4jGraphUtils.getSourceNodes(graph, s1);
		s2nodes = Neo4jGraphUtils.getSourceNodes(graph, s2);

		log.info("Compute pairwise shortest path: S{} ({} nodes), S{} ({} nodes)",
				s1.getSnippetId(), s1nodes.size(),
				s2.getSnippetId(), s2nodes.size());

		int pathLenghtSum = 0;
		int pathCount = 0;
		int commonNodeCount = 0;

		int cacheCounter = 0;
		
		Double[][] distMatrix = new Double[s1nodes.size()][s2nodes.size()];
		int s1i = -1; //matrix indices

		for (Vertex v1 : s1nodes) {
			
			s1i++;
			int s2i = -1;
			
			for (Vertex v2 : s2nodes) {
				
				s2i++;
				
				distMatrix[s1i][s2i] = null;

				Long v1id = (Long) (v1.getId());
				Long v2id = (Long) (v2.getId());

				if (v1id.compareTo(v2id) == 0) {
					commonNodeCount++;
					//pathCount++; 
					//pathLenghtSum += 0
					distMatrix[s1i][s2i] = 0d;
					continue;
				}

				log.debug("Processing Node {} and Node {}", v1id, v2id);

				Node n1 = graph.getRawGraph().getNodeById(v1id);
				Node n2 = graph.getRawGraph().getNodeById(v2id);

				//Get shortest path

				Integer pathLen = null; //p.length();

				Tuple<Integer, Double> path = pathCache.getPath(v1id, v2id, log.isDebugEnabled());

				if (path != null) {

					pathLen = path.k;

					cacheCounter++;

					log.debug("Path between Node{} and Node{} found, Length {}, Weight {} (from mysql cache)",
							v1id, v2id, pathLen);
				}
				else {

					log.debug("Start ShortestPath Node {} and Node {}", v1id, v2id);

					PathFinder<Path> shortestPathAlgo = GraphAlgoFactory.shortestPath(
							(PathExpander<?>) Traversal.expanderForAllTypes(),
							maxPathLen.intValue());

					Path shortestPath = shortestPathAlgo.findSinglePath(n1, n2);
					StringBuffer shorestPathSteps = null;

					if (shortestPath != null) {

						pathLen = shortestPath.length();

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

							log.debug("Path between Node{} and Node{} found, Length {}, Path {}",
									v1id, v2id, pathLen, shorestPathSteps);
						}
					}

					else {
						log.debug("Path between Node{} and Node{} not found.", v1id, v2id);
					}

					//Update mysql db cache
					if (log.isDebugEnabled()) {
						pathCache.setPath(v1id, v2id, pathLen, null, shorestPathSteps);
					} else {
						pathCache.setPath(v1id, v2id, pathLen, null);
					}
				}

				//Getting shortest path data
				if (pathLen != null) {

					if (pathLen <= maxPathLen) {
						pathLenghtSum += pathLen;
						
						distMatrix[s1i][s2i] = 1d * pathLen;

						pathCount++;

					}
				}

			}

		}

		log.info("Similarity measures S{}, S{}: CommonNodes {}, PathCnt {}, MaxTheoPathCnt {}, SumPathLen {}",
				s1.getSnippetId(),
				s2.getSnippetId(), 
				commonNodeCount,
				pathCount, 
				s1nodes.size()*s2nodes.size(), 
				pathLenghtSum);
		
		log.debug("DistMatrix={}", distMatrix);

		//log.info("Total path score S{}, S{}: {}", s1.getSnippetId(), s2.getSnippetId(), sim);
		return distMatrix;
	}

}