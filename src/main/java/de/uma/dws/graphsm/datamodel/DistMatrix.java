package de.uma.dws.graphsm.datamodel;

public class DistMatrix<O1 extends Object, O2 extends Object, M> {
	
	public O1 obj1;
	public O2 obj2;
	public M distanceMatrix;
	
	public DistMatrix(O1 obj1, O2 obj2, M distanceMatrix) {
		this.obj1 = obj1;
		this.obj2 = obj2;
		this.distanceMatrix = distanceMatrix;
	}

	@Override
	public String toString() {
		return "DistMatrix [obj1=" + obj1 + ", obj2=" + obj2
				+ ", distanceMatrix=" + distanceMatrix + "]";
	}
		
}
