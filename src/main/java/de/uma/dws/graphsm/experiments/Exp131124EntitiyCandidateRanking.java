package de.uma.dws.graphsm.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import javax.management.RuntimeErrorException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uma.dws.eleval.wikipedia.WikiRedir;
import de.uma.dws.eleval.wikipedia.WikiRedirMySql;
import de.uma.dws.graphsm.ConfFactory;
import de.uma.dws.graphsm.datamodel.JGraphTNode;
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
import de.uma.dws.graphsm.webservice.DBPediaAllOutgoing;
import de.uma.dws.graphsm.webservice.DBPediaEdgeSelector;
import de.uni_mannheim.informatik.dws.dwslib.MyFileReader;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;

public class Exp131124EntitiyCandidateRanking {

	public final static Logger log = 
				LoggerFactory.getLogger(Exp131124EntitiyCandidateRanking.class);
	public final static Configuration	 conf = 
				ConfFactory.getConf();
	public final static LodURI lodUtils	 = 
				LodURI.getInstance();

	public final static Integer	 MAX_PATH_LENGTH = Integer.MAX_VALUE;
	public final static Double	 MAX_PATH_COST	  = Double.MAX_VALUE;
	
	//Weighting and graph expension settion
	public final static DBPediaEdgeSelector  edgeExensionSelector = new DBPediaAllOutgoing();
	public static TripleWeighter 		        tripleWeighter = new TripleWeighterAddedIC(true);

	public static String	 graphDirBase	  = "neo4j/EntityLinking";
	
	public static final WikiRedir redirectResolver = WikiRedirMySql.getInstance();


	public static void main(String[] args) throws IOException {

		System.out.println("Starting Exp131124EntitiyCandidateRanking...");
		
		String input = "/dataset/EntityLinking/kore-50docs-el.tsv";
	
		tripleWeighter = new TripleWeighterEqualWeights(true);
		
		ArrayList<double[]> unweighted = runExperiment(input);
		

	}
	
	public static ArrayList<double[]> runExperiment(String inputFile) throws IOException {

		InputStream in =
		         Exp131124EntitiyCandidateRanking.class
		                  .getResourceAsStream(inputFile);
		
		File testFile = new File(inputFile);
		
		System.out.println(testFile.canRead());
		
		System.out.println(inputFile);
		in.available();
		
		String outputFile =
					"output/EntityLinking/ranking_" + tripleWeighter.toString() + ".tsv";
		
		Writer output = new BufferedWriter(
					new OutputStreamWriter(
								new FileOutputStream(
											outputFile), "UTF-8"));

		ArrayList<ArrayList<String>> input =
		         MyFileReader.readXSVFile(in, "\t", false);
		
		//Separate doc information
		ArrayList<Doc> docs = new ArrayList<Doc>();
		
		docs.add(new Doc (input.get(0).get(0)));
		
		for (ArrayList<String> line : input) {
			
			Doc cDoc = docs.get(docs.size() - 1);
			
			String cName = line.get(0);
			
			if (!cName.equals(cDoc.getName())) {
				cDoc = new Doc (cName);
				docs.add(cDoc);
			}
			
			cDoc.surface.add(line.get(1));
			cDoc.gsUri.add(line.get(2));
			
		}
		
		System.out.println(docs);
		
//		
//		
//		ArrayList<double []> result = new ArrayList<>();
//		
//		Double avgCorrelation = 0d;
//
//		for (ArrayList<String> line : input) {
//			
//			String cDoc = line.get(0);
			
			
			
			//Compute similiarity with graph method
//			double[] scores = 
//						ArrayUtils.toPrimitive(
//									computeSimilarity(doc, output));
			
			//Evaluate results against goldstandard
//			double[] gsRanking = new double[scores.length];
//			for (int i = 0; i < gsRanking.length; i++)
//				gsRanking[i] = -i;
//			
//			log.debug(ArrayUtils.toString(scores));
//			log.debug(ArrayUtils.toString(gsRanking));
//			index
//			SpearmansCorrelation sCorr = new SpearmansCorrelation();
//			Double corr = sCorr.correlation(gsRanking, scores);
//			
//			result.add(scores);
//			
//			avgCorrelation += corr;
//			
//			System.out.println(
//						"SpearmansCorrelation\t" + doc.get(0) + "\t" + corr);
//
//		}
//		
		output.close();
//		
//		System.out.println("Average corr for all " + input.size() +
//					         " ranking tasks " + avgCorrelation/input.size() + 
//					         " with\t" + tripleWeighter.toString());
//		
//		return result;
		return null;

	}
	
