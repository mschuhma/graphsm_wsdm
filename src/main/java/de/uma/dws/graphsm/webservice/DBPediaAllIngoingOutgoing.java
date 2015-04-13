package de.uma.dws.graphsm.webservice;

import java.util.ArrayList;

import de.uma.dws.graphsm.datamodel.Triple;

public class DBPediaAllIngoingOutgoing implements DBPediaEdgeSelector {

	@Override
	public ArrayList<Triple> get(String uri) {
		ArrayList<Triple> fetchedTriple = DBPedia.getAllOutgoingLinks(uri);
		fetchedTriple.addAll(DBPedia.getAllIngoingLinks(uri));
		return fetchedTriple;
	}
	
	//TODO nicer description
	public String toString() {
		return "DBPediaAllIngoingOutgoing Edgle Selector gets all ingoing (?s ?p uri) and outgoing (uri ?s ?o) links";
	}

}
