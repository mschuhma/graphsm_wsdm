package de.uma.dws.graphsm.datamodel;

public class JGraphTNode {
	
	public String name = null;
	public boolean sourceNode = false;
	
	public JGraphTNode(String name, boolean sourceNode) {
	   this.name = name;
	   this.sourceNode = sourceNode;
   }
	
	public JGraphTNode(String name, Object sourceNode) {
	   this.name = name;
	   if (sourceNode == null)
	   	this.sourceNode = false;
	   else 
	   	this.sourceNode = Double.valueOf(sourceNode.toString()).equals(1d);
   }

	@Override
   public int hashCode() {
	   final int prime = 31;
	   int result = 1;
	   result = prime * result + ((name == null) ? 0 : name.hashCode());
	   return result;
   }

	@Override
   public boolean equals(Object obj) {
	   
		if (this == obj)
		   return true;
	  
		if (obj == null)
		   return false;
	   
		if (getClass() != obj.getClass())
		   return false;
	  
		JGraphTNode other = (JGraphTNode) obj;
	   if (name == null) {
		   if (other.name != null)
			   return false;
	   }
	   
	   else if (!name.equals(other.name))
		   return false;
	   
	   return true;
   }

	@Override
   public String toString() {
	   return name;
   }


}
