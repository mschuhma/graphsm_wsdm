package de.uma.dws.graphsm.io;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.Snippet;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class SnippetReader {

	/**
	 * @param args
	 */
	
	final static Logger log = LoggerFactory.getLogger(SnippetReader.class);
			
	public static HashMultimap<Integer,Snippet> fromFile(File inputFile) {
		ArrayList<ArrayList<String>> vals = MyFileReader.readXSVFile(inputFile, "\t", true);
		HashMultimap<Integer, Snippet> snippets = HashMultimap.create();
		for (ArrayList<String> v : vals) {
			if (v.size() == 4) {
				String[] s = v.get(0).split("\\.");
				Snippet snip = new Snippet(Integer.valueOf(s[0]), Integer.valueOf(s[1]), v.get(1), v.get(2), v.get(3));
				snippets.put(Integer.valueOf(s[0]), snip);
			}
			else if (v.size() == 3) {
				String[] s = v.get(0).split("\\.");
				Snippet snip = new Snippet(Integer.valueOf(s[0]), Integer.valueOf(s[1]), v.get(1), v.get(2), "");
				log.info("Loaded snippet with empty snippet text: {}", v);
				snippets.put(Integer.valueOf(s[0]), snip);
			}
			else {
				log.warn("Loaded correpted data, no snippet created: {}", v);
			}	
		}
		return snippets;
	}
	
	public static HashMultimap<Integer,Snippet> fromDefaultFile() {
		Configuration conf = ConfFactory.getConf();
		return fromFile(new File(
				conf.getString("input.snippets.path"), 
				conf.getString("input.snippets.results")));
	}
	
	public static void main(String[] args) {
		// Test reader
		System.out.println(fromDefaultFile());
		}

}
