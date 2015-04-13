package de.uma.dws.graphsm.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Query implements Comparable<Query> {
	
	int queryId;
	String queryName;
	ArrayList<Snippet> snippets;

	public Query(int queryId, String queryName) {
		this.queryId = queryId;
		this.queryName = queryName;
		this.snippets = null;
	}

	public void addSnippet(Snippet s) {
		this.snippets.add(s);
	}
	
	public void getSnippet(int i) {
		this.snippets.get(i);
	}
	
	public ArrayList<Snippet> getSnippets() {
		return snippets;
	}

	public int compareTo(Query y) {
		return this.queryId - y.queryId;
	}

	public void addAllSnippet(Collection<Snippet> snippets) {
		if (this.snippets == null) {
			this.snippets = new ArrayList<Snippet>(snippets.size());
		}
		this.snippets.addAll(snippets);
		Collections.sort(this.snippets);
	}
	
	public String toString() {
		return "Query " + this.queryId +" ("+ this.queryName +"): Number of snippets "+ this.snippets.size();
	}

}
