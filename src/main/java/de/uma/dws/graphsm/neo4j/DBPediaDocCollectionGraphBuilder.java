package de.uma.dws.graphsm.neo4j;

import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uma.dws.graphsm.datamodel.Triple;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uma.dws.graphsm.webservice.Annotator;
import de.uma.dws.graphsm.webservice.DBPediaEdgeSelector;
import de.uma.dws.graphsm.webservice.DBPediaFilter;
import de.uma.dws.graphsm.webservice.DBPediaSpotlight;

public class DBPediaDocCollectionGraphBuilder {
	
	final static Logger log = 
				LoggerFactory.getLogger(DBPediaDocCollectionGraphBuilder.class);
	
	public final static Configuration	 conf = 
				ConfFactory.getConf();

	private Snippet	        s	            = null;

	private Neo4jRdfGraph	  graph	         = null;
	private ArrayList<String>	sourceNodeUris	= null;
	private static int	     docIdCounter	   = 0;

	private DBPediaFilter	  filter	         = null;
	private TripleWeighter	  tripleWeighter	= null;
	private Annotator			annotator		= null;
	
	public DBPediaDocCollectionGraphBuilder(Neo4jRdfGraph graph) {
		this.graph 			= graph;
		this.filter 		= new DBPediaFilter();
		this.annotator 	= new DBPediaSpotlight();
	}
	
	
	public DBPediaDocCollectionGraphBuilder(Neo4jRdfGraph graph, TripleWeighter tripleWeighter, Annotator annotator) {
		this.graph 				= graph;
		this.filter 			= new DBPediaFilter();
		this.tripleWeighter 	= tripleWeighter;
		this.annotator 		= annotator;
	}


	public DBPediaDocCollectionGraphBuilder addExpandedNetwork(DBPediaEdgeSelector selector, int hopsLimit) {
	
		if (sourceNodeUris == null)
			throw new RuntimeException("Source node URI list ist empty");
		int docNum;
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
			log.info("Exanding DBPedia source nodes from endpoint {}: hop {} of {}, using {}",
						conf.getString("dbpedia.sparql.url"), i, hopsLimit, selector);
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
			Vertex o = graph.addOrGetVertex("uri", t.getObj());
			
			//Add subgraph infos (query and snippet)
			String snippetId = "snippetId" + docNum;
			String queryId = "queryId" + queryNum; 
			v.setProperty(snippetId, 1);
			o.setProperty(queryId, 1);
			v.setProperty(snippetId, 1);
			o.setProperty(queryId, 1);
			
			//Add edge (pred)
			Edge e = graph.addOrGetEdge(v, o, t.getPred());
			if (tripleWeighter != null && e.getProperty("weight") == null)
				e.setProperty("weight", tripleWeighter.compute(t));
			e.setProperty(snippetId, 1);
			e.setProperty(queryId, 1);

		}
		graph.commit();
		return this;
	}
	
	public DBPediaDocCollectionGraphBuilder addSourceNodes(Snippet snippet) {
		
		assert s == null;
		s = snippet;
		String bow = s.getTextBowAsString();
		
		if (sourceNodeUris != null)
			throw new RuntimeException("Suspected Buug occured. Fix it now!");

		sourceNodeUris = annotator.annotate(bow);
		
		for (String uri: sourceNodeUris) {
			Vertex v = graph.addOrGetVertex("uri", uri);
			String snippetId = "snippetId" + s.getSnippetId();
			String queryId = "queryId" + s.getQueryId(); 
			v.setProperty(snippetId, 1);
			v.setProperty(queryId, 1);
			v.setProperty("sourceNode", 1);
		}
		graph.commit();
		return this;
	}
	
	
	public DBPediaDocCollectionGraphBuilder addSourceNodes(String document) {
		
		docIdCounter++;
		
		if (sourceNodeUris != null)
			throw new RuntimeException("Suspected Buug occured. Fix it now!");
		
		sourceNodeUris = annotator.annotate(document);
		
		for (String uri: sourceNodeUris) {
			
			Vertex v = graph.addOrGetVertex("uri", uri);
			
			String snippetId = "snippetId" + docIdCounter;
			v.setProperty(snippetId, 1);
			
			String sourceNode = "sourceNode" + docIdCounter;
			v.setProperty(sourceNode, 1);
			v.setProperty("sourceNode", 1);
		}
		graph.commit();
		return this;
	}
	
	public DBPediaDocCollectionGraphBuilder addExternalSourceNodes(ArrayList<String> nodes) {
		
		docIdCounter++;
		
		if (sourceNodeUris == null) {
			sourceNodeUris = nodes;
		}
		else {
			
			HashSet<String> set = new HashSet<String>();
			
			set.addAll(sourceNodeUris);
			set.addAll(nodes);
			
			sourceNodeUris.clear();
			sourceNodeUris.addAll(set);
		}

		for (String uri: sourceNodeUris) {
			
			Vertex v = graph.addOrGetVertex("uri", uri);
			
			String snippetId = "snippetId" + docIdCounter;
			v.setProperty(snippetId, 1);
			
			String sourceNode = "sourceNode" + docIdCounter;
			v.setProperty(sourceNode, 1);
			v.setProperty("sourceNode", 1);
		}
		graph.commit();
		return this;
	}
	
	public <T extends TripleWeighter> DBPediaDocCollectionGraphBuilder setEdgeWeight(T tripleWeighter) {
		log.info("Edge weighting activated: {}", tripleWeighter);
		this.tripleWeighter = tripleWeighter;
		return this;	
	}

}
