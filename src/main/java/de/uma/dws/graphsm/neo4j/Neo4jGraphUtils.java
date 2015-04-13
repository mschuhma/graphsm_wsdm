package de.uma.dws.graphsm.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLTokens;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uni_mannheim.informatik.dws.dwslib.Counter;

public class Neo4jGraphUtils {

	final static Logger	log	= LoggerFactory.getLogger(Neo4jGraphUtils.class);

	@SuppressWarnings("unchecked")
	public static <E> HashSet<E> addOrUpdateHashSetProperty(Element e, String key, E value) {
		Object o = e.getProperty(key);
		HashSet<E> set = new HashSet<E>();
		set.add(value);

		if (o != null) {
			if (o instanceof Integer) {
				set.add((E) o);
			}

			else if (o instanceof Integer[]) {
				Integer[] arrayInteger = (Integer[]) o;
				for (int i = 0; i < arrayInteger.length; i++) {
					set.add((E) arrayInteger[i]);
				}
			}

			else if (o instanceof int[]) {
				int[] arrayInt = (int[]) o;
				for (int i = 0; i < arrayInt.length; i++) {
					set.add((E) Integer.valueOf(arrayInt[i]));
				}
			}

			else if (o instanceof String) {
				String[] arrayStr = (String[]) o;
				for (int i = 0; i < arrayStr.length; i++) {
					set.add((E) arrayStr[i]);
				}
			}

			else if (o instanceof boolean[]) {
				boolean[] arrayBoolean = (boolean[]) o;
				for (int i = 0; i < arrayBoolean.length; i++) {
					set.add((E) Boolean.valueOf(arrayBoolean[i]));
				}
			}

			else {
				log.error("Casting error {}", o.getClass().getSimpleName());
			}

		}
		e.setProperty(key, set);
		return set;
	}

	public static void removeDeadEndNodes(Neo4jRdfGraph graph, boolean keepAllSourceNodes) {

		StringBuffer unconnectedNodes = new StringBuffer();
		StringBuffer deadEndNodes = new StringBuffer();

		int nodeCnt = 0;
		int unconnectedNodesCnt = 0;
		int deadEndNodesCnt = 0;
		int sourceNodeCnt = 0;

		Transaction tx = graph.getRawGraph().beginTx();
		try {
			Iterator<Node> allNodes = GlobalGraphOperations.at(graph.getRawGraph()).getAllNodes().iterator();
			while (allNodes.hasNext()) {
				Node n = allNodes.next();

				if (n.getId() == 0l)
					continue;

				if (keepAllSourceNodes && n.hasProperty("sourceNode")) {
					sourceNodeCnt++;
					continue;
				}
				nodeCnt++;

				if (!n.hasRelationship()) {
					if (log.isDebugEnabled())
						unconnectedNodes.append(n.getProperty("label") + "(" + n.getId() + "), ");
					unconnectedNodesCnt++;
					n.delete();
				}

				else {
					int relCnt = 0;
					Relationship onlyRel = null;
					for (Relationship r : n.getRelationships()) {
						if (relCnt > 1)
							break;
						relCnt++;
						onlyRel = r;
					}

					if (relCnt == 1) {
						if (log.isDebugEnabled())
							deadEndNodes.append(n.getProperty("label") + "(" + n.getId() + "), ");
						deadEndNodesCnt++;
						onlyRel.delete();
						n.delete();
					}
				}
			}
			tx.success();
		}
		catch (Exception e) {
			System.out.println(e);
		}
		finally {
			tx.finish();
		}
		log.info("removeDeadEndNodes checked {} nodes ({} source nodes skipped): {} unconnected and {} deadend nodes were deleted.",
		         nodeCnt, sourceNodeCnt, unconnectedNodesCnt, deadEndNodesCnt);
		log.debug("List of removed unconnected nodes: {}", unconnectedNodes);
		log.debug("List of removed deadend nodes: {}", deadEndNodes);
	}
	
	public static void updateAllEdgeWeights(Neo4jRdfGraph graph, TripleWeighter tripleWeighter) {
		updateAllEdgeWeights(graph, tripleWeighter, "weight");
	}
	
