package de.uma.dws.graphsm.neo4j;

import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uma.dws.graphsm.webservice.Annotator;
import de.uma.dws.graphsm.webservice.DBPediaEdgeSelector;
import de.uma.dws.graphsm.webservice.DBPediaFilter;
import de.uma.dws.graphsm.webservice.DBPediaSpotlight;

public class DBPediaSingleDocGraphBuilder {
	
	final static Logger log = LoggerFactory.getLogger(DBPediaSingleDocGraphBuilder.class);

	private Snippet s = null;

	private Neo4jRdfGraph graph = null;
	private ArrayList<String> sourceNodeUris = null;
	private static int docIdCounter = 0;

	private DBPediaFilter filter = null;
	private TripleWeighter tripleWeighter = null;
	private Annotator annotator = null;
	
	public DBPediaSingleDocGraphBuilder(Neo4jRdfGraph graph) {
		this.graph 				= graph;
		this.annotator 		= new DBPediaSpotlight();
		this.filter 			= new DBPediaFilter();
	}
	
	public DBPediaSingleDocGraphBuilder(Neo4jRdfGraph graph, TripleWeighter tripleWeighter, Annotator annotator) {
		this.graph 				= graph;
		this.tripleWeighter 	= tripleWeighter;
		this.annotator 		= annotator;
		this.filter 			= new DBPediaFilter();
	}
	
	public DBPediaSingleDocGraphBuilder addExpandedNetwork(DBPediaEdgeSelector selector, int hopsLimit) {
	
		assert sourceNodeUris != null;
		@SuppressWarnings("unused")
      int docNum;
		@SuppressWarnings("unused")
      int queryNum;
		
		if (s == null) {
			docNum = docIdCounter;
			queryNum = 0;
			
		} else {
			docNum = s.getSnippetId();
			queryNum = s.getQueryId();
		}
		
		//Filter already fetched URIs for duplicates and filter lists
		HashSet<String> currentUris = new HashSet<>();
		currentUris.addAll(sourceNodeUris);
		
		currentUris = filter.defaultCategoriesFilter(currentUris);
		currentUris = filter.defaultPredicateFilter(currentUris);
		
		HashSet<Triple> newTriple = new HashSet<Triple>();
		
		for (int i = 1; i <= hopsLimit; i++) {
			log.info("Exanding DBPedia source nodes: hop {} of {}, using {}", i, hopsLimit, selector);
			ArrayList<Triple> fetchedTriple = new ArrayList<Triple>();
			for (String uri: currentUris) {
				fetchedTriple.addAll(selector.get(uri));
			}
			
			//Filter fetched triple
			fetchedTriple =  filter.defaultCategoriesFilter(fetchedTriple);
			fetchedTriple =  filter.defaultPredicateFilter(fetchedTriple);
			
			//Add all sub and obj of fetched triples to to-fetch-list (currentUris)
			currentUris.clear();
			for (Triple t: fetchedTriple) {
				currentUris.add(t.getSub());
				currentUris.add(t.getObj());
			}
			log.info("Expansion hop {} extracted {} triple", i, fetchedTriple.size());
			newTriple.addAll(fetchedTriple);
		}
		
		//Filter newly fetch triples
		//TODO Make Filter selection modular EdgeSelector
		ArrayList<Triple> triple = null;
		
		triple = filter.defaultCategoriesFilter(newTriple);
		triple = filter.defaultPredicateFilter(triple);
		
		//Add all triple to graph database
		for (Triple t : ImmutableSet.copyOf(triple)) {
			
			//Add vertices (sub, obj)
			Vertex v = graph.addOrGetVertex("uri", t.getSub());
			if (v.getProperty("sourceNode") == null)
				v.setProperty("sourceNode", 0);
			Vertex o = graph.addOrGetVertex("uri", t.getObj());
			if (o.getProperty("sourceNode") == null)
				o.setProperty("sourceNode", 0);
			
			//Add subgraph infos (query and snippet)
//			v.setProperty("snippetId", docNum);
//			v.setProperty("snippetId",docNum);
			
			//Add edge (pred)
			Edge e = graph.addOrGetEdge(v, o, t.getPred());
			
			//Add edge weight
			if (tripleWeighter != null && e.getProperty("weight") == null)
				e.setProperty("weight", tripleWeighter.compute(t));
//			e.setProperty("snippetId", docNum);

		}
		graph.commit();
		return this;
	}
	
	public DBPediaSingleDocGraphBuilder addSourceNodes(Snippet snippet) {
		
		assert s == null;
		s = snippet;
		
		String bow = s.getTextBowAsString();
		
		if (sourceNodeUris != null)
			throw new RuntimeException("Suspected Buug occured. Fix it now!");

		sourceNodeUris = annotator.annotate(bow);
		
		for (String uri: sourceNodeUris) {
			Vertex v = graph.addOrGetVertex("uri", uri);
//			v.setProperty("snippetId", s.getSnippetId());
			v.setProperty("sourceNode", 1);
		}
		graph.commit();
		return this;
	}
	

	public DBPediaSingleDocGraphBuilder addSourceNodes(String document) {
		
		if (sourceNodeUris != null)
			throw new RuntimeException("Suspected Buug occured. Fix it now!");
		
		sourceNodeUris = annotator.annotate(document);
		
		for (String uri: sourceNodeUris) {
			
			Vertex v = graph.addOrGetVertex("uri", uri);	
//			v.setProperty("snippetId", docIdCounter);	
			v.setProperty("sourceNode", 1);
		}
		graph.commit();
		return this;
	}
	
//	public DBPediaDocCollectionGraphBuilderDense addExternalSourceNodes(ArrayList<String> nodes) {
//		
//		docIdCounter++;
//		
//		sourceNodeUris = nodes;
//		
//		for (String uri: sourceNodeUris) {
//			
//			Vertex v = graph.addOrGetVertex("uri", uri);
//			
//			String snippetId = "snippetId" + docIdCounter;
//			v.setProperty(snippetId, 1);
//			
//			String sourceNode = "sourceNode" + docIdCounter;
//			v.setProperty(sourceNode, 1);
//			v.setProperty("sourceNode", 1);
//		}
//		graph.commit();
//		return this;
//	}
	
	public <T extends TripleWeighter> DBPediaSingleDocGraphBuilder setEdgeWeight(T tripleWeighter) {
		log.info("Edge weighting activated: {}", tripleWeighter);
		this.tripleWeighter = tripleWeighter;
		return this;	
	}
	
	public <T extends Annotator> DBPediaSingleDocGraphBuilder setAnnotator(T annotator) {
		log.info("Annotator activated: {}", annotator);
		this.annotator = annotator;
		return this;	
	}

}
