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

package dk.aaue.sna.alg.hierarchy;

import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.util.StringContinousFactory;
import org.apache.commons.io.IOUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.Subgraph;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import static dk.aaue.sna.alg.JGraphTTests.builder;
import static dk.aaue.sna.alg.JGraphTTests.emptyDirectedWeighted;
import static dk.aaue.sna.alg.JGraphTTests.emptyWeighted;
import static junit.framework.Assert.assertEquals;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class AgonyMinimizingHierarchyTest {

    AgonyMinimizingHierarchy<String, DefaultWeightedEdge> impl;
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> res;

    // perfect hierarchy graphs
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_1_a;
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_1_b;
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_1_c;

    // some hierarchy graphs
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_2_a;
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_2_b;

    // no hierarchy graphs
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_3_a;
    DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> figure_3_b;

    @Before
    public void makeExamples() {
        figure_1_a = builder(emptyDirectedWeighted())
                .addVertices("0", "1", "2", "3", "4")
                .addEdges("0", "1", "1", "2", "2", "3", "3", "4")
                .graph(DefaultDirectedWeightedGraph.class);

        figure_1_b = builder(emptyDirectedWeighted())
                .addVertices("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l")
                // left side
                .addEdges("a", "b", "c", "b", "b", "d", "e", "d", "d", "f")
                // center
                .addEdges("g", "f")
                // right side
                .addEdges("h", "i", "i", "j", "k", "j", "l", "j", "j", "f")
                .graph(DefaultDirectedWeightedGraph.class);

        figure_1_c = builder(emptyDirectedWeighted())
                .addVertices("0_0", "0_1", "1_0", "1_1", "1_2", "2_0", "2_1", "3_0")
                // 0_0
                .addEdges("0_0", "1_0", "0_0", "3_0", "0_0", "1_1")
                // 0_1
                .addEdges("0_1", "1_1", "0_1", "1_2")
                // 1_0
                .addEdges("1_0", "2_0")
                // 1_1
                .addEdges("1_1", "3_0", "1_1", "2_1")
                // 1_2
                .addEdges("1_2", "2_1")
                // 2_0
                .addEdges("2_0", "3_0")
                // 2_1
                // no outgoing edges
                .graph(DefaultDirectedWeightedGraph.class);

        figure_2_a = builder(emptyDirectedWeighted())
                .addVertices("3r", "2l", "2", "0", "1", "0l")
                .addEdges("0l", "1", "1", "2", "0", "2", "2", "3r", "2l", "3r", "3r", "2l")
                .graph(DefaultDirectedWeightedGraph.class);

        figure_2_b = builder(emptyDirectedWeighted())
                .addVertices("3r", "2l", "2", "0", "1", "0l")
                .addEdges("0l", "1", "1", "2", "0", "2", "2", "3r", "2l", "3r", "3r", "0l")
                .graph(DefaultDirectedWeightedGraph.class);

        figure_3_a = builder(emptyDirectedWeighted())
                .addVertices("a", "b", "c", "d", "e", "f")
                .addEdges("a", "b", "b", "c", "c", "d", "d", "e", "e", "f", "f", "a")
                .graph(DefaultDirectedWeightedGraph.class);

    }

    @Test
    public void testAlgorithm_1_Perfect_Hierarchy() {

        res = AgonyMinimizingHierarchy.calculateMaxEulerianSubgraph(figure_1_a);
        Subgraph[] dag_h = AgonyMinimizingHierarchy.createSubgraphs(res, figure_1_a);

        assertEquals(4, dag_h[0].edgeSet().size());
        assertEquals(0, dag_h[1].edgeSet().size());

        impl = new AgonyMinimizingHierarchy<String, DefaultWeightedEdge>(figure_1_a);
        CentralityResult<String> c = impl.calculate();

        assertEquals(0.0, c.get("0"));
        assertEquals(1.0, c.get("1"));
        assertEquals(2.0, c.get("2"));
        assertEquals(3.0, c.get("3"));
        assertEquals(4.0, c.get("4"));

        System.out.println("c = " + c);

        AsWeightedGraph<String, DefaultWeightedEdge> agony = impl.calculateAgony();

        for (DefaultWeightedEdge e : agony.edgeSet()) {
            assertEquals(0.0, agony.getEdgeWeight(e));
        }
    }

    @Test
    public void testAlgorithm_1_Some_Hierarchy() {

        res = AgonyMinimizingHierarchy.calculateMaxEulerianSubgraph(figure_2_a);
        Subgraph[] dag_h = AgonyMinimizingHierarchy.createSubgraphs(res, figure_2_a);

        assertEquals(2, dag_h[1].edgeSet().size());
        assertEquals(2, dag_h[1].vertexSet().size());

        assertEquals(5, dag_h[0].vertexSet().size());
        assertEquals(4, dag_h[0].edgeSet().size());

        impl = new AgonyMinimizingHierarchy<String, DefaultWeightedEdge>(figure_2_a);
        CentralityResult<String> c = impl.calculate();

        assertEquals(0.0, c.get("0"));

        System.out.println("c = " + c);

        AsWeightedGraph<String, DefaultWeightedEdge> agony = impl.calculateAgony();

        for (DefaultWeightedEdge e : agony.edgeSet()) {
            double w = agony.getEdgeWeight(e);
            String eStr = e.toString();
            if ("(3r : 2l)".equals(eStr)) {
                assertEquals(2.0, w);
            }
            else {
                assertEquals(0.0, w);
            }
        }
    }

    @Test
    public void testAlgorithm_1_No_Hierarchy() {

        res = AgonyMinimizingHierarchy.calculateMaxEulerianSubgraph(figure_3_a);
        Subgraph[] dag_h = AgonyMinimizingHierarchy.createSubgraphs(res, figure_3_a);

        assertEquals(6, dag_h[1].edgeSet().size());
        assertEquals(6, dag_h[1].vertexSet().size());

        assertEquals(0, dag_h[0].vertexSet().size());
        assertEquals(0, dag_h[0].edgeSet().size());

        impl = new AgonyMinimizingHierarchy<String, DefaultWeightedEdge>(figure_3_a);
        CentralityResult<String> c = impl.calculate();

        System.out.println("c = " + c);

        AsWeightedGraph<String, DefaultWeightedEdge> agony = impl.calculateAgony();

        for (DefaultWeightedEdge e : agony.edgeSet()) {
            assertEquals(1.0, agony.getEdgeWeight(e));
        }
    }

    /*
    @Test
    public void test_on_Real_Large_Graph() throws Exception {


        // download the cit-HepTh dataset.
        DirectedGraph<String, DefaultWeightedEdge> graph = emptyDirectedWeighted();

        Map vStr = new HashMap<String, String>();
        new DownloadHepThGraphGenerator().generateGraph(graph, new StringContinousFactory(), vStr);

        System.out.println("generated n=" + graph.vertexSet().size() + ", m=" + graph.edgeSet().size());

        impl = new AgonyMinimizingHierarchy<String, DefaultWeightedEdge>(graph);
        CentralityResult<String> c = impl.calculate();

        for (Map.Entry<String, Double> e : c.getSorted()) {
            System.out.println("label(" + e.getKey() + ") -> " + e.getValue());
        }

        AsWeightedGraph<String, DefaultWeightedEdge> agony = impl.calculateAgony();

        for (DefaultWeightedEdge e : agony.edgeSet()) {
            System.out.println("agony " + e + " " + agony.getEdgeWeight(e));
        }

    }
    */

    private static class DownloadHepThGraphGenerator implements GraphGenerator<String, DefaultWeightedEdge, String> {

        @Override
        public void generateGraph(Graph<String, DefaultWeightedEdge> graph, VertexFactory<String> vf, Map<String, String> map) {
            try {
                // open URL
                URL url = new URL("http://snap.stanford.edu/data/cit-HepTh.txt.gz");
                final BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(url.openStream())));

                System.out.println("generating ... ");

                while (true) {
                    String line = br.readLine();
                    if (line == null)
                        break;

                    if (line.startsWith("#"))
                        continue;

                    String[] a = line.split("\\s+");

                    if (a.length != 2)
                        continue;

                    String src = map.get(a[0]);
                    String dst = map.get(a[1]);
                    if (src == null) {
                        src = vf.createVertex();
                        graph.addVertex(src);
                        map.put(a[0], src);
                    }
                    if (dst == null) {
                        dst = vf.createVertex();
                        graph.addVertex(dst);
                        map.put(a[0], dst);
                    }

                    if (!graph.containsEdge(src, dst)) {
                        graph.addEdge(src, dst);
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
