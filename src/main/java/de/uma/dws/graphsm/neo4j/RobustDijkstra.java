package de.uma.dws.graphsm.neo4j;

import java.util.Iterator;
import java.util.concurrent.Callable;

import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.impl.core.NodeProxy;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RobustDijkstra implements Callable<WeightedPath> {
	
	final static Logger log = LoggerFactory.getLogger(RobustDijkstra.class);
	
	PathFinder<WeightedPath> dijkstra;
	Node n1;
	Node n2;
	RobustDijkstra(PathFinder<WeightedPath> dijkstra, Node n1, Node n2) {
		this.dijkstra = dijkstra;
		this.n1 = n1;
		this.n2 = n2;
	}

	@Override
	public WeightedPath call() throws Exception {
		WeightedPath p = dijkstra.findSinglePath(n1, n2);
		log.debug("Path between Node{} and Node{} found, Length {}, Weight {}, Weight per Step {}", 
				n1.getId(), n2.getId(), p.length(), p.weight(), p.weight()/p.length());
		Iterator<PropertyContainer> iter = p.iterator();
		StringBuffer bf = new StringBuffer();
		while (iter.hasNext()) {
			Object e = iter.next();
			if (e instanceof NodeProxy) {
				Node n = (Node) e;
				bf.append("Node("+n.getProperty("label")+")");
			}
			else if (e instanceof RelationshipProxy) {
				Relationship r = (Relationship) e;
				bf.append("-["+r.getType()+"]-");
			}
			else {log.error("ERROR");}
		}
		System.out.println(bf);
		System.out.println("End Dijkstra");
		return p;
	}
}