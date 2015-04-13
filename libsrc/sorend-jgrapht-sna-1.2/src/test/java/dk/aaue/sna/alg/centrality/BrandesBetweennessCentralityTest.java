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

import dk.aaue.sna.util.GraphBuilder;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class BrandesBetweennessCentralityTest {

    @Test
    public void testBrandesCentralityComplete() {

        WeightedGraph<String, DefaultWeightedEdge> G = generate(new CompleteGraphGenerator(10));

        CentralityResult<String> c = new BrandesBetweennessCentrality(G).calculate();

        System.out.println("C = " + c);

        for (Entry<String, Double> pair : c.getRaw().entrySet()) {
            System.out.println(String.format("Centrality: %s = %.1f", pair.getKey(), pair.getValue()));
            // has to be 0, there are no shortest paths passing anywhere, since the graph is complete.
            assertEquals(0.0, pair.getValue());
        }
    }

    public void testBrandesCentralityLinear() {

        WeightedGraph<String, DefaultWeightedEdge> graph = generate(new LinearGraphGenerator(5));

        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder<String, DefaultWeightedEdge, String>(graph);

        assertNotNull(g.node("n1"));
        assertNotNull(g.node("n5"));

        CentralityResult<String> c = new BrandesBetweennessCentrality(graph).calculate();

        System.out.println("c=" + c);

        // edges have no passing paths
        assertEquals(0.0, c.getRaw().get(g.node("n1")));
        assertEquals(0.0, c.getRaw().get(g.node("n5")));
        // others have two passing paths (x2 beause graph is undirected)
        assertEquals(6.0, c.getRaw().get(g.node("n2")));
        assertEquals(6.0, c.getRaw().get(g.node("n4")));
        // middle
        assertEquals(8.0, c.getRaw().get(g.node("n3")));

        List<String> sorted = c.getSortedNodes();
        System.out.println("sorted = " + sorted);
        // top node should be n3
        assertEquals("n3", sorted.get(0));
    }
}
