package de.uma.dws.graphsm.experiments;

import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.main.BuildSeparateDocGraphs;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterAddedIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterJointIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterPMIPlusIC;
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;

@SuppressWarnings("unused")
public class Exp130723OneGraphOneDocGraphML {

	public final static Logger	       log	= LoggerFactory.getLogger(Exp130723OneGraphOneDocGraphML.class);
	public final static Configuration	conf	= ConfFactory.getConf();
	
	final static String LP50_TEXT_SAMPLE 			= "src/main/resources/dataset/LeePincombe/documents.development.data";
	final static String LP50_TEXT 					= "src/main/resources/dataset/LeePincombe/documents.cleaned.data";	
	final static String LP50_NEO4J_GRAPH 			= "neo4j/LP50_2Hops_TagMe/";

   public static void main(String[] args) {

		System.out.println("Exp130723OneGraphOneDocGraphML running with LeePincombe dataset");		

////		Option A) Build graph from input
//		ArrayList<Neo4jRdfGraph> graphs = BuildSeparateDocGraphs.
//					fromDocsTextFile(
//								LP50, graphDirLP, 
//								2, 
//								new DBPediaAllOutgoing(), 
//								null); //don't add weights, will be done below
//		
//		System.out.println("Creating network finished");
		
		
		
//		Option B Load graph from disk
//		ArrayList<Neo4jRdfGraph> graphs = new ArrayList<>();
		for (int i=0; i< 50; i++) {
			Neo4jRdfGraph g = Neo4jRdfGraph.getInstance(LP50_NEO4J_GRAPH + "doc-"+i+".db", false);
			Neo4jGraphUtils.exportGraphML(
						g, 
						g.actualGraphDBDirectory.getAbsolutePath().replace(".db", ".graphml"),
						true,
						true);
			g.shutdown();
		}
		

//		//Compute weights
//		for (Neo4jRdfGraph graph : graphs)  {
//			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterJointIC(), "weight");
//			graph.commit();
////			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterJointIC(), "ICJoint");
////			graph.commit();
////			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterAddedIC(), "ICAdded");
////			graph.commit();
////			Neo4jGraphUtils.updateAllEdgeWeights(graph, new TripleWeighterPMIPlusIC(), "ICPMI");
////			graph.commit();
//		}

//		for (Neo4jRdfGraph graph : graphs) {
//			Neo4jGraphUtils.exportGraphML(
//						graph, 
//						graph.actualGraphDBDirectory.getAbsolutePath().replace(".db", ".graphml"),
//						true,
//						true);
//			graph.shutdown();
//		}

	}
}
