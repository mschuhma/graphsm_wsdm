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

import junit.framework.TestCase;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.ArrayList;
import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.generate;

public class DegreeCentralityTest extends TestCase {

    public void testDegCent_Complete() {

        WeightedGraph<String, DefaultWeightedEdge> G = generate(new CompleteGraphGenerator(5));

        CentralityResult<String> c = new DegreeCentrality(G).calculate();

        System.out.println("complete = " + c);

        assertEquals(5, c.getRaw().keySet().size());

        for (String n : c.getRaw().keySet()) {
            assertEquals(1.0, c.getRaw().get(n));
        }

    }

    public void testDegCent_Linear() {

        WeightedGraph<String, DefaultWeightedEdge> G = generate(new LinearGraphGenerator(5));

        System.out.println("G = " + G);

        CentralityResult<String> c = new DegreeCentrality(G).calculate();

        List<String> sorted = c.getSortedNodes();

        System.out.println("linear = " + c + ", sorted=" + sorted);

        assertEquals(5, c.getRaw().keySet().size());

        assertEquals(0.25, c.getRaw().get("n1"));
        assertEquals(0.5, c.getRaw().get("n2"));
        assertEquals(0.5, c.getRaw().get("n3"));
        assertEquals(0.5, c.getRaw().get("n4"));
        assertEquals(0.25, c.getRaw().get("n5"));

        List<String> l = new ArrayList<String>();
        l.add("n3");
        l.add("n2");
        l.add("n4");
        for (int i = 0; i < 3; i++) {
            assertTrue(l.contains(sorted.get(i)));
        }
    }

}
