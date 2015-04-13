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

import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.*;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class BellmanFordShortestPathWithNegativeCycleDetectorTest {

    BellmanFordShortestPathWithNegativeCycleDetector<String, DefaultWeightedEdge> impl;

    @Test
    public void testGetCost() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> graph = generate(new LinearGraphGenerator(5));
        graph.addVertex("n6");

        System.out.println("graph=" + graph);

        impl = new BellmanFordShortestPathWithNegativeCycleDetector<String, DefaultWeightedEdge>(graph, "n1");

        assertEquals(1.0, impl.getCost("n2"));
        assertEquals(4.0, impl.getCost("n5"));
        assertEquals(Double.POSITIVE_INFINITY, impl.getCost("n6"));
    }

    @Test
    public void testHasNegativeCycle() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> graph = generate(new RingGraphGenerator(5));

        assertTrue(graph.containsVertex("n1"));
        assertTrue(graph.containsVertex("n5"));
        assertTrue(graph.containsEdge("n5", "n1"));
        assertEquals(5, graph.edgeSet().size());

        impl = new BellmanFordShortestPathWithNegativeCycleDetector<String, DefaultWeightedEdge>(graph, "n1");

        assertFalse(impl.hasNegativeCycle());

        // make the ring value negative.
        for (DefaultWeightedEdge e : graph.edgeSet()) {
            graph.setEdgeWeight(e, -0.1);
        }

        // try on this graph.
        impl = new BellmanFordShortestPathWithNegativeCycleDetector<String, DefaultWeightedEdge>(graph, "n1");

        assertTrue(impl.hasNegativeCycle());

        List<DefaultWeightedEdge> vertices = impl.getNegativeCycleVertices();

        System.out.println("vertices = " + vertices);
    }
}
