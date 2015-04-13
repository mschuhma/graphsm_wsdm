package de.uma.dws.graphsm.neo4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.configuration.Configuration;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.impl.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

import de.uma.dws.graphsm.ConfFactory;
import de.uni_mannheim.informatik.dws.dwslib.virtuoso.LodURI;


public class Neo4jRdfGraph extends Neo4jGraph {
	
	final static Logger log = LoggerFactory.getLogger(Neo4jRdfGraph.class);
	private final static Configuration conf = ConfFactory.getConf();

	@SuppressWarnings("unused")
	private static final String SERVER_ROOT_URI = conf.getString("graphdb.rest.api");
    private static final String BASE_GRAPH_PATH = conf.getString("graphdb.testdir");
    
   private static HashMap<File, Neo4jRdfGraph> singeltonInstances = new HashMap<File, Neo4jRdfGraph>();
	private GraphDatabaseService graphDatabaseService = null;
	public File actualGraphDBDirectory = null;
	private  ExecutionEngine cypherEngine = null;
	
	private static LodURI lod = LodURI.getInstance(conf.getString("misc.prefixcc.file"));
    
	@SuppressWarnings("deprecation")
   private Neo4jRdfGraph(String directory, boolean readOnly) {
		super(
				new GraphDatabaseFactory().
				newEmbeddedDatabaseBuilder(directory).
				setConfig(GraphDatabaseSettings.node_keys_indexable, "uri,label,snippetId,queryId,sourceNode" ).
				setConfig(GraphDatabaseSettings.node_auto_indexing, "true" ).
				setConfig(GraphDatabaseSettings.relationship_auto_indexing, "true" ).
				setConfig(GraphDatabaseSettings.relationship_keys_indexable, "snippetId,queryId" ).
				setConfig(GraphDatabaseSettings.read_only, readOnly ? "true" : "false").
				setConfig(GraphDatabaseSettings.use_memory_mapped_buffers, "true").
				setConfig(GraphDatabaseSettings.keep_logical_logs, "false").
//				setConfig(Config.online_backup_enabled, "false").
				setConfig("online_backup_enabled", "false").
				newGraphDatabase());
		this.createKeyIndex("uri", Vertex.class, (Parameter[]) null);
		this.commit();
		graphDatabaseService = super.getRawGraph();
		actualGraphDBDirectory = new File(directory);
		super.getRawGraph();
		log.info("Database set-up with auto-indexing for nodes {} and relations {}", 
				graphDatabaseService.index().nodeIndexNames(),
				graphDatabaseService.index().relationshipIndexNames());
	}
    
    public static Neo4jRdfGraph getInstanceDefaultDatabase(boolean readOnly) {
    	return getInstance(BASE_GRAPH_PATH, readOnly);
    }
    
    public static Neo4jRdfGraph getInstance(String fullDatabasePath, boolean readOnly) {
   	File graphPath = new File(fullDatabasePath);
   	Neo4jRdfGraph singeltonInstance = singeltonInstances.get(graphPath);
    	if (singeltonInstance == null) {
    		singeltonInstance = new Neo4jRdfGraph(fullDatabasePath, readOnly);
    		singeltonInstances.put(graphPath, singeltonInstance);
        	log.info("Neo4j graph loaded from disk: {}", graphPath);
        	return singeltonInstance;
    	} else {
    		return singeltonInstance;
    	}
    }
    
    @Deprecated
    public Neo4jRdfGraph getReadOnlyInstance() {
//    	singeltonInstance.shutdown();
//    	try {Thread.sleep(1000*2);} catch (InterruptedException e) {e.printStackTrace();}
//    	singeltonInstance = null;
//    	return getInstance(actualGraphDBDirectory, true);
   	 return null;
    }
    
    public static boolean hasInstances() {
    	return (singeltonInstances.size() > 0);
    }
    
    public static boolean hasInstance(String fullDatabasePath) {
   	String graphPath = (new File(fullDatabasePath)).getAbsolutePath();
     	return (singeltonInstances.containsKey(graphPath));
     }
    
    public void shutdownAll() {
   	for (Neo4jRdfGraph singeltonInstance : singeltonInstances.values()) {
   		singeltonInstance.commit();
       	log.info("All Neo4j Graph database commited and connection closed: {}", this);
       	singeltonInstance.shutdown();
       	singeltonInstance = null;
   	}
    	singeltonInstances.clear();
    }
    
	public void shutdown() {
		super.commit();
		log.info("Neo4j Graph database commited and connection closed: {}", actualGraphDBDirectory.getAbsolutePath());
		super.shutdown();
		singeltonInstances.remove(actualGraphDBDirectory);
	}
    
    
    public ExecutionResult executeCypher(String query) {
    	if (cypherEngine  == null) {
    		cypherEngine = new ExecutionEngine(graphDatabaseService);
    	}
    	return cypherEngine.execute(query);
    }
    
