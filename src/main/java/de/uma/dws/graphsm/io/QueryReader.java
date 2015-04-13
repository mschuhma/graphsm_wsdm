package de.uma.dws.graphsm.io;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class QueryReader {
	
	final static Logger log = LoggerFactory.getLogger(QueryReader.class);
	
	public static ArrayList<ArrayList<String>> fromFile(File inputFile) {
		ArrayList<ArrayList<String>> vals = MyFileReader.readXSVFile(inputFile, "\t", true);
		return vals;
	}
	
	
	public static ArrayList<ArrayList<String>> fromDefaultFile() {
		Configuration conf = null;
		try {
			conf = new PropertiesConfiguration("default.graphsm.conf");
		} catch (ConfigurationException e) {
			log.warn("Configuration file not found");
			log.warn(e.toString());
		}
		return fromFile(new File(
				conf.getString("input.snippets.path"), 
				conf.getString("input.snippets.topics")));
	}
	
	public static void main(String[] args) {
		// Test reader
		System.out.println(fromDefaultFile());
		}
	

}
