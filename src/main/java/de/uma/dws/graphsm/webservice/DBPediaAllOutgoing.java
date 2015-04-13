package de.uma.dws.graphsm.webservice;

import java.util.ArrayList;

import de.uma.dws.graphsm.datamodel.Triple;

public class DBPediaAllOutgoing implements DBPediaEdgeSelector {

	@Override
	public ArrayList<Triple> get(String uri) {
		ArrayList<Triple> fetchedTriple = DBPedia.getAllOutgoingLinks(uri);
		return fetchedTriple;
	}
	
	//TODO nicer description
	public String toString() {
		return "DBPediaAllOutgoing Edgle Selector gets all outgoing (uri ?s ?o) links";
	}

}
