package de.uma.dws.graphsm.webservice;

import java.util.ArrayList;

import de.uma.dws.graphsm.datamodel.Triple;

public interface DBPediaEdgeSelector {
	
	public ArrayList<Triple> get(String uri);
	
	public String toString(); //Implement description

}
