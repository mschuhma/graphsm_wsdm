package de.uma.dws.graphsm.webservice;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Triple;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class DBPediaFilter {
	
	final static Logger log = LoggerFactory.getLogger(DBPediaFilter.class);
	private final static Configuration conf = ConfFactory.getConf();
	
	HashSet<String> categoriesFilter = null;
	HashSet<String> predFilter = null;
	
	public DBPediaFilter() {
		File filterFileCat = new File(conf.getString("dbpedia.filter.categories"));
		categoriesFilter = new HashSet<String>();
		
		for (ArrayList<String> v: MyFileReader.readXSVFile(filterFileCat, "", false)) {
			for (String s: v) {
				categoriesFilter.add(s);
			}
		}
		
		File filterFilePred = new File(conf.getString("dbpedia.filter.predicates"));
		predFilter = new HashSet<String>();
		
		for (ArrayList<String> v: MyFileReader.readXSVFile(filterFilePred, "", false)) {
			for (String s: v) {
				predFilter.add(s);
			}
		}
	}
	
	public static ArrayList<Triple> returnCopy(Collection<Triple> triple, HashSet<String> filter) {
		
		ArrayList<Triple> filteredTripleVector = new ArrayList<Triple>();
		int filterCounter = 0;
		for (Triple t: triple) {
			if (filter.contains( t.getObj()) || filter.contains(t.getSub()) || filter.contains(t.getPred()) ) {
				filterCounter++;
			} else {
				filteredTripleVector.add(t);
			}
		}
		log.debug("Filtered out {} of {} triple, {} remaining.", 
				filterCounter, triple.size(), filteredTripleVector.size());
		return filteredTripleVector;
	}
	
	
	public static HashSet<String> returnCopy(HashSet<String> uris, HashSet<String> filter) {
		
		HashSet<String> filteredUris = new HashSet<String>();
		int filterCounter = 0;
		for (String t: uris) {
			if (filter.contains(t)) {
				filterCounter++;
			} else {
				filteredUris.add(t);
			}
		}
		log.debug("Filtered out {} of {} URIs, {} remaining.", 
				filterCounter, uris.size(), filteredUris.size());
		return filteredUris;
	}
	
	public ArrayList<Triple> defaultCategoriesFilter (Collection<Triple> triple) {
		log.info("Filtering categories with {}", conf.getString("dbpedia.filter.categories"));
		return returnCopy(triple, categoriesFilter);
	}
	
	public HashSet<String> defaultCategoriesFilter (HashSet<String> uriList) {
		log.info("Filtering categories with {}", conf.getString("dbpedia.filter.categories"));
		return returnCopy(uriList, categoriesFilter);
	}
	
	public ArrayList<Triple> defaultPredicateFilter (Collection<Triple> triple) {
		log.info("Filtering categories with {}", conf.getString("dbpedia.filter.predicates"));
		return returnCopy(triple, predFilter);
	}
	
	public HashSet<String> defaultPredicateFilter (HashSet<String> uriList) {
		log.info("Filtering categories with {}", conf.getString("dbpedia.filter.predicates"));
		return returnCopy(uriList, predFilter);
	}
	
	public static void main(String[] args) {
		
		ArrayList<Triple> triple = new ArrayList<Triple>(3);
		
		triple.add(new Triple("http://test.org/1", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2002/07/owl#Class"));
		triple.add(new Triple("http://test.org/2", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://do.not.filter.org"));
		triple.add(new Triple("http://dbpedia.org/resource/Category:Topic", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://test.org/3"));
		
		System.out.println(triple);
		DBPediaFilter filter = new DBPediaFilter();
		
		triple = filter.defaultCategoriesFilter(triple);
		triple = filter.defaultCategoriesFilter(triple);
		
		System.out.println(triple);
	}

}
