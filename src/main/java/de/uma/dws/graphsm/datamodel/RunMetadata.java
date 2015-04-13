package de.uma.dws.graphsm.datamodel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;

import de.uma.dws.graphsm.io.QueryReader;
import de.uma.dws.graphsm.io.SnippetReader;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;

public class RunMetadata {
	
	List<Query> queries;
	String runDateString;
	Date runDate;
	
	final static Logger log = LoggerFactory.getLogger(RunMetadata.class);

	public RunMetadata() {
		this.runDate = new Date();
		this.runDateString = (new SimpleDateFormat("yyyy-MM-dd-HH-mm")).format(this.runDate);
		this.queries = new ArrayList<Query>(158);
	}
	
	public RunMetadata loadQueries() {
		ArrayList<ArrayList<String>> inputQueries = QueryReader.fromDefaultFile();
		HashMultimap<Integer, Snippet> inputSnippets = SnippetReader.fromDefaultFile();
		
		this.queries = new ArrayList<Query>(inputQueries.size());	
		for (ArrayList<String> q : inputQueries) {
			Integer queryId = Integer.valueOf(q.get(0));
			Query query = new Query(queryId, q.get(1));
			query.addAllSnippet(inputSnippets.get(queryId));
			this.queries.add(query);
		}
		return this;
	}
	
	public List<Query> getQueries() {
		return this.queries;
	}
	
	public Query getQuery(int queryId) {
		Query q = this.queries.get(queryId);
		assert q.queryId == queryId;
		return q;
	}
	
	public String toString() {
		return "Current run is " + this.runDateString +". Queries loaded " + this.queries.size();  
	}
	
	public RunMetadata loadDevQuery() {
		this.loadQueries();
		this.queries = this.queries.subList(0, 1);
		
		File  f = new File("src/main/resources/dataset/Moresque/semeval_q1_aida_cleaned_snippets.txt");
		ArrayList<String> line = MyFileReader.readLinesFile(f);
		
		for (Query q : this.queries) {
			for (int i = 0; i < q.getSnippets().size(); i++){
				q.getSnippets().get(i).setTextBow(line.get(i));
			}
		}
		log.info("Development QueryRun created: {}, {}", this, this.queries.get(0));
		return this;
	}
	
	public static void main (String[] args) {
		RunMetadata devRun = new RunMetadata();
		devRun.loadDevQuery();
		System.out.println(devRun);
	}

}
