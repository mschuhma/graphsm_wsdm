package de.uma.dws.graphsm.datamodel;

public class Tuple<K, V> {
	
	public final K k;
	public final V v;
	
	public Tuple(K k, V v) {
		this.k = k;
		this.v = v;
	}
	
	public String toString() {
		return "(" + k.toString() +"\t" + v.toString() + ")";
	}
}
