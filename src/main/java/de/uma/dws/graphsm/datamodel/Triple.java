package de.uma.dws.graphsm.datamodel;

import org.apache.commons.configuration.Configuration;

import de.uma.dws.graphsm.ConfFactory;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;

public class Triple{
	
	final static Configuration conf = ConfFactory.getConf();
	
	String sub;
	String pred;
	String obj;

	public Triple(String sub, String pred, String obj) {
		super();
		this.sub = sub.trim();
		this.pred = pred.trim();
		this.obj = obj.trim();
	}
	
	public Triple(String pred, String obj) {
		super();
		this.sub = null;
		this.pred = pred.trim();
		this.obj = obj.trim();
	}
	
	@Override
	public String toString() {
		LodURI lod = LodURI.getInstance(conf.getString("misc.prefixcc.file"));
		return "Triple " + (sub != null ? "" + lod.toPrefixedUri(sub) + " " : "?sub, ")
				+ (pred != null ? "" + lod.toPrefixedUri(pred) + " " : "")
				+ (obj != null ? "" + lod.toPrefixedUri(obj) : "");
	}
	
	public boolean equals(Triple o) {
		return (this.sub.equals(o.sub) &
				this.pred.equals(o.pred) &
				this.pred.equals(o.obj));
	}
	
	public Triple addSubj(String sub) {
		assert this.sub == null;
		this.sub = sub.trim();
		return this;
	}
	
	public int hashCodeSPO() {
		return (this.sub+this.pred+this.obj).hashCode();
	}
	
	public int hashCode() {
		return hashCodeSPO();
	}
	
	public int hashCodePO() {
		return (this.sub+this.pred+this.obj).hashCode();
	}

	public String getSub() {return sub;}

	public void setSub(String sub) {this.sub = sub;}

	public String getPred() {return pred;}

	public void setPred(String pred) {this.pred = pred;}

	public String getObj() {return obj;}

	public void setObj(String obj) {this.obj = obj;}


}
