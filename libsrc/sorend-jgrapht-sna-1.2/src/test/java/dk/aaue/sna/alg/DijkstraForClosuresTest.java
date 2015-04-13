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

import fuzzy4j.aggregation.AlgebraicProduct;
import junit.framework.TestCase;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class DijkstraForClosuresTest extends TestCase {

    SimpleGraph<String, DefaultWeightedEdge> graph;

    public void setUp() {

        graph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);

        graph.addVertex("a");
        graph.addVertex("b");
        graph.addVertex("c");
        graph.addVertex("d");
        graph.addVertex("cc");

        Graphs.addEdge(graph, "a", "b", 1.0);
        Graphs.addEdge(graph, "b", "c", 0.5);
        Graphs.addEdge(graph, "c", "cc", 0.5);
    }

    public void testDistance() throws Exception {

        PathCostCalculator<String, DefaultWeightedEdge> pc =
                new PathCostCalculator.AggregatedCost<String, DefaultWeightedEdge>(PathCostCalculator.SUM);

        DijkstraForClosures<String, DefaultWeightedEdge> d =
                new DijkstraForClosures<String, DefaultWeightedEdge>(
                        graph,
                        DijkstraForClosures.SHORTEST_PATH,
                        pc,
                        0.0,
                        "a"
                );

        assertNotNull(d.get("b"));
        assertEquals(1.0, d.get("b"));
        assertNotNull(d.get("c"));
        assertEquals(1.5, d.get("c"));
        assertEquals(2.0, d.get("cc"));
        assertNull(d.get("d"));
    }

    public void testStrongest() throws Exception {

        PathCostCalculator<String, DefaultWeightedEdge> pc =
                new PathCostCalculator.AggregatedCost<String, DefaultWeightedEdge>(AlgebraicProduct.INSTANCE);

        DijkstraForClosures<String, DefaultWeightedEdge> d =
                new DijkstraForClosures<String, DefaultWeightedEdge>(
                        graph,
                        DijkstraForClosures.STRONGEST_PATH,
                        pc,
                        1.0,
                        "a"
                );

        assertNotNull(d.get("b"));
        assertEquals(1.0, d.get("b"));
        assertNotNull(d.get("c"));
        assertEquals(0.5, d.get("c"));
        assertNull(d.get("d"));
        assertEquals(0.25, d.get("cc"));

    }
}