	public static Double[] computeSimilarity(ArrayList<String> prob, Writer out) throws IOException {

		ArrayList<String> inst = new ArrayList<>();
		
		//Input Preprocessing
		for (String s : prob) {
			
			String cStr;
			
			cStr = s.trim().replace(" ", "_");
			cStr = redirectResolver.get(cStr);
			cStr = "http://dbpedia.org/resource/" + cStr;
			
			inst.add(cStr);
		}
		
		//Prepare results array for later eval		
		Double[] scores = new Double[prob.size() - 1];

		//Build neo4 graph
		String graphDir = graphDirBase + prob.get(0) + ".db/";

		Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(graphDir, false);
		
		DBPediaDocCollectionGraphBuilder graphBuilder =
		         new DBPediaDocCollectionGraphBuilder(
		         			graph, null, null);
		
		graphBuilder.addExternalSourceNodes(inst);
		
		graphBuilder.addExpandedNetwork(edgeExensionSelector , 2);

		Neo4jGraphUtils.updateAllEdgeWeights(graph, tripleWeighter, "cost");
		
		//Build jgrapht graph for computation
		JGraphTWeightedRdf jgraph =
		         new JGraphTWeightedRdf(graph.actualGraphDBDirectory.getName());
		
		jgraph.addGraph(graph);

		File delGraph = graph.actualGraphDBDirectory;
		graph.shutdown();
		FileSystem.deleteFileOrDirectory(delGraph);

		jgraph.removeDeadEndNodes();
		
		String n1 = lodUtils.toPrefixedUri(inst.remove(0));
		
		for (int j = 0; j < inst.size(); j++) {
			
			String str = inst.get(j);
			String n2 = lodUtils.toPrefixedUri(str);
			
			Double cost = -999999999d;
			
			try {
				cost = jgraph.dijkstra(
							new JGraphTNode(n1, true),
							new JGraphTNode(n2, true),
							MAX_PATH_LENGTH,
							MAX_PATH_COST);
            
				if (cost == null)
					scores[j] = -999999999d;
				else
					scores[j] = -cost;
				
            out.write(n1 + "\t" + n2 + "\t" + cost + "\n");
         }
         catch (IllegalArgumentException e) {
         	out.write(n1 + "\t" + n2 + "\t" + cost + "\n");
         	log.warn(e.getMessage());
         }
		}
		
		out.flush();
		return scores;

	}
	
	private static class Doc {
		
		String name;
		ArrayList<String> surface;
		ArrayList<String> gsUri;
		
		public Doc(String name) {
			this.name = name;
			this.surface = new ArrayList<String>();
			this.gsUri = new ArrayList<String>();
      }
		
		public String getName() {
			return this.name;
		}
		
		public String getGS(int i) {
			if(this.surface.size() != this.gsUri.size())
				throw new RuntimeException("Number of SurfaceForms does not equal number of Goldstandard URIs");
			return this.gsUri.get(i);
		}
		
		public String getSF(int i) {
			if(this.surface.size() != this.gsUri.size())
				throw new RuntimeException("Number of SurfaceForms does not equal number of Goldstandard URIs");
			return this.surface.get(i);
		}
		
		@Override
		public String toString() {
			if(this.surface.size() != this.gsUri.size())
				throw new RuntimeException("Number of SurfaceForms does not equal number of Goldstandard URIs");
		   return "\n" + name +"\n"+ surface.toString() +"\n" + gsUri.toString();
		}
		
		
	}

}
