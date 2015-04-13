package de.uma.dws.graphsm.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.JGraphTNode;
import de.uma.dws.graphsm.datamodel.Tuple;
import de.uma.dws.graphsm.jgrapht.JGraphTWeightedRdf;
import de.uma.dws.graphsm.neo4j.DBPediaDocCollectionGraphBuilder;
import de.uma.dws.graphsm.neo4j.Neo4jGraphUtils;
import de.uma.dws.graphsm.neo4j.Neo4jRdfGraph;
import de.uma.dws.graphsm.tools.FileSystem;
import de.uma.dws.graphsm.tripleweighter.TripleWeighter;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterAddedIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterEqualWeights;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterJointIC;
import de.uma.dws.graphsm.tripleweighter.TripleWeighterPMIPlusIC;
import de.uma.dws.graphsm.webservice.Annotator;
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;
import de.uma.dws.graphsm.webservice.DBPediaSpotlight;
import de.uma.dws.graphsm.webservice.TagMe;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;

public class Exp130730ArnabSubjObjCandidateRanking {

	public final static Logger	       log	  = LoggerFactory.getLogger(Exp130730ArnabSubjObjCandidateRanking.class);
	
	public final static Configuration	conf	= ConfFactory.getConf();
	public final static LodURI lod = LodURI.getInstance(conf.getString("misc.prefixcc.file"));
	
	public final static Integer MAX_PATH_LENGTH 	= Integer.MAX_VALUE;
	public final static Double  MAX_PATH_COST = Double.MAX_VALUE;
	
	public final static Annotator annotator = new TagMe();
//	public final static TripleWeighter tripleWeighter = new TripleWeighterJointIC(true);

	public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {
	   
		ArrayList<String> nellPredicates = new ArrayList<String>();
		
//		nellPredicates.add("actorstarredinmovie");
//		nellPredicates.add("agentcollaborateswithagent");
//		nellPredicates.add("animalistypeofanimal");
//		nellPredicates.add("athleteledsportsteam");
//		nellPredicates.add("bankbankincountry");
//		nellPredicates.add("citylocatedinstate");
//		nellPredicates.add("bookwriter");
//		nellPredicates.add("companyasloknownas");
//		nellPredicates.add("personleadsorganization");
//		nellPredicates.add("teamplaysagainstteam");
//		nellPredicates.add("weaponmadeincountry");
//		nellPredicates.add("lakeinstate");
		
		nellPredicates.add("teamplaysagainstteam-nonumbers");
		
		ArrayList<TripleWeighter> tripleWeighters = new ArrayList<TripleWeighter>();
		
//		tripleWeighters.add(new TripleWeighterJointIC(true));
//		tripleWeighters.add(new TripleWeighterAddedIC(true));
		tripleWeighters.add(new TripleWeighterEqualWeights(true));
		
		
		for (String s : nellPredicates) {

			for (TripleWeighter tWeighter : tripleWeighters) {

				String nellPredicate = s;

				String input = "src/main/resources/dataset/Arnab/top5-candidates/top5_" + nellPredicate + ".tsv";
				String graphDir = "neo4j/arnab/graphs-wsdm/" + nellPredicate + "/";

				String outputDir = "output/arnab/wsdm/";

				try {
//					runMainExp(input, graphDir, outputDir, nellPredicate, tWeighter);
					createTop1File(nellPredicate);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	//		checkNodes();
		
//		try {
//	      createTop1File();
//      }
//      catch (IOException e) {
//	      e.printStackTrace();
//      }
   
	}
	
	public static void createTop1File(String nellPredicate) throws IOException {
		
		final String input  = "src/main/resources/dataset/Arnab/top5-candidates/top5_"+nellPredicate+".tsv";
		final String output 	= "src/main/resources/dataset/Arnab/baseline/baseline_"+nellPredicate+".tsv";
		
		ArrayList<ArrayList<String>> inputDoc = MyFileReader.readXSVFile(new File(input), "\t", false);
		
		FileWriter out = new FileWriter(output);
		
		String cSub = "";
		
		for (ArrayList<String> l : inputDoc) {
			
			String sub = l.get(0);
			
			if (cSub.equals(sub)) {
				cSub = sub;
				continue;
			}
			cSub = sub;
			
			out.write(l.get(0) + "\t" + l.get(1) + "\t" + l.get(2) + "\t" + l.get(3) + "\t" + l.get(4) + "\t" + 1d + "\n");

		}
		
		out.close();
	   
   }

	public static void runMainExp(
				String input, String graphDir, String outputDir, String outputName, TripleWeighter tripleWeighter) 
							throws IOException {

		System.out.println("Creating neo4j graphs for Arnab DBpedia subject-object candidates ranking");
		
//		final String outputBestMatch  = outputDir + "arnab-" + 
//							annotator.toString() + "-" + outputName + "-" + tripleWeighter.toString() 
//							+ "-best-match.tsv";
		final String outputAllMatches = outputDir + "arnab-"  +
							annotator.toString() + "-" + outputName + "-" + tripleWeighter.toString()
							+ "-all-match.tsv";

		ArrayList<ArrayList<String>> inputDoc = MyFileReader.readXSVFile(new File(input), "\t", false);

		HashSet<String> subNodes = new HashSet<String>();
		HashSet<String> objNodes = new HashSet<String>();
		
		String cSubGS  = inputDoc.get(0).get(0).trim();
		String cPredGS = inputDoc.get(0).get(1).trim();
		String cObjGS  = inputDoc.get(0).get(2).trim();
			
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir + cSubGS + "-" + cObjGS + ".db/", false);
		
//		Writer outBest = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputBestMatch),  "UTF-8"));
		Writer outAll =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputAllMatches), "UTF-8"));
		
