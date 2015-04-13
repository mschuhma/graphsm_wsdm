/*
 * Copyright (c) 2012, Søren Atmakuri Davidsen
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package dk.aaue.sna.ext.graphml;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Reads a GraphML file possibly with attributes on nodes and edges (such as weights).
 * <ol>
 * <li> Use one of the factory methods {@link GraphMLImporter#create(java.io.Reader)},
 * {@link GraphMLImporter#createFromClasspathResource(String)}, or
 * {@link GraphMLImporter#createFromFile(java.io.File)} to create an importer.</li>
 * <li> Use {@link GraphMLImporter#edgeAttributeHandler(AttributeHandler)} and/or
 * {@link GraphMLImporter#nodeAttributeHandler(AttributeHandler)} to specify how to handle
 * attributes.</li>
 * <li> Use the {@link GraphMLImporter#generateGraph(org.jgrapht.Graph, org.jgrapht.VertexFactory, java.util.Map)}
 * as with any GraphGenerator to map the GraphML file onto a Graph object</li>
 * </ol>
 *
 * @author Soren <soren@tanesha.net>
 * @see org.jgrapht.generate.GraphGenerator
 */
public class GraphMLImporter<V, E> implements GraphGenerator<V, E, V> {

    /**
     * Create a GraphMLExtImporter from a File
     *
     * @param file the file
     * @param <V>  Vertex type
     * @param <E>  Edge type
     * @return new importer object
     */
    public static <V, E> GraphMLImporter<V, E> createFromFile(File file) {
        try {
            FileReader fr = new FileReader(file);
            return create(fr);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + e.getMessage(), e);
        }
    }

    /**
     * Create GraphMLExtImporter from a classpath resource
     *
     * @param classpathResource the classpath resource path
     * @param <V>               Vertex type
     * @param <E>               Edge type
     * @return new importer object
     */
    public static <V, E> GraphMLImporter<V, E> createFromClasspathResource(String classpathResource) {
        return create(new InputStreamReader(GraphMLImporter.class.getResourceAsStream(classpathResource)));
    }

    /**
     * Create GraphMLExtImporter from a Reader
     *
     * @param r   The reader
     * @param <V> Vertex type
     * @param <E> Edge type
     * @return new importer object
     */
    public static <V, E> GraphMLImporter<V, E> create(Reader r) {
        try {
            javax.xml.parsers.DocumentBuilderFactory builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new org.xml.sax.InputSource(r));
            Element element = document.getDocumentElement();
            return new GraphMLImporter<V, E>(element);
        }
        catch (Exception e) {
            throw new RuntimeException("Error reading XML: " + e.toString(), e);
        }
    }

    private boolean useNodeIDAsNode = false;
    private Element element;
    private AttributeHandler<E> edgeAttributeHandler = new DummyAttributeHandler<E>();
    private AttributeHandler<V> nodeAttributeHandler = new DummyAttributeHandler<V>();

    private GraphMLImporter(Element element) {
        this.element = element;
    }

    public GraphMLImporter<V, E> useNodeIDAsNode() {
        this.useNodeIDAsNode = true;
        return this;
    }

    public GraphMLImporter<V, E> nodeAttributeHandler(AttributeHandler<V> handler) {
        this.nodeAttributeHandler = handler;
        return this;
    }

    public GraphMLImporter<V, E> edgeAttributeHandler(AttributeHandler<E> handler) {
        this.edgeAttributeHandler = handler;
        return this;
    }

    /**
     * Check if the XML file in question contains any elements which are directed (ie. hinting the user if the generated graph should
     * be directed or undirected).
     *
     * @return True if any directed elements are present in the graphml XML.
     */
    public boolean hasDirected() {

        // check all graph elements for edgedefault property
        NodeList graphElements = element.getElementsByTagName("graph");
        for (int i = 0; i < graphElements.getLength(); i++) {
            Element graph = (Element) graphElements.item(i);
            String edgedefault = graph.getAttribute("edgedefault");
            if (edgedefault != null && "directed".equals(edgedefault.toLowerCase()))
                return true;
        }

        // check all edge elements for directed property
        NodeList edgeElements = element.getElementsByTagName("edge");
        for (int i = 0; i < edgeElements.getLength(); i++) {
            Element edge = (Element) edgeElements.item(i);
            String directed = edge.getAttribute("directed");
            if (directed != null && "true".equals(directed.toLowerCase()))
                return true;
        }

        return false;
    }

    private Map<String, String> readData(Element element) {

        Map<String, String> map = new HashMap<String, String>();

        NodeList data = element.getElementsByTagName("data");
        for (int k = 0; k < data.getLength(); k++) {
            Element key = (Element) data.item(k);
            String keyID = key.getAttribute("key");
            String value = key.getFirstChild().getTextContent().trim();
            map.put(keyID, value);
        }
        return map;
    }

    private Class toGraphMLClass(String value) {
        if ("string".equals(value))
            return String.class;
        else if ("double".equals(value))
            return Double.class;
        else if ("int".equals(value))
            return Integer.class;
        else if ("float".equals(value))
            return Float.class;
        else if ("long".equals(value))
            return Long.class;
        else if ("boolean".equals(value))
            return Boolean.class;
        else
            throw new RuntimeException("Unsupported GraphML attr.type " + value);
    }

    private Map<String, Object[]> readIDMap(Element element) {

        Map<String, Object[]> map = new HashMap<String, Object[]>();

        NodeList data = element.getElementsByTagName("key");
        for (int k = 0; k < data.getLength(); k++) {
            Element key = (Element) data.item(k);
            String id = key.getAttribute("id");
            String name = key.getAttribute("attr.name");
            Class type = toGraphMLClass(key.getAttribute("attr.type"));
            String theFor = key.getAttribute("for");
            map.put(id, new Object[]{name, type, theFor});
        }
        return map;
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        Map<String, Object[]> idMap = readIDMap(element);

        // get graph(s) in file
        NodeList graphElements = element.getElementsByTagName("graph");

        for (int i = 0; i < graphElements.getLength(); i++) {

            NodeList nodeElements = element.getElementsByTagName("node");

            for (int j = 0; j < nodeElements.getLength(); j++) {
                Element node = (Element) nodeElements.item(j);
                String nodeID = node.getAttribute("id");
                Map<String, String> data = readData(node);

                // create and prepare the node (before adding to graph)
                V nodeV = useNodeIDAsNode ? (V) nodeID : vVertexFactory.createVertex();

                nodeAttributeHandler.handle(nodeV, nodeID, buildAttributeGetter("node", idMap, data));

                stringVMap.put(nodeID, nodeV);
                veGraph.addVertex(nodeV);
            }

            NodeList edgeElements = element.getElementsByTagName("edge");

            for (int j = 0; j < edgeElements.getLength(); j++) {
                Element edge = (Element) edgeElements.item(j);

                String source = edge.getAttribute("source");
                String target = edge.getAttribute("target");
                String edgeID = edge.getAttribute("id"); // probably null
                Map<String, String> data = readData(edge);

                V sourceV = stringVMap.get(source);
                V targetV = stringVMap.get(target);

                // create and add the edge
                E newEdge = veGraph.addEdge(sourceV, targetV);

                // handle attributes for the edge
                edgeAttributeHandler.handle(newEdge, edgeID, buildAttributeGetter("edge", idMap, data));
            }
        }
    }

    private static class DummyAttributeHandler<O> implements AttributeHandler<O> {
        @Override
        public void handle(O obj, String id, AttributeGetter getter) {
            // do nothing
        }
    }

    private static <T> T valueOf(Class<T> clazz, String value) {

        if (clazz.isAssignableFrom(String.class))
            return (T) value;

        try {
            Method m = clazz.getMethod("valueOf", new Class[]{String.class});
            return (T) m.invoke(null, value);
        }
        catch (Exception e) {
            throw new RuntimeException("Error getting " + clazz.getName() + ".valueOf(" + value + ") " + e.getMessage(), e);
        }
    }

    private static AttributeGetterImpl buildAttributeGetter(String theFor, Map<String, Object[]> idMap, Map<String, String> values) {
        Map<String, Object> mapped = new HashMap<String, Object>();

        for (Map.Entry<String, Object[]> entry : idMap.entrySet()) {
            Object[] val = entry.getValue();
            if (val[2].equals(theFor) || val[2].equals("both"))
                mapped.put((String) val[0], valueOf((Class) val[1], values.get(entry.getKey())));
        }

        return new AttributeGetterImpl(mapped);
    }

    private static class AttributeGetterImpl implements AttributeGetter {
        private Map<String, Object> values;

        private AttributeGetterImpl(Map<String, Object> values) {
            this.values = values;
        }

        @Override
        public <T> T get(Class<T> clazz, String key) {
            Object value = values.get(key);
            return (T) value;
        }

        @Override
        public <T> boolean has(Class<T> clazz, String key) {
            Object obj = values.get(key);
            return obj != null && obj.getClass().isAssignableFrom(clazz);
        }

        @Override
        public Set<String> keys() {
            return values.keySet();
        }
    }
}
