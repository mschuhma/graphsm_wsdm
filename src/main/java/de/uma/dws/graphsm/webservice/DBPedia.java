package de.uma.dws.graphsm.webservice;

import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.sparql.ARQException;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Triple;

public class DBPedia {

	/**
	 * @param args
	 */
	
	final static Logger log = LoggerFactory.getLogger(DBPedia.class);
	final static Configuration conf = ConfFactory.getConf();
	
	final static String SPARQLENDPOINT = conf.getString("dbpedia.sparql.url");
	
	final static String NAMESPACES = 
										"PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" 		+ "\n" +
		       						 "PREFIX rdfs: 	<http://www.w3.org/2000/01/rdf-schema#> " 			+ "\n" +
								       "PREFIX dcterms: <http://purl.org/dc/terms/>" 						+ "\n" +
								       "PREFIX owl: 	<http://www.w3.org/2002/07/owl#>" 					+ "\n" +
								       "PREFIX skos:	<http://www.w3.org/2004/02/skos/core#>" 			+ "\n";
	
	public DBPedia() {};
	
	public static ArrayList<Triple> getAllOutgoingLinks(String dbPediaUri) {
		return generalSpoQuery(dbPediaUri, "?pred", "?obj");
	}
	
	public static ArrayList<Triple> getAllIngoingLinks(String dbPediaUri) {
		return generalSpoQuery("?subj", "?pred", dbPediaUri);
	}
	
	public static ArrayList<Triple> generalSpoQuery(String subjIn, String predIn, String objIn) {
		//Variables are ?subj, ?pred, ?obj.
		//No <> around URIs needed
		//Only returns <URI,URI,URI> triples, triples with literals are ignored
		
		String q = (
				NAMESPACES + 
				"SELECT * WHERE { " +
				(subjIn.contains("://") ? "<"+subjIn+">" : subjIn) + " " +
				(predIn.contains("://") ? "<"+predIn+">" : predIn) + " " +
				(objIn.contains("://") 	? "<"+objIn+">"  : objIn)  + " " +
				".}");
		
//		System.out.println(q);
		
		QueryExecution qe;
		ArrayList<Triple> allTriple = new ArrayList<Triple>();
		
		try {
			qe = QueryExecutionFactory.sparqlService(
					SPARQLENDPOINT,
					QueryFactory.create(q, Syntax.syntaxSPARQL_11));
			qe.setTimeout(1000*conf.getInt("dbpedia.conn.timeout.sec"));
			
		} catch (ARQException e) {
			log.warn("Skipping generalSpoQuery as sparql query creation failed: {}", e);
			log.warn("Query: {}", q);
			log.warn("SPARQL Endpoint at {}", SPARQLENDPOINT);
			return allTriple;
		}
		
		int i = 0;
		while (i < conf.getInt("dbpedia.conn.retries")) {
			i++;
			try {
				Thread.sleep(conf.getInt("dbpedia.conn.pauses.each.conn.msec"));
			    ResultSet rs = qe.execSelect();
			    while ( rs.hasNext() ) {
			    	QuerySolution qs = rs.next();
			    	//Now skipping <URI,URI,Literal> triples
			    	if (objIn.equals("?obj") && !qs.get("obj").isURIResource()) {
			    		//log.debug("Skipping {} {} {}", qs.get("sub"), qs.get("pred"), qs.get("obj"));
			    		continue;
			    		}
			    	Triple t = new Triple(
			    			(subjIn.equals("?subj")) 	? qs.getResource("subj").getURI() : subjIn,
			    			(predIn.equals("?pred")) 	? qs.getResource("pred").getURI() : predIn,
			    			(objIn.equals("?obj"))		? qs.getResource("obj").getURI()  : objIn);
			    	allTriple.add(t);
			    	}
				}
			catch(Exception e) {
				int wait = conf.getInt("dbpedia.conn.wait.after.error.sec");
				log.info("Connection error for {} {} {} : {}", subjIn, predIn, objIn, e.toString());
				log.info("Waiting for {} sec for retry {}", wait, i);
				log.warn("SPARQL Endpoint at {}", SPARQLENDPOINT);
				try {Thread.sleep(1000*wait);} 
				catch (InterruptedException e1) {log.warn(e1.toString());}
				continue;
				}
			qe.close();
			return allTriple;
		}
		log.error("DBPedia connection failed ultimately after {} attempts: {} {} {}.", i, subjIn,predIn,objIn);
		log.warn("SPARQL Endpoint at {}", SPARQLENDPOINT);
		return allTriple; //useless code
	}
	
