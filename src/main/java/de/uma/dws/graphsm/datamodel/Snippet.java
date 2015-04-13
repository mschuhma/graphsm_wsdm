package de.uma.dws.graphsm.datamodel;

import java.util.ArrayList;

public class Snippet implements Comparable<Snippet> {
	
	int queryId;
	int snippetId;
	String url;
	String title;
	String text;
	private ArrayList<String> textBow;
	
	public Snippet(int queryId, int snippetId, String url, String title, String text) {
		this.snippetId = snippetId;
		this.url = url;
		this.title = title;
		this.text = text;
	}

	public int getQueryId() {
		return queryId;
	}

	public int getSnippetId() {
		return snippetId;
	}

	public ArrayList<String> getTextBow() {
		return textBow;
	}
	
	public String getText() {
		return text;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getTextBowAsString() {
		StringBuffer bf = new StringBuffer();
		for (String s: textBow) {
			bf.append(s);
			bf.append(" ");
		}
		return bf.toString();
	}

	public void setTextBow(ArrayList<String> textBow) {
		this.textBow = textBow;
	}

	public int compareTo(Snippet y) {
		return this.snippetId - y.snippetId;
	}
	
	public String toString() {
		return "" + this.snippetId +", "+ this.url +", "+ this.title;
	}

	public void setTextBow(String string) {
		ArrayList<String> s = new ArrayList<String>();
		s.add(string);
		setTextBow(s);
	}

}
