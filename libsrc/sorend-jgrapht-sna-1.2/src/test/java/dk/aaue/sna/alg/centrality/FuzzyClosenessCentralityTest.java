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

package dk.aaue.sna.alg.centrality;

import fuzzy4j.aggregation.DombiIntersection;
import fuzzy4j.aggregation.weighted.AsWeightedAggregation;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author Soren <soren@tanesha.net>
 */
public class FuzzyClosenessCentralityTest {

    SimpleGraph<String, DefaultWeightedEdge> graph;

    @Before
    public void configure() {

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

    @Test
    public void calculateForSimpleGraph() throws Exception {

        CentralityResult<String> normal =
                new LatoraClosenessCentrality<String, DefaultWeightedEdge>(
                        graph).calculate();

        CentralityResult<String> fuzzy =
            new FuzzyClosenessCentrality<String, DefaultWeightedEdge>(
                    graph, AsWeightedAggregation.asWeighted(DombiIntersection.BY_DRASTICALITY.create(0.1))).calculate();

        System.out.println("normal = " + normal);
        System.out.println("res = " + fuzzy);

        /*
        assertNotNull(fuzzy.get("b"));
        assertEquals(1.0, fuzzy.get("b"));
        assertNotNull(fuzzy.get("c"));
        assertEquals(0.5, fuzzy.get("c"));
        assertNull(fuzzy.get("d"));
        assertEquals(0.25, fuzzy.get("cc"));
        */
    }

}
