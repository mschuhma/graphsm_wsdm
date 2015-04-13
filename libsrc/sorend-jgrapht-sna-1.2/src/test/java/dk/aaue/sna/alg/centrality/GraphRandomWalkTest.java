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

import dk.aaue.sna.generate.ErdosRenyiGraphGenerator;
import dk.aaue.sna.util.GraphBuilder;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.emptyWeighted;
import static dk.aaue.sna.alg.JGraphTTests.generate;

/**
 * @author Soren <soren@tanesha.net>
 */
public class GraphRandomWalkTest {

    @Test
    public void testLargeRandomWalk() throws Exception {
        UndirectedGraph<String, DefaultEdge> graph = (UndirectedGraph) generate(new ErdosRenyiGraphGenerator(50, 0.5, null));

        ConnectivityInspector<String, DefaultEdge> ci = new ConnectivityInspector<String, DefaultEdge>(graph);

        System.out.println("connected=" + ci.isGraphConnected());
        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);

        int sum = 0;
        for (int i = 0; i < 100000; i++) {
            List<String> walk = new GraphRandomWalk(graph).randomWalk(g.node("n1"), g.node("n50"), 2000);

            // System.out.println("walk("+walk.size()+")=" + walk);

            sum += walk.size();
        }

        System.out.println("avg. walk length: " + (sum / 100000));
    }

    @Test
    public void testRandomWalk() throws Exception {

        Graph<String, DefaultWeightedEdge> graph = emptyWeighted();

        GraphBuilder<String, DefaultWeightedEdge, String> g = new GraphBuilder(graph);

        g.addVertices("n1", "n2", "n3", "n4", "n5", "n6");

        g.addEdges("n1", "n2");
        g.addEdges("n2", "n3");
        g.addEdges("n3", "n4");
        g.addEdges("n2", "n5");
        g.addEdges("n3", "n6");

        List<String> walk = new GraphRandomWalk(graph).randomWalk(g.node("n1"), g.node("n4"), 400);

        System.out.println("Walk = " + walk);
    }


}
