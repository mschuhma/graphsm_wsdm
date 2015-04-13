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
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.assertEquals;

/**
 * @author Soren <soren@tanesha.net>
 */
public class EdgeDisjointPathsTest {

    @Test
    public void testWater() {
        // do nothing
    }

    /*
    @Test
    public void testCalculate_Big() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new ErdosRenyiGraphGenerator(50, 0.15, null));

        long start = System.currentTimeMillis();
        for (String n : g.vertexSet()) {
            Map<String, List<GraphPath<String, DefaultEdge>>> r =
                    new EdgeDisjointPaths(g, 10).calculate(n);
            System.out.println("r = " + r);
        }
        long end = System.currentTimeMillis();

        System.out.println("time spent: " + (end - start) + "ms");

    }


    @Test
    public void testCalculate() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new LinearGraphGenerator(10));

        Map<String, List<GraphPath<String, DefaultEdge>>> r =
                new EdgeDisjointPaths(g, 10).calculate("n1");

        System.out.println("r = " + r);

        for (String k : r.keySet()) {
            List<GraphPath<String, DefaultEdge>> v = r.get(k);
            assertEquals(1, v.size());
        }

    }

    @Test
    public void testCalculatePair() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new LinearGraphGenerator(10));

        List<GraphPath<String, DefaultEdge>> r =
                new EdgeDisjointPaths(g, 10).calculatePair("n1", "n10");

        System.out.println("r = " + r);

        assertEquals(1, r.size());

    }
    */
}
