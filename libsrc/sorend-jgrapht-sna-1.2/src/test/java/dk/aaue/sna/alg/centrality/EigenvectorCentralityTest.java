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
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static junit.framework.Assert.assertEquals;

public class EigenvectorCentralityTest {

    EigenvectorCentrality impl;

    @Test
    public void testEigenvectorCentrality() {

        //Graph<MyNode, DefaultEdge> g = new RandomGraphBuilder().randomErdosRenyi(10, 0.5, null);
        WeightedGraph<String, DefaultWeightedEdge> g = generate(new LinearGraphGenerator(5));
        CentralityResult v = new EigenvectorCentrality(g).calculate();
        List<String> nodes = v.getSortedNodes();

        CentralityResult v2 = new EigenvectorCentrality(g).calculate();
        List<String> nodes2 = v2.getSortedNodes();

        System.out.println("v = " + v);
        System.out.println("n1 = " + nodes);
        System.out.println("n2 = " + nodes2);

        assertEquals("n3", nodes2.get(0));
        assertEquals(nodes.get(0), nodes2.get(0));
    }

    @Test
    public void testEigenvectorCentrality_NonConnected() {
        WeightedGraph<String, DefaultWeightedEdge> g = generate(new LinearGraphGenerator(5));

        // add a node which is not connected
        GraphBuilder<String, DefaultWeightedEdge, String> gh = new GraphBuilder(g);
        gh.addVertices("notcon");

        CentralityResult v = new EigenvectorCentrality(g).calculate();
        List<String> nodes = v.getSortedNodes();

        System.out.println("v = " + v);
        System.out.println("n1 = " + nodes);

    }

}