		DBPediaDocCollectionGraphBuilder graphBuilder = 
					new DBPediaDocCollectionGraphBuilder(graph, null, annotator);
		
		for (int lcnt = 0; lcnt < inputDoc.size(); lcnt++) {
			
//			System.out.println(inputDoc.get(lcnt).get(0) + "-" + inputDoc.get(lcnt).get(2));
			
			ArrayList<String> l = inputDoc.get(lcnt);
			
			String subGS  = l.get(0).trim();
			String predGS = l.get(1).trim();
			String objGS  = l.get(2).trim();
			
			String sub = l.get(3).trim();
			String obj = l.get(4).trim();
			
			if (sub.contains("DUMMY-NO-VALUE") || obj.contains("DUMMY-NO-VALUE")) {
				
				outAll.write((
							cSubGS + "\t" +
							cPredGS + "\t" +
							cObjGS + "\t" +
							sub.replace("dbpedia:", "").replace("http://dbpedia.org/resource/", "") + "\t" +
							obj.replace("dbpedia:", "").replace("http://dbpedia.org/resource/", "") + "\t" + 
							1d + "\n"));
				
				cSubGS = subGS;
				cPredGS = predGS;
				cObjGS = objGS;

				subNodes.clear();
				objNodes.clear();

				continue;
			}
			

			// Get all candidates for one nell pair
			if (subGS.equals(cSubGS) && objGS.equals(cObjGS)) {
				
				sub = "http://dbpedia.org/resource/" + l.get(3).trim();
				obj = "http://dbpedia.org/resource/" + l.get(4).trim();		

				subNodes.add(sub);
				objNodes.add(obj);
				
				if (lcnt < inputDoc.size() - 1)
					continue;
				else
					lcnt++;
			}

			// Build network for dbpedia candidates
			graphBuilder.addExternalSourceNodes(new ArrayList<String>(subNodes));
			graphBuilder.addExternalSourceNodes(new ArrayList<String>(objNodes));

			graphBuilder.addExpandedNetwork(new DBPediaAllOutgoing(), 2);

			Neo4jGraphUtils.updateAllEdgeWeights(graph, tripleWeighter, "cost");

			JGraphTWeightedRdf jgraph = new JGraphTWeightedRdf(graph.actualGraphDBDirectory.getName());

			try {
				jgraph.addGraph(graph);
				
				File delGraph = graph.actualGraphDBDirectory;
				graph.shutdown();
				FileSystem.deleteFileOrDirectory(delGraph);
				
				jgraph.removeDeadEndNodes();

				// Compute candidates
				Double min = Double.MAX_VALUE;

				String bestSub = "";
				String bestObj = "";
				
//				System.out.println(jgraph);
				
				for (String subjN : subNodes) {
					
					subjN = lod.toPrefixedUri(subjN);
					
					for (String objN : objNodes) {

						objN = lod.toPrefixedUri(objN);
						
						Double cost = jgraph.dijkstra(
									new JGraphTNode(subjN, true), 
									new JGraphTNode(objN, true),
						         MAX_PATH_LENGTH, MAX_PATH_COST);
					

						if (cost == null || cost.equals(0d)) {
							cost = Double.MAX_VALUE;
						}
							
						outAll.write((
									cSubGS + "\t" +
									cPredGS + "\t" +
									cObjGS + "\t" +
									subjN.replace("dbpedia:", "").replace("http://dbpedia.org/resource/", "") + "\t" +
									objN.replace("dbpedia:", "").replace("http://dbpedia.org/resource/", "") + "\t" + 
									cost + "\n"));

						if (cost < min) {
							min = cost;
							bestSub = subjN;
							bestObj = objN;
						}
					}
				}

//				Tuple<String, String> t1 = new Tuple<String, String>(
//							bestSub.replace("dbpedia:", "DBP#resource/"),
//				         "NELL#Instance/" + cSubGS);
//
//				Tuple<String, String> t2 = new Tuple<String, String>(
//							bestObj.replace("dbpedia:", "DBP#resource/"),
//				         "NELL#Instance/" + cObjGS);

//				outBest.write("sameAs(\"" + t1.k + "\", \"" + t1.v + "\")\n");
//				outBest.write("sameAs(\"" + t2.k + "\", \"" + t2.v + "\")\n");
//				
//				outBest.flush();
				outAll.flush();
				
			}
			catch (IOException | RuntimeException e) {
				e.printStackTrace();
			}

			finally {

				// Prepare settings for next nell pair, i.e. the next graph
				cSubGS = subGS;
				cPredGS = predGS;
				cObjGS = objGS;

				subNodes.clear();
				objNodes.clear();

				graph = Neo4jRdfGraph.getInstance(graphDir + cSubGS + "-" + cObjGS + ".db/", false);
				graphBuilder = new DBPediaDocCollectionGraphBuilder(graph, null, annotator);
			
			}
			
			lcnt--; //process line again
		}
		
		try {
//			outBest.close();
			outAll.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
//		log.info("Sub Obj candidates scoring written to output file {}", outputBestMatch);
		log.info("Run finished.");
	}
	
	
	public static void checkNodes() {
		
		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance("neo4j/arnab/graphs/ben_stillermeet_the_parents.db", false);
		
		JGraphTNode node1 = new JGraphTNode("dbpedia:Meet_the_Parents", true);
		JGraphTNode node2 = new JGraphTNode("dbpedia:Ben_Stiller", true);
		
		for (Vertex v : graph.getVertices()) {
			System.out.println(v.getProperty("uri") + "\t" + v.getProperty("sourceNode"));
			for (Edge e : v.getEdges(Direction.BOTH))
				System.out.println(e.getLabel());
		}
		
		JGraphTWeightedRdf jgraph = new JGraphTWeightedRdf(graph.actualGraphDBDirectory.getName());
		jgraph.addGraph(graph);
		
		System.out.println(jgraph);
		
		Double cost = null;
      try {
	      cost = jgraph.dijkstra(node1, node2, MAX_PATH_LENGTH, MAX_PATH_COST);
      }
      catch (IOException e) {
	      e.printStackTrace();
      }
		
		System.out.println(cost);
		
		
	}
}