	public static void updateAllEdgeWeights(Neo4jRdfGraph graph, TripleWeighter tripleWeighter, String propertyLabel) {

		Transaction tx = graph.getRawGraph().beginTx();
		int cnt = 0;

		try {
			Iterator<Relationship> allRels = GlobalGraphOperations.at(graph.getRawGraph()).getAllRelationships()
			         .iterator();

			while (allRels.hasNext()) {
				cnt++;
				Relationship r = allRels.next();

				String subj = r.getStartNode().getProperty("label").toString();
				String pred = r.getType().toString();
				String obj = r.getEndNode().getProperty("label").toString();
				
				r.setProperty(propertyLabel, tripleWeighter.compute(new Triple(subj, pred, obj)));				
				
			}

			tx.success();

		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		finally {
			tx.finish();
			graph.commit();
		}
		log.info("Weights for graph {} in field {} updated with {} for {} edges", 
					graph.actualGraphDBDirectory.getName() ,propertyLabel, tripleWeighter, cnt);

	}

	public static Counter getRelationshipStats(Neo4jRdfGraph graph) {
		// Iterable<RelationshipType> types = GlobalGraphOperations.at(graph.getRawGraph()).getAllRelationshipTypes();
		Iterable<Relationship> rels = GlobalGraphOperations.at(graph.getRawGraph()).getAllRelationships();
		
		Counter c = new Counter();
		
		for (Relationship r : rels) {
			c.add(r.getType().name().toString());
		}
		return c;
	}

	public static HashSet<Vertex> getSourceNodes(Neo4jRdfGraph graph, Snippet s1) {

		HashSet<Vertex> nodes = new HashSet<Vertex>();
		StringBuffer nodesLabels = new StringBuffer();

		for (Vertex v : graph.getVertices("snippetId" + s1.getSnippetId(), 1)) {
			if (v.getProperty("sourceNode" + s1.getSnippetId()) != null) {
				nodes.add(v);
				if (log.isDebugEnabled())
					nodesLabels.append("Node(" + v.getProperty("label") + "," + v.getId() + ") ");
			}
		}

		log.debug("Source nodes for Doc{} ({}) fetched from database: {}", s1.getSnippetId(), s1.getTitle(),
		         nodesLabels);
		return nodes;
	}
	
	public static HashSet<Vertex> getSourceNodes(Neo4jRdfGraph graph) {
		
		HashSet<Vertex> nodes = new HashSet<Vertex>();
		
		for (Vertex v : graph.getVertices("sourceNode", 1))
			nodes.add(v);
		
		return nodes;
	}
	

	public static void exportGraphML(Neo4jRdfGraph graph, String outputFile) {
		exportGraphML(graph, outputFile, true, true);
	}

	public static void exportGraphML(Neo4jRdfGraph graph, String outputFile, boolean formatPretty, boolean cytoscape) {

		// Remove artificial node0 created by neo4j
		Vertex node0 = graph.getVertex(0);
		if (node0 != null) {
			node0.remove();
			log.debug("Artificial node0 created by neo4j removed");
		}

		// Optimize graph for cytoscape
		if (cytoscape) {
			for (Edge e : graph.getEdges()) {
				String fromNode = e.getVertex(Direction.OUT).getProperty("label");
				String toNode   = e.getVertex(Direction.IN).getProperty("label");
				e.setProperty("direction", fromNode +"--"+ toNode);
			}
		}

		graph.commit();

		// Check if output directory already exists
		File f = (new File(outputFile));
		if (f.exists())
			log.warn("Designated file for graphML already exists, will be overwritten: {}", f);

		try {

			GraphMLWriter writer = new GraphMLWriter(graph);

			// Necessary for GraphML schema validation
			writer.setEdgeLabelKey("type");

			// Work around for bug in cytoscape3 http://code.cytoscape.org/redmine/issues/1924
			Map<String, String> vertexKeyTypes = new HashMap<String, String>();
			
			vertexKeyTypes.put("sourceNode", GraphMLTokens.DOUBLE);
			vertexKeyTypes.put("label", GraphMLTokens.STRING);
			vertexKeyTypes.put("uri", GraphMLTokens.STRING);
			
			writer.setVertexKeyTypes(vertexKeyTypes);
			
			writer.setNormalize(true);

			writer.outputGraph(outputFile);

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Graph {} exported to graphml file {}", graph.actualGraphDBDirectory.getAbsolutePath(), outputFile);
	}

	public static void main(String[] args) {

		String neo4j = "/var/lib/neo4j/data/graph-leepincombe-2hops.db";
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(neo4j, false);

		Neo4jGraphUtils.exportGraphML(graph, "output/graphml.xml");
		// Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterGlobalPredObjIC());
		// Neo4jGraphUtils.removeDeadEndNodes(graph, true);
		// Counter c = Neo4jGraphUtils.getRelationshipStats(graph);
		// System.out.println(c.mostCommon());

		graph.shutdown();
	}

}
