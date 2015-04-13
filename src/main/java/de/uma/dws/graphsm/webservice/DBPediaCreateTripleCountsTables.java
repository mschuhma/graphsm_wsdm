package de.uma.dws.graphsm.webservice;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uni_mannheim.informatik.dws.dwslib.cli.QueryCLI;


/**
 * Class contains method for computing different count statistics for the DBpedia. Execute the methods and obtain a .tab
 * data file, than create the MySQL tables needed (schema given below with each method) and load the created data
 * directly into the database using the following command (adapt file names accordingly): 
 * <pre> {@code 
 * load data infile '/var/lib/mysql/_tmp_input/ObjectCount.tab'
 * INTO TABLE dbpcounts.instprop 
 * FIELDS TERMINATED BY '\t' enclosed by '"' 
 * LINES TERMINATED BY '\n';
 * }
 * </pre>
 * 
 */

public class DBPediaCreateTripleCountsTables{
	
	//Using dwslib version 38
	
	public final static Logger	       log	= LoggerFactory.getLogger(DBPediaCreateTripleCountsTables.class);
	public final static Configuration	conf	= ConfFactory.getConf();
	
	private static final String SERVER   = conf.getString("dbpedia.virtuoso.server");
	private static final String USER     = conf.getString("dbpedia.virtuoso.user");
	private static final String PASSWORD = conf.getString("dbpedia.virtuoso.password");
	
	private static final String TODAY	= (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
	
	//Regex filer list needs to be synchronized with predicate/ object filters used for graph creation!
	//Current filter filter is DBPediaStopUris_Preds.txt
	private static final String FILTER = 
				"FILTER ( " +
			   "isURI(?o) && " +
				"!REGEX(?p, \"^http://purl.org/dc/elements/1.1/language$\") && " +
				"!REGEX(?p, \"^http://purl.org/dc/elements/1.1/rights$\") && " +
				"!REGEX(?p, \"^http://purl.org/dc/elements/1.1/description$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/wikiPageExternalLink$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/wikiPageWikiLink$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/wikiPageRedirects$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/wikiPageInterLanguageLink$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/thumbnail$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/ontology/wikiPageDisambiguates$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/property/wikiPageUsesTemplate$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/property/url$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/property/filetype$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/property/format$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2000/01/rdf-schema#suBClassOf$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2000/01/rdf-schema#seeAlso$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2007/05/powder-s#describedby$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2002/07/owl#sameAs$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/ns/prov#wasDerivedFrom$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2003/01/geo/wgs84_pos#lat$\") && " +
				"!REGEX(?p, \"^http://www.w3.org/2003/01/geo/wgs84_pos#long$\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/resource/Template:\") && " +
				"!REGEX(?p, \"^http://dbpedia.org/property/hasPhotoCollection$\") && " +
				"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/primaryTopic$\") && " +
				"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/isPrimaryTopicOf$\") && " +
				"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/homepage$\") && " +
				"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/thumbnail$\") && " +
				"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/depiction$\") " +
			    " )  ";
		
	
	/**
	 * Compute object counts from DBpedia via local Virtuoso server. Creates file that need to be uploaded via MySQL
	 * command line insert into new table with schema:
	 * 
	 * <pre>
	 * {@code 
	 * CREATE TABLE IF NOT EXISTS `ObjCnt` (
	 *    `obj` varchar(1024) COLLATE utf8_bin NOT NULL, 
	 *    `cnt` int(10) unsigned NOT NULL
	 *    ) 
	 *  ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
	 * }
	 * </pre>
	 */
	
	public static void ObjectCount () {
		
		String outfile = "/home/mschuhma/local/" + TODAY + "-" + "ObjectCnt.tab";
		boolean shortener = true;
		
		String query = 	"select ?o count(?s) " +
								"from <http://dbpedia.org>  " +
									"where { ?s ?p ?o.  " +
										FILTER +
									" } " +
								"GROUP BY ?o ";
		
		try {
			QueryCLI.sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
			log.info("ObjectCount successfully written to file {}", outfile);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compute predicate counts from DBpedia via local Virtuoso server. Creates file that need to be uploaded via MySQL
	 * command line insert into new table with schema:
	 * 
	 * <pre>
	 * {@code 
	 * CREATE TABLE IF NOT EXISTS `PredCnt` (
	 *   `pred` varchar(255) COLLATE utf8_bin NOT NULL, 
	 *   `cnt` int(10) unsigned NOT NULL
	 *   ) 
	 *  ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
	 * }
	 * </pre>
	 */
	
	public static void PredicateCount () {
		
		String outfile = "/home/mschuhma/local/" + TODAY + "-" + "PredicateCnt.tab";
		boolean shortener = true;
		
		String query = "select ?p count(?s) " + 
							"from <http://dbpedia.org>  " + 
								"where { ?s ?p ?o.  " + 
									FILTER + 
								" } "+ 
							"GROUP BY ?p ";
				
		try {
			QueryCLI.sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
			log.info("PredicateCount successfully written to file {}", outfile);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Compute <predicate,object> counts from DBpedia via local Virtuoso server. Creates file that need to be uploaded via MySQL
	 * command line insert into new table with schema:
	 * 
	 * <pre>
	 * {@code 
	 * CREATE TABLE IF NOT EXISTS `PredObjCnt` (
	 *   `pred` varchar(255) COLLATE utf8_bin NOT NULL,
	 *   `obj` varchar(1024) COLLATE utf8_bin NOT NULL,
	 *   `cnt` int(10) unsigned NOT NULL
	 *   ) 
	 *  ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
	 * }
	 * </pre>
	 */
	
	public static void PredicateObjectCount() {

		String outfile = "/home/mschuhma/local/" + TODAY + "-" + "PredObjCnt.tab";
		boolean shortener = true;

		String query = "select ?p ?o count(?s) " + 
							"from <http://dbpedia.org>  " + 
								"where { ?s ?p ?o.  " + 
										FILTER + 
										" } "+ 
							"GROUP BY ?p ?o " + 
							"HAVING (count(?s) > 1) ";

		try {
			QueryCLI.sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
			log.info("PredicateObjectCount successfully written to file {}", outfile);
		}
		catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		
		long start = System.currentTimeMillis();
		
		try {
	      PredicateCount();
	      System.out.println("Processing time PredicateCount " + (System.currentTimeMillis() - start)/(1000.0*60) + " mins.");
      }
      catch (Exception e) {
	      e.printStackTrace();
      }
		
		try {
	      ObjectCount();
	      System.out.println("Processing time ObjectCount " + (System.currentTimeMillis() - start)/(1000.0*60) + " mins.");
      }
      catch (Exception e) {
	      e.printStackTrace();
      }
		
		try {
	      PredicateObjectCount();
	      System.out.println("Processing time PredicateObjectCount " + (System.currentTimeMillis() - start)/(1000.0*60) + " mins.");
      }
      catch (Exception e) {
	      e.printStackTrace();
      }
		
	}
}
