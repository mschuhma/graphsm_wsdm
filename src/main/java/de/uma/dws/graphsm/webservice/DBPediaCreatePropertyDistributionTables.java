package de.uma.dws.graphsm.webservice;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;

public class DBPediaCreatePropertyDistributionTables{ //extends Query{
	
	private static final String SERVER = "wifo5-32.informatik.uni-mannheim.de";
	private static final String USER = "dba";
	private static final String PASSWORD = "test1234";
	
	public static void sparqlQuery(String server, String user, String password, String outfile, String query,
            boolean shorten) throws SQLException, IOException {
			
			URIShortener shortener = 
			shorten ? new URIShortener.LODShortener() : new URIShortener.DummyShortener();
			
			DriverManager.registerDriver(new virtuoso.jdbc4.Driver());
			Connection conn = DriverManager
			.getConnection(String.format("jdbc:virtuoso://%s/UID=%s/PWD=%s/", server, user, password));
			Statement stmt = conn.createStatement();
			System.out.printf("Query: '%s'\n", query);
			ResultSet res = stmt.executeQuery("sparql " + query);
			
			CSVWriter writer = new CSVWriter(new FileWriter(outfile), '\t');
			
			String[] colNames = new String[res.getMetaData().getColumnCount()];
			for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
			colNames[i - 1] = res.getMetaData().getColumnName(i);
			}
			writer.writeNext(colNames);
			
			while (res.next()) {
				String[] row = new String[res.getMetaData().getColumnCount()];
				for (int i = 1; i <= res.getMetaData().getColumnCount(); i++) {
				row[i - 1] = shortener.shorten(res.getString(i));
				if (row[i -i] != null)
					row[i -i] = row[i -i].replace("http://dbpedia.org/resource/", "dbpedia:");
			}
			writer.writeNext(row);
			}
			res.close();
			stmt.close();
			conn.close();
			
			writer.close();
			}
	
	public static void instancePorpertyCount () {
		
		String outfile = "instancePropertyCount.csv";
		boolean shortener = true;
		
		String query = 	"select ?s ?p (count(?o) AS ?cnt) "+
						"from <http://dbpedia.org> "+
						"where { "+
						"?s ?p ?o. "+
						"FILTER(isURI(?o)) } "+
						"GROUP BY ?s ?p ";
		try {
			sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void porpertyCount () {
		
		String outfile = "propertyCount.csv";
		boolean shortener = true;
		
		String query = 	"select ?p (count(?o) AS ?cnt) "+
						"from <http://dbpedia.org> "+
						"where { "+
						"?s ?p ?o. "+
						"FILTER ( " +
						"REGEX(?sURIShortener, \"http://dbpedia.org/resource/*\") && " +
						"isURI(?o)) " +
						" } "+
						"GROUP BY ?p ";
		try {
			sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void porpertyObjectCount () {
		
		String outfile = "/home/mschuhma/local/propertyObjectCount.csv";
		boolean shortener = true;
		
		String query = 	"select ?p ?o count(?s) " +
						"from <http://dbpedia.org>  " +
						"where { ?s ?p ?o.  " +
						"FILTER ( " +
						   "REGEX(?s, \"http://dbpedia.org/resource/\") &&  " +
						   "isURI(?o) && " +
						   "!REGEX(?p, \"http://www.w3.org/2002/07/owl#sameAs\") && " +
						   "!REGEX(?p, \"http://dbpedia.org/ontology/wikiPageWikiLink\") && " +
						   "!REGEX(?p, \"http://dbpedia.org/ontology/wikiPageInterLanguageLink\") && " +
						   "!REGEX(?p, \"http://dbpedia.org/ontology/wikiPageExternalLink\") " +
						   " )  " +
						   " } " +
						"GROUP BY ?p ?o " +
						"HAVING (count(?s) > 1) ";
		
		try {
			sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Method for creating <prop, obj> counts. Mysql database schema to create manually:
	 * 
	 * CREATE TABLE IF NOT EXISTS `PropObjCombIC` (
	 * 			  `prop` varchar(53) COLLATE utf8_bin NOT NULL,
	 * 			  `obj` varchar(710) COLLATE utf8_bin NOT NULL,
	 * 			  `cnt` bigint(20) NOT NULL,
	 * 			  `IC` double NOT NULL,
	 * 			  KEY `prop` (`prop`),
	 * 			  KEY `obj` (`obj`(255)),
	 * 			  KEY `cnt` (`cnt`),
	 * 			  KEY `IC` (`IC`)
	 * 			) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
	 *  
	 *  Afterwards, the Information Content (IC) values get computed by: 
	 *   UPDATE PropObjCombIC SET IC = log(sum(cnt)/cnt);
	 */
	
	public static void fullPropertyObjectCount () {
		
		String outfile = "/home/mschuhma/local/fullPropertyObjectCountFiltered.csv";
		boolean shortener = true;
		
		String query = 	"select ?p ?o count(?s) " +
						"from <http://dbpedia.org>  " +
						"where { ?s ?p ?o.  " +
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
							"!REGEX(?p, \"^http://www.w3.org/2000/01/rdf-schema#suBClassOf$\") && " +
							"!REGEX(?p, \"^http://www.w3.org/2007/05/powder-s#describedby$\") && " +
							"!REGEX(?p, \"^http://www.w3.org/2002/07/owl#sameAs$\") && " +
							"!REGEX(?p, \"^http://www.w3.org/ns/prov#wasDerivedFrom$\") && " +
							"!REGEX(?p, \"^http://xmlns.com/foaf/0.1/primaryTopic$\") && " +
							"!REGEX(?p, \"^http://www.w3.org/2003/01/geo/wgs84_pos#lat$\") && " +
							"!REGEX(?p, \"^http://www.w3.org/2003/01/geo/wgs84_pos#long$\") && " +
							"!REGEX(?p, \"^http://dbpedia.org/resource/Template:\") " +
						    " )  " +
						   " } " +
						"GROUP BY ?p ?o " +
						"HAVING (count(?s) > 1) ";
		
		try {
			sparqlQuery(SERVER, USER, PASSWORD, outfile, query, shortener);
		} catch (SQLException | IOException e) {
			e.printStackTrace();
		}
	}
	
    public abstract static class URIShortener {
        public abstract String shorten(String uri);

        public static class LODShortener extends URIShortener {
            public LodURI lodUri = LodURI.getInstance();

            @Override
            public String shorten(String uri) {
                return lodUri.toPrefixedUri(uri);
            }
        }

        public static class DummyShortener extends URIShortener {
            @Override
            public String shorten(String uri) {
                return uri;
            }
        }
    }
	
	
	public static void main(String[] args) {
		fullPropertyObjectCount();
	}
}