    public static boolean deleteDatabaseDir(String dirString) {
    	File dir = new File(dirString);
    	if (!dir.isDirectory()) {
    		log.warn("Neo4j database directory deletion failed. {} is not a directory", dir.getAbsolutePath());
    		return false;
    	}
    	if (Neo4jRdfGraph.hasInstance(dirString)) {
    		log.error("Neo4j database directory deletion can not be initatied with existing Neo4jRdfGraph instance.");
    		log.error("Java VM is shutting down.");
    		System.exit(1);
    	}
    	try {
			FileUtils.deleteRecursively(dir);
			Thread.sleep(1*1000);
		} catch (IOException e) {
			log.error("Neo4j database directory deletion failed: {}", e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	log.info("Neo4j database directory deleted: {}", dir);
    	return true;
    }
  
    public GraphDatabaseService getRawGraph() {
    	assert graphDatabaseService != null;
    	return graphDatabaseService;
    }
    
    public Vertex addOrGetVertex(String key, String value) {
    	Iterator<Vertex> iter = this.getVertices(key, value).iterator();
    	while(iter.hasNext()) {
    		assert !iter.hasNext(); //Check for already existing duplicates
    		return iter.next();
    	}
    	Vertex n = this.addVertex(null);
    	n.setProperty(key, value);
    	n.setProperty("label", lod.toPrefixedUri(value));
    	return n;
    }
    
    public Edge addOrGetEdge(Vertex outVertex, Vertex inVertex, String edgeUri) {
    	Edge edge = null;
    	String edgeLabel = lod.toPrefixedUri(edgeUri);
    	int duplErrCheck = 0;
    	
    	for (Edge e : outVertex.getEdges(Direction.OUT, edgeLabel)) {
    		if (e.getVertex(Direction.IN).equals(inVertex)) {
    			duplErrCheck++;
    			edge = e;
    			assert duplErrCheck == 1; //Check for already existing duplicates
    		}
    	}
    	switch (duplErrCheck) {
    		case 0: return this.addEdge(null, outVertex, inVertex, edgeLabel);
    		case 1: return edge;
    		default: assert duplErrCheck <= 1; return null;
    	}
    }
    
    public String printDatabaseVertices() {
    	StringBuffer bf = new StringBuffer();
    	bf.append("Database all vertices:\n");
    	for (Vertex v : this.getVertices()) {
    		bf.append(v);
    		for(String propName : v.getPropertyKeys()) {
    			bf.append("\t" + propName +":" + v.getProperty(propName));
    		}
    		bf.append("\n");
    	}
    	return bf.toString();
    }
    
    public String printDatabaseEdges() {
    	StringBuffer bf = new StringBuffer();
    	bf.append("Database all edges:\n");
    	for (Edge v : this.getEdges()) {
    		bf.append(v);
    		for(String propName : v.getPropertyKeys()) {
    			bf.append("\t" + propName +":" + v.getProperty(propName));
    		}
    		bf.append("\n");
    	}
    	return bf.toString();
    }
    
    public String printDatabase() {
    	return "Database content:\n" + this.printDatabaseVertices() + this.printDatabaseEdges();
    }
    
    
    @SuppressWarnings("unused")
	public static void main( String[] args) {
    	
    	Neo4jRdfGraph.deleteDatabaseDir(conf.getString("graphdb.testdir")+".test");
    	
    	Neo4jRdfGraph graph = Neo4jRdfGraph.getInstance(conf.getString("graphdb.testdir"+".test"), false);
    	
    	System.out.println(graph.printDatabase());
    	
    	Vertex a = graph.addOrGetVertex("uri", "http://dbpedia.org/resource/Michael");
    	Vertex b = graph.addOrGetVertex("uri", "http://dbpedia.org/resource/Daniel");
    	a.setProperty("someOtherRandomPropery","the uri has to be the unique identifier");
    	b.setProperty("anotherCommentProp","as neo4j reuses the id property internally");
    	Edge e = graph.addEdge(null, a, b, "knowsEdge");
    	e.setProperty("someAttributeForTheEdge", 0.8);
    	e.setProperty("comment", "property uniquenes is not enforced for edges, currently");
    	graph.commit();
    	System.out.println(graph.printDatabase());
    	
    	//Check if unique constrains for nodes is enforced (based on uri)
    	Vertex c = graph.addOrGetVertex("uri", "http://daniel");
    	Vertex d = graph.addOrGetVertex("uri", "http://michael");
    	Vertex q =graph.addOrGetVertex("uri", "http://cilli");
    	//Check if unique constrains for vertices/edges is enforced (based on lable)
    	Edge f = graph.addOrGetEdge(c, d, "knowsEdge");
    	Edge g = graph.addOrGetEdge(c, d, "knowsEdge1");
    	Edge h = graph.addOrGetEdge(c, d, "knowsEdge");
    	graph.commit();
    	System.out.println(graph.printDatabase());
    	
    	//Test property utils
    	Neo4jGraphUtils.addOrUpdateHashSetProperty(c, "testArray", 2);
    	graph.commit();
    	System.out.println(graph.printDatabase());
    	
    	Neo4jGraphUtils.addOrUpdateHashSetProperty(c, "testArray", 212);
    	Neo4jGraphUtils.addOrUpdateHashSetProperty(c, "testArray", 2);
    	Neo4jGraphUtils.addOrUpdateHashSetProperty(c, "testArray", 2);
    	Neo4jGraphUtils.addOrUpdateHashSetProperty(c, "testArray", 3);
    	graph.commit();
    	System.out.println(graph.printDatabase());
    	
    	graph.shutdown();
    }
   
}