package de.uma.dws.graphsm.neo4j.compare;

import java.util.concurrent.ConcurrentHashMap;

import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Node;

public class PathCache {
	
	private static PathCache instance = null;
	
	private static ConcurrentHashMap<Node,ConcurrentHashMap<Node,WeightedPath>> cache = null;
	
	private PathCache() {
		cache = new ConcurrentHashMap<Node,ConcurrentHashMap<Node,WeightedPath>>();
	}
	
	public static PathCache getInstance() {
		if (instance == null)
			instance = new PathCache();
		return instance;
	}
	
	public WeightedPath get(Node n1, Node n2) {
		
		if (cache.contains(n1)) {
			return cache.get(n1).get(n2);
		} else {
			if (cache.contains(n2)) {
				return cache.get(n2).get(n1);
			}
			return null;
		}
	}
	
	public void put(Node n1, Node n2, WeightedPath path) {
		
		if (path == null)
			return;
		
		ConcurrentHashMap<Node, WeightedPath> map1 = cache.get(n1);
		ConcurrentHashMap<Node, WeightedPath> map2 = null;
		
		if (map1 == null) {
			map2 = new ConcurrentHashMap<Node, WeightedPath>();
			map2.put(n2, path);
			cache.put(n1, map2);
		} 
		else {
			map1.put(n2, path);
			}
		}

}
