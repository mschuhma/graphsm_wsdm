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

package dk.aaue.sna.ext.gml;

import org.apache.commons.io.IOUtils;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Import a graph from GML layout[1].
 *
 * The implementation is very simple, it supports only node.id and edge.source/edge.target, no labels/weights for now.
 *
 * [1] See, http://www.fim.uni-passau.de/en/fim/faculty/chairs/theoretische-informatik/projects.html
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class GmlImporter<V, E> implements GraphGenerator<V, E, V> {

    public static GmlImporter from(String classpathResource) throws IOException {
        //return from(Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathResource));
        return from(GmlImporter.class.getResourceAsStream(classpathResource));
    }

    public static GmlImporter from(InputStream in) throws IOException {
        return from(new InputStreamReader(in));
    }

    public static GmlImporter from(Reader r) throws IOException {
        return new GmlImporter(IOUtils.toString(r));
    }

    private String text;

    public GmlImporter(String text) {
        this.text = text;
    }

    protected static String parseNodeID(String node) {
        for (String line : node.trim().split("\r?\n")) {
            String[] keyVal = line.trim().split("\\s+");
            if (keyVal.length != 2)
                continue;
            if (keyVal[0].equals("id"))
                return keyVal[1];
        }
        return null;
    }

    protected static String[] parseEdge(String val) {
        String src = null, dst = null;
        for (String line : val.trim().split("\r?\n")) {
            String[] keyVal = line.trim().split("\\s+");
            if (keyVal.length != 2)
                continue;
            if (keyVal[0].equals("source"))
                src = keyVal[1];
            else if (keyVal[0].equals("target"))
                dst = keyVal[1];
        }
        if (src != null && dst != null)
            return new String[]{ src, dst };
        else
            return null;
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringTMap) {

        //
        String graph = "";
        for (String line : text.split("\r?\n")) {
            if (line.startsWith("Creator"))
                continue;
            graph += line + "\n";
        }

        graph = graph.trim();
        if (!graph.startsWith("graph"))
            return;

        graph = graph.substring(5).trim();
        if (!graph.startsWith("[") && graph.endsWith("]"))
            return;

        graph = graph.substring(1, graph.length() - 1);
        // System.out.println("graph = " + graph);

        graph = graph.trim();

        while (graph.length() > 0) {

            if (graph.startsWith("node")) {
                int nodeStart = graph.indexOf('[');
                int nodeEnd = graph.indexOf(']');
                String nodeID = parseNodeID(graph.substring(nodeStart + 1, nodeEnd));
                V newNode = vVertexFactory.createVertex();
                stringTMap.put(nodeID,  newNode);
                veGraph.addVertex(newNode);
                graph = graph.substring(nodeEnd + 1).trim();
            }
            else if (graph.startsWith("edge")) {
                int edgeStart = graph.indexOf('[');
                int edgeEnd = graph.indexOf(']');
                String[] srcDst = parseEdge(graph.substring(edgeStart + 1, edgeEnd));
                V source = stringTMap.get(srcDst[0]);
                V target = stringTMap.get(srcDst[1]);
                veGraph.addEdge(source, target);
                graph = graph.substring(edgeEnd + 1).trim();
            }
            else {
                throw new RuntimeException("Unexpected token reading graph, at: " + graph);
            }
        }

    }
}
