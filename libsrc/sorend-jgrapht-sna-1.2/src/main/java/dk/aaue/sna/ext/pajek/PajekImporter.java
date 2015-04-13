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
package dk.aaue.sna.ext.pajek;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.GraphGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;

/**
 * Importer for Pajek files. Supports files with multiple networks and multiple sets of arcs. Each arc-set is
 * defined as a separate network which can be "generated".
 *
 * @param <V> node type
 * @param <E> edge type
 */
public class PajekImporter<V, E> implements GraphGenerator<V, E, V> {

    public static final String DEFAULT_NETWORK = "_default";

    /**
     * Creates an importer from a Pajek "Reader" source.
     * @param reader the pajek source
     * @param <V> node type
     * @param <E> edge type
     * @return importer
     * @throws IOException
     */
    public static <V, E> PajekImporter<V, E> createImporter(Reader reader) throws IOException {

        BufferedReader br = new BufferedReader(reader);

        LinkedList<PajekGraphModel> networks = new LinkedList<PajekGraphModel>();
        PajekGraphModel current = null;
        boolean readVertices = false;
        boolean readArcs = false;

        while (true) {
            String line = br.readLine();

            if (line == null)
                break;

            String[] args = line.trim().split("\\s+");

            if (args[0].startsWith("*")) // reset when we get "*"
                readArcs = readVertices = false;

            if (args[0].equals("*Network")) {
                PajekGraphModel newNetwork = new PajekGraphModel();
                newNetwork.name = args.length > 1 ? joinSlice(args, " ", 1) : DEFAULT_NETWORK;
                current = newNetwork;
                readArcs = readVertices = false;
            }
            else if (args[0].equals("*Vertices")) {
                readArcs = false;
                readVertices = true;
            }
            else if (args[0].equals("*Arcs")) {
                PajekGraphModel model = new PajekGraphModel();
                model.vertices.putAll(current.vertices);
                model.name = current.name;
                model.version = args.length > 1 ? joinSlice(args, " ", 1).replaceAll("\"", "") : DEFAULT_NETWORK;
                networks.add(model);
                current = model;
                readArcs = true;
                readVertices = false;
            }
            else if (args.length > 1 && readVertices) {
                int id = Integer.parseInt(args[0]);
                String name = joinSlice(args, " ", 1).replaceAll("\"", "");
                current.vertices.put(id, name);
            }
            else if (args.length > 1 && readArcs) {
                PajekArc arc = new PajekArc();
                arc.source = Integer.parseInt(args[0]);
                arc.target = Integer.parseInt(args[1]);
                arc.weight = args.length > 2 ? Double.parseDouble(args[2]) : 1.0;
                current.arcs.add(arc);
            }

        }

        return new PajekImporter<V, E>(networks);
    }

    // currently selected network
    private PajekGraphModel selected;

    private Map<String, PajekGraphModel> models = new LinkedHashMap<String, PajekGraphModel>();

    private PajekImporter(List<PajekGraphModel> models) {
        selected = models.get(0);
        for (PajekGraphModel model : models)
            this.models.put(model.name + "->" + model.version, model);
    }

    public PajekImporter<V, E> selectNetwork(String network) {
        this.selected = models.get(network);
        return this;
    }

    public Set<String> getNetworks() {
        return models.keySet();
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        Map<Integer, V> intToNodeMap = new HashMap<Integer, V>();

        for (Integer nodeID : selected.vertices.keySet()) {
            V node = vVertexFactory.createVertex();
            if (!veGraph.containsVertex(node))
                veGraph.addVertex(node);
            intToNodeMap.put(nodeID, node);
        }

        for (PajekArc arc : selected.arcs) {
            V source = intToNodeMap.get(arc.source);
            V target = intToNodeMap.get(arc.target);
            E edge = null;
            if (!veGraph.containsEdge(source, target))
                edge = veGraph.addEdge(source, target);
            else
                edge = veGraph.getEdge(source, target);

            if (veGraph instanceof WeightedGraph)
                ((WeightedGraph) veGraph).setEdgeWeight(edge, arc.weight);
        }
    }

    private static class PajekGraphModel {
        String name;
        String version;
        Map<Integer, String> vertices = new HashMap<Integer, String>();
        List<PajekArc> arcs = new ArrayList<PajekArc>();
    }

    private static class PajekArc {
        int source;
        int target;
        double weight;
    }

    private static String joinSlice(String[] s, String d, int from) {
        StringBuilder b = new StringBuilder();
        for (int i = from; i < s.length; i++) {
            if (i > from)
                b.append(d);
            b.append(s[i]);
        }
        return b.toString();
    }
}