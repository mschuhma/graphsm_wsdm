package de.uma.dws.graphsm.webservice;

import java.util.ArrayList;

import de.uma.dws.graphsm.datamodel.Triple;

public class DBPediaConceptHierarchy implements DBPediaEdgeSelector {

	@Override
	public ArrayList<Triple> get(String uri) {
		ArrayList<Triple> fetchedTriple = DBPedia.getDcTerm(uri);
		fetchedTriple.addAll(DBPedia.getRdfType(uri));
		fetchedTriple.addAll(DBPedia.getRdfsSubClassOf(uri));
		fetchedTriple.addAll(DBPedia.getSkosBroaderTerm(uri));
		return fetchedTriple;
	}
	
	//TODO nicer description
	public String toString() {
		return "DBPediaConceptHierarchy Edgle Selector uses getDcTerm, getRdfType, getRdfsSubClassOf, getSkosBroaderTerm";
	}
}