	public static ArrayList<Triple> getRdfType(String dbPediaUri) {
		return generalSpoQuery(dbPediaUri, "rdf:type", "?obj");
		//TODO Custom filtering here
	}
	
	public static ArrayList<Triple> getSkosBroaderTerm(String dbPediaUri) {
		ArrayList<Triple> skosBroader 	 = generalSpoQuery(dbPediaUri, "skos:broader", "?obj");
		ArrayList<Triple> skosBroaderOf  = generalSpoQuery(dbPediaUri, "skos:broaderOf", "?obj");
		skosBroader.addAll(skosBroaderOf);
		return skosBroader;
		//TODO Custom filtering here
	}

	public static ArrayList<Triple> getDcTerm(String dbPediaUri) {
		return generalSpoQuery(dbPediaUri, "dcterms:subject", "?obj");
		//TODO Custom filtering here
	}
	
	public static ArrayList<Triple> getRdfsSubClassOf(String dbPediaUri) {
		return generalSpoQuery(dbPediaUri, "rdfs:subClassOf", "?obj");
		//TODO Custom filtering here
	}
	
	public static Double getPredWeight(String predIn, String objIn) {
		predIn = (predIn.contains("://")) ? "<"+predIn+">" : predIn;
		objIn = (objIn.contains("://")) ? "<"+objIn+">" : objIn;
		String q = (
				NAMESPACES + 
				"SELECT (COUNT(*) AS ?cnt) WHERE { " +
				"?subj "+predIn+" "+objIn+".}");
		//System.out.println(q);
		QueryExecution qe;
		try {
			qe = QueryExecutionFactory.sparqlService(
					SPARQLENDPOINT,
					QueryFactory.create(q, Syntax.syntaxSPARQL_11));
			qe.setTimeout(1000*conf.getInt("dbpedia.conn.timeout.sec"));
		} catch (ARQException e) {
			log.warn("Skipping getPredWeight request as sparql query creation failed: {}", e);
			log.warn("Query: {}", q);
			return Double.NaN;
		}
		
		int i = 0;
		while (i < conf.getInt("dbpedia.conn.retries")) {
			i++;
			try {
				Thread.sleep(conf.getInt("dbpedia.conn.pauses.each.conn.msec"));
			    ResultSet rs = qe.execSelect();
			    if ( rs.hasNext() ) {
			    	QuerySolution qs = rs.next();
			    	Literal lit = qs.getLiteral("cnt");
			    	return lit.getDouble();
			    } else {
			    	log.warn("Emtpy result set for getPredWeight.");
			    	return Double.NaN;
				}
			}
			catch(Exception e) {
				int wait = conf.getInt("dbpedia.conn.wait.after.error.sec");
				log.info("Waiting for {} sec for retry {}", wait, i);
				try {Thread.sleep(1000*wait);} 
				catch (InterruptedException e1) {log.warn(e1.toString());}
				continue;
				}
			finally {
				qe.close();
			}
		}
		log.error("DBPedia connection failed ultimately after {} attempts: Pred Count for {} {}.", i, predIn,objIn);
		return Double.NaN; //useless code
	}

	public static Double getPredWeight(String predIn) {
		return getPredWeight(predIn, "?obj");
	}
	
