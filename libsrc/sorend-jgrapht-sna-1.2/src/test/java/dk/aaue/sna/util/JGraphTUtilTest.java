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

package dk.aaue.sna.util;

import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import dk.aaue.sna.alg.VertexPair;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * @author Soren <soren@tanesha.net>
 */
public class JGraphTUtilTest {

    WeightedGraph<String, DefaultWeightedEdge> graph;
    FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw;

    @Before
    public void setUp() {
        graph = generate(new LinearGraphGenerator(10));
        fw = new FloydWarshallAllShortestPaths(graph);
    }

    @Test
    public void testDiameterVertices() throws Exception {

        VertexPair<String> pair = JGraphTUtil.diameterVertices(graph, fw);

        assertEquals("n1", pair.getFirst().toString());
        assertEquals("n10", pair.getSecond().toString());
    }

    @Test
    public void testEffectivediameter() throws Exception {

        double effective = JGraphTUtil.effectivediameter(graph, fw);

        assertEquals(7.0, effective, 0.01);

    }

    @Test
    public void testCloning() throws Exception {

        Graph<String, DefaultEdge> g = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        assertTrue(g instanceof SimpleGraph);
        Graph<String, DefaultEdge> c = JGraphTUtil.clone(g);
        assertTrue(c instanceof SimpleGraph);
        assertEquals(c.getEdgeFactory().getClass(), g.getEdgeFactory().getClass());

        g = new ListenableUndirectedWeightedGraph<String, DefaultEdge>(DefaultEdge.class);
        c = JGraphTUtil.clone(g);

        assertTrue(c instanceof ListenableUndirectedWeightedGraph);
        assertEquals(c.getEdgeFactory().getClass(), g.getEdgeFactory().getClass());

    }
}
