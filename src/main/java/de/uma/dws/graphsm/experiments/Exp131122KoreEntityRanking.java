package de.uma.dws.graphsm.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.util.URIref;

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

public class Exp131122KoreEntityRanking {

	public final static Logger log = 
				LoggerFactory.getLogger(Exp131122KoreEntityRanking.class);
	public final static Configuration	 conf = 
				ConfFactory.getConf();
	public final static LodURI lodUtils	 = 
				LodURI.getInstance();

	public final static Integer	 MAX_PATH_LENGTH = Integer.MAX_VALUE;
	public final static Double	 MAX_PATH_COST	  = Double.MAX_VALUE;
	
	//Weighting and graph expension settion
	public final static DBPediaEdgeSelector  edgeExensionSelector = new DBPediaAllOutgoing();
	public static TripleWeighter 		        tripleWeighter = new TripleWeighterAddedIC(true);

	public static String	 graphDirBase	  = "neo4j/kore/";
	
	public static final WikiRedir redirectResolver = WikiRedirMySql.getInstance();


	public static void main(String[] args) throws IOException {
		
//		conf.setProperty("dbpedia.sparql.url", "http://dbpedia.org/sparql");
//		conf.setProperty("dbpedia.sparql.url", "http://wifo5-32.informatik.uni-mannheim.de:8890/sparql");

		System.out.println("Starting Exp131122KoreEntityRanking...");
		
		String inputFile = "/dataset/AidaKore/kore-rankedrelatedentities-gs.tsv";
	
		tripleWeighter = new TripleWeighterEqualWeights(true);
		ArrayList<double[]> unweighted = runExperiment(inputFile);
		
		tripleWeighter = new TripleWeighterJointIC(true);
		ArrayList<double[]> jointIC = runExperiment(inputFile);
		
		tripleWeighter = new TripleWeighterAddedIC(true);
		ArrayList<double[]> addedIC = runExperiment(inputFile);
		
		tripleWeighter = new TripleWeighterPMIPlusIC(true);
		ArrayList<double[]> PMIPlusIC = runExperiment(inputFile);
		
		int i = 0;
		
		for (i = 0; i < unweighted.size(); i++) {
			System.out.println(
						"Paired t-test \t jointIC \t unweighted\t" +
						TestUtils.pairedTTest(unweighted.get(i), jointIC.get(i)));
		}
		
		for (i = 0; i < unweighted.size(); i++) {
			System.out.println(
						"Paired t-test \t addedIC \t unweighted\t" +
						TestUtils.pairedTTest(unweighted.get(i), addedIC.get(i)));
		}
		
		for (i = 0; i < unweighted.size(); i++) {
			System.out.println(
						"Paired t-test \t PMIPlusIC \t unweighted\t" +
						TestUtils.pairedTTest(unweighted.get(i), PMIPlusIC.get(i)));
		}
		
		System.out.println("... done.");
		
	}
	
	public static ArrayList<double[]> runExperiment(String inputFile) throws IOException {

		InputStream in =
		         Exp131122KoreEntityRanking.class
		                  .getResourceAsStream(inputFile);
		
		String outputFile =
					"output/kore/ranking_" + tripleWeighter.toString() + ".tsv";
		
		System.out.println("Starting experiment. Writing output to " + outputFile);
		
		Writer output = new BufferedWriter(
					new OutputStreamWriter(
								new FileOutputStream(
											outputFile), "UTF-8"));

		ArrayList<ArrayList<String>> input =
		         MyFileReader.readXSVFile(in, "\t", false);
		
		ArrayList<double []> result = new ArrayList<>();
		
		Double avgCorr = 0d;

		for (ArrayList<String> problem : input) {
			
			//Compute similiarity with graph method
			double[] scores = ArrayUtils.toPrimitive(
						computeSimilarity(
									problem, output));
			
			//Evaluate results against goldstandard
			double[] gsRanking = new double[scores.length];
			for (int i = 0; i < gsRanking.length; i++)
				gsRanking[i] = -i;
			
			log.debug(ArrayUtils.toString(scores));
			log.debug(ArrayUtils.toString(gsRanking));
			
			SpearmansCorrelation rankCorr = new SpearmansCorrelation();
			
			Double corr = rankCorr.correlation(gsRanking, scores);
			result.add(scores);
			avgCorr += corr;
			
//			RealMatrix pVals = rankCorr.getRankCorrelation().getCorrelationPValues();
//			System.out.println(pVals.getData());
			
			System.out.println(
						"SpearmansCorrelation\t" + problem.get(0) + "\t" + corr);
		}
		
		output.close();
		
		System.out.print("Avg results all problems\t" + tripleWeighter.toString());
		System.out.println("Spearman Correlation\t"     + avgCorr/input.size());
		
		return result;

	}
	
	public static Double[] computeSimilarity(ArrayList<String> prob, Writer out) throws IOException {

		ArrayList<String> inst = new ArrayList<>();
		
		//Input Preprocessing
		for (String s : prob) {
			
			String cStr;
			
			cStr = s.trim().replace(" ", "_");
			cStr = redirectResolver.get(cStr);
			cStr = URIref.encode(cStr);
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
				
            out.write(URIref.decode(n1) + "\t" + URIref.decode(n2) + "\t" + cost + "\n");
         }
         catch (IllegalArgumentException e) {
         	out.write(URIref.decode(n1) + "\t" + URIref.decode(n2) + "\t" + cost + "\n");
         	log.warn(e.getMessage());
         }
		}
		
		out.flush();
		return scores;

	}

}
