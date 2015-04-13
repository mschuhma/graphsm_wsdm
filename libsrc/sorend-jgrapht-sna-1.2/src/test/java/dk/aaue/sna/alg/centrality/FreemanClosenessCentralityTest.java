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

import fuzzy4j.aggregation.OWA;
import fuzzy4j.aggregation.weighted.AsWeightedAggregation;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Soren <soren@tanesha.net>
 */
public class FreemanClosenessCentralityTest {

    SimpleGraph<String, DefaultEdge> G;
    FreemanClosenessCentrality<String, DefaultEdge> impl;

    @Before
    public void setup() {
        G = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
    }

    @Test
    public void testSimpleExample() {


        for (String v : Arrays.asList("A", "B", "C", "D", "E", "F"))
            G.addVertex(v);

        G.addEdge("A", "B");
        G.addEdge("B", "C");
        G.addEdge("C", "D");
        G.addEdge("C", "E");
        G.addEdge("C", "F");


        impl = new FreemanClosenessCentrality<String, DefaultEdge>(G);


        System.out.println(impl.calculate());

        System.out.println("fuz = " + new FuzzyClosenessCentrality(G, new AsWeightedAggregation(OWA.MEOWA_FACTORY(6, 0.9))).calculate());

    }
}