	public static void main(String[] args) {
		
/////////////////// Test predicate weights //////////////////////
//		System.out.println(getPredWeight(
//				"dcterms:subject", 
//				"http://dbpedia.org/resource/Category:German_politicians"));
//		System.out.println(getPredWeight(
//				"dcterms:subject"));
//		System.exit(0);
////////////////////////////////////////////////////////////////
		
		
/////////////////// Test DBPedia instance query (in- and outgoing) //////////////////////
//		final String instanceUri = "http://dbpedia.org/resource/Giuseppe_Verdi";
//		ArrayList<Triple> v1 = new ArrayList<Triple>();
//		v1.addAll(DBPedia.getAllOutgoingLinks(instanceUri));
//		v1.addAll(DBPedia.getAllIngoingLinks(instanceUri));
//		TripleWeighter tw = new DBPediaGlobalEdgeWeight();
//		for (Triple t: v1) {
//			tw.compute(t);
//		}
//		System.exit(0);
////////////////////////////////////////////////////////////////
	
		
/////////////////// Demonstrating DBPedia Error //////////////////////
//		String q =
//		"PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
//		"PREFIX rdfs: 	<http://www.w3.org/2000/01/rdf-schema#> " +
//		"PREFIX dcterms: <http://purl.org/dc/terms/> " +
//		"PREFIX owl: 	<http://www.w3.org/2002/07/owl#> " +
//		"PREFIX skos:	<http://www.w3.org/2004/02/skos/core#> " +
//		"SELECT * WHERE { <http://dbpedia.org/class/yago/JazzRecordLabels> ?pred ?obj .} ";
//		
//		QueryExecution qe = QueryExecutionFactory.sparqlService(
//				"http://dbpedia.org/sparql",
//				QueryFactory.create(q, Syntax.syntaxSPARQL_11));
//		ResultSet rs = qe.execSelect();
//	    while ( rs.hasNext() ) {
//	    	QuerySolution qs = rs.next();
//	    	System.out.print(qs.getResource("pred").getURI());
//	    	System.out.println(" | " + qs.get("obj"));
//	    }
////////////////////////////////////////////////////////////////

		
/////////////////// Test Local DBPedia Endpoint //////////////////////
		String q =
		"PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
		"PREFIX rdfs: 	<http://www.w3.org/2000/01/rdf-schema#> " +
		"PREFIX dcterms: <http://purl.org/dc/terms/> " +
		"PREFIX owl: 	<http://www.w3.org/2002/07/owl#> " +
		"PREFIX skos:	<http://www.w3.org/2004/02/skos/core#> " +
		"select distinct ?sub where {?sub a ?class. ?class rdfs:subClassOf owl:Thing}";
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://wifo5-32.informatik.uni-mannheim.de:8890/sparql",
				QueryFactory.create(q, Syntax.syntaxSPARQL_11));
		ResultSet rs = qe.execSelect();
	    int cnt = 0;
		while ( rs.hasNext() ) {
	    	QuerySolution qs = rs.next();
	    	cnt++;
	    	System.out.println(cnt + "\t" + qs.getResource("sub").getURI());
	    }
		
/////////////////// Test DBPedia instance query (outgoing rdf:type) //////////////////////
//		final String instanceUri = "http://dbpedia.org/resource/Pl%C3%A1cido_Domingo";
//		
//		ArrayList<Triple> v1 = DBPedia.getRdfType(instanceUri);
//		System.out.println("Triple v1:");
//		System.out.println(v1);
//		
//		ArrayList<Triple> v2 = DBPedia.getDcTerm(instanceUri);
//		System.out.println("Triple v2:");
//		System.out.println(v2);
//		
//		Counter c = new Counter();
//		c.addAll(v1);
//		c.addAll(v2);
//		c.addAll(v1);
//		System.out.println("Counter of Triple 2*v1 and 1*v2:");
//		System.out.println(c);
//		
//		ArrayList<Triple> v3 = DBPedia.getAllOutgoingLinks(instanceUri);
//		System.out.println("Triple v3:");
//		System.out.println(v3);
//		System.exit(0);
////////////////////////////////////////////////////////////////
	}

}
