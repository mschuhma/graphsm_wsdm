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

package dk.aaue.sna.alg;

import dk.aaue.sna.generate.ErdosRenyiGraphGenerator;
import dk.aaue.sna.util.GraphBuilder;
import org.jgrapht.*;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.junit.Test;

import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.emptyWeighted;
import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class FloydWarshallAllShortestPathsTest {

    FloydWarshallAllShortestPaths impl;
    WeightedGraph<String, DefaultWeightedEdge> graph;

    @Test
    public void testGetShortestPath_01() {
        graph = generate(new CompleteGraphGenerator(5));
        impl = new FloydWarshallAllShortestPaths(graph);
        double length = impl.shortestDistance("n1", "n2");
        assertEquals(1.0, length, 0.001);
    }

    @Test
    public void testGetShortestPath_02() {
        graph = new ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);
        // build n1 <-> n2 <-> n3 <-> n4 <-> n5
        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addVertices("n4");
        g.addVertices("n5");
        g.addEdges("n1", "n2");
        g.addEdges("n2", "n3");
        g.addEdges("n3", "n4");
        g.addEdges("n4", "n5");

        impl = new FloydWarshallAllShortestPaths(graph);
        assertEquals(1.0, impl.shortestDistance("n1", "n2"), 0.001);
        assertEquals(2.0, impl.shortestDistance("n1", "n3"), 0.001);
        assertEquals(3.0, impl.shortestDistance("n1", "n4"), 0.001);
        assertEquals(4.0, impl.shortestDistance("n1", "n5"), 0.001);

        assertEquals(2.0, impl.shortestDistance("n2", "n4"), 0.001);
    }

    @Test
    public void testGetShortestPath_03() {
        graph = new ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);

        // build n1 <-> n2 <-> n3 <-> n4 <-> n5
        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addVertices("n4");
        g.addVertices("n5");
        g.addEdges("n1", "n2");
        g.addEdges("n2", "n3");
        g.addEdges("n3", "n4");
        g.addEdges("n4", "n5");

        impl = new FloydWarshallAllShortestPaths(graph);
        impl.lazyCalculatePaths();

        GraphPath<String, DefaultWeightedEdge> sp = impl.shortestPath("n1", "n2");

        List<String> n = Graphs.getPathVertexList(sp);

        System.out.println("Edges: " + sp + ", nodes: " + n);

        assertEquals(sp.getEdgeList().size() + 1, n.size());

        assertEquals("n1", n.get(0));
        assertEquals("n2", n.get(1));

        impl.shortestPath("n1", "n5");
    }

    @Test
    public void testGetShortestPath_04() {

        graph = new ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);

        // build star graph with 1 in middle
        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addVertices("n4");
        g.addVertices("n5");
        g.addEdges("n1", "n2");
        g.addEdges("n1", "n3");
        g.addEdges("n1", "n4");
        g.addEdges("n1", "n5");

        impl = new FloydWarshallAllShortestPaths(graph);
        // we need to do this in order to get paths.
        impl.lazyCalculatePaths();

        impl.shortestPath("n1", "n2");
        impl.shortestPath("n1", "n5");

    }


    @Test
    public void testGetShortestPath_NotConnected() {

        graph = new ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);

        // build not connected graph; n1 <--> n2    n3
        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addEdges("n1", "n2");

        impl = new FloydWarshallAllShortestPaths(graph);

        assertEquals(1.0, impl.shortestDistance("n1", "n2"), 0.001);
        assertEquals(Double.POSITIVE_INFINITY, impl.shortestDistance("n1", "n3"), 0.001);

        GraphPath<String, DefaultWeightedEdge> edges = impl.shortestPath("n1", "n2");
        assertEquals(1, edges.getEdgeList().size());

        edges = impl.shortestPath("n1", "n3");
        System.out.println("edges=" + edges);
        assertNull(edges);
    }

    @Test
    public void testGetShortestPath_CalculatePaths() {

        graph = generate(new LinearGraphGenerator(5));

        impl = new FloydWarshallAllShortestPaths(graph);
        int paths = impl.lazyCalculatePaths();

        assertEquals(20, paths);

        assertEquals(4, impl.getShortestPaths("n1").size());
        assertEquals(4, impl.getShortestPaths("n2").size());
        assertEquals(4, impl.getShortestPaths("n3").size());
        assertEquals(4, impl.getShortestPaths("n4").size());
        assertEquals(4, impl.getShortestPaths("n5").size());

        System.out.println("paths.n1=" + impl.getShortestPaths("n1"));
        System.out.println("paths.n2=" + impl.getShortestPaths("n2"));
        System.out.println("paths.n3=" + impl.getShortestPaths("n3"));
        System.out.println("paths.n4=" + impl.getShortestPaths("n4"));
        System.out.println("paths.n5=" + impl.getShortestPaths("n5"));
    }


    @Test
    public void testCompareJGraphTFWAlg() {


        Graph<String, DefaultWeightedEdge> g = generate(new ErdosRenyiGraphGenerator(100, 0.5, null));

        System.out.println("Warmup phase...");

        for (int i = 0; i < 1; i++) {
            FloydWarshallShortestPaths<String, DefaultWeightedEdge> fw = new FloydWarshallShortestPaths(g);
            fw.shortestDistance("n1", "n2");
            FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw2 = new FloydWarshallAllShortestPaths(g);
            fw2.shortestDistance("n1", "n2");
        }

        System.out.println("Running .. ");

        long start = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            FloydWarshallShortestPaths<String, DefaultWeightedEdge> fw = new FloydWarshallShortestPaths(g);
            System.out.println("length = " + fw.shortestDistance("n1", "n2"));
        }

        System.out.println("jgrapht = " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();

        for (int i = 0; i < 50; i++) {
            FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw2 = new FloydWarshallAllShortestPaths(g);
            System.out.println("length = " + fw2.shortestDistance("n1", "n2"));
        }

        System.out.println("prototype = " + (System.currentTimeMillis() - start));

    }

    @Test
    public void testCompareJGraphTFWAlg_02() {


        Graph<String, DefaultWeightedEdge> g = generate(new ErdosRenyiGraphGenerator(25, 0.2, null));

        FloydWarshallShortestPaths<String, DefaultWeightedEdge> fw = new FloydWarshallShortestPaths(g);
        FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw2 = new FloydWarshallAllShortestPaths(g);

        for (String v_i : g.vertexSet()) {
            for (String v_j : g.vertexSet()) {
                if (v_i == v_j)
                    continue;

                double sp1 = fw.shortestDistance(v_i, v_j);
                double sp2 = fw2.shortestDistance(v_i, v_j);

                System.out.println("(" + v_i + ", " + v_j + ") = { " + sp1 + ", " + sp2 + " }");

                assertEquals(sp1, sp2, 0.001);
            }
        }
    }

}
