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

import dk.aaue.sna.util.GraphBuilder;
import junit.framework.TestCase;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

/**
 * Test for Prime Paths
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class PrimePathsTest extends TestCase {

    public void testKereena_Testing() throws Exception {

        GraphBuilder<String, DefaultEdge, String> b = new GraphBuilder<String, DefaultEdge, String>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class));

        // grammar for the graph
        DirectedGraph<String, DefaultEdge> graph =
            b.addVertices("0", "the", "noun_0", "verb", "preposition", "adjective", ",", "a", "noun_1", "true", "false")
                .addEdges("0", "the", "0", "false")
                .addEdges("the", "noun_0", "noun_0", "verb", "verb", "true")
                .addEdges("the", "adjective", "adjective", ",", ",", "adjective", "adjective", "noun_0")
                .addEdges("verb", "preposition", "preposition", "a", "a", "noun_1", "noun_1", "true")
                // false edges
                .addEdges("the", "false", "noun_0", "false", "verb", "false", "preposition", "false", "adjective", "false")
                .addEdges(",", "false", "a", "false", "noun_1", "false")
                // as graph
                .graph(DirectedGraph.class);

        PrimePaths<String, DefaultEdge> pp = new PrimePaths<String, DefaultEdge>(graph);
        List<PrimePaths.SimplePath<String>> paths = pp.calculate();

        for (PrimePaths.SimplePath<String> p : paths) {
            System.out.println(p.nodes());
        }

        System.out.println("len(paths) = " + paths.size());
    }


    public void testCalculate() throws Exception {

        DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

        for (int i = 0; i <= 6; i++)
            graph.addVertex("" + i);

        graph.addEdge("0", "4");
        graph.addEdge("0", "1");
        graph.addEdge("1", "2");
        graph.addEdge("1", "5");
        graph.addEdge("2", "3");
        graph.addEdge("3", "1");
        graph.addEdge("4", "4");
        graph.addEdge("4", "6");
        graph.addEdge("5", "6");

        PrimePaths<String, DefaultEdge> pp = new PrimePaths<String, DefaultEdge>(graph);
        List<PrimePaths.SimplePath<String>> paths = pp.calculate();

        assertEquals(8, paths.size());

        System.out.println("paths = " + paths);
    }

    public void testSubpathOf() throws Exception {
        PrimePaths.SimplePath<String> p = new PrimePaths.SimplePath<String>().add("a");
        PrimePaths.SimplePath<String> q = new PrimePaths.SimplePath<String>().add("a").add("b").add("c").add("d");

        assertNotNull(p);
        assertNotNull(q);

        assertTrue(p.subpathOf(p));
        assertTrue(q.subpathOf(q));

        assertTrue(p.subpathOf(q));
        assertFalse(q.subpathOf(p));

        p = p.add("b");

        assertTrue(p.subpathOf(q));
        assertFalse(q.subpathOf(p));

        p = p.add("c").add("d");

        assertTrue(p.subpathOf(q));
        assertTrue(q.subpathOf(p));

        p = new PrimePaths.SimplePath<String>().add("b");

        assertTrue(p.subpathOf(q));
        assertFalse(q.subpathOf(p));

        p = p.add("c");

        assertTrue(p.subpathOf(q));
        assertFalse(q.subpathOf(p));

        p = p.add("c");

        assertFalse(p.subpathOf(q));
        assertFalse(q.subpathOf(p));

    }
}
