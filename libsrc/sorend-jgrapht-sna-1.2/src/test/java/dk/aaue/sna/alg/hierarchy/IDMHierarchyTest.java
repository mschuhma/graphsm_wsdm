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

import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.ext.graphml.GraphMLImporter;
import org.jgrapht.DirectedGraph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static dk.aaue.sna.alg.JGraphTTests.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class IDMHierarchyTest {

    @Test
    public void testCalculate_Example() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> graph = builder(emptyWeighted())
                .addVertices("salem.a", "hani.h", "majed.m", "nawaf.a", "ahmed.a", "khalid.a",
                             "ahmed.alg", "hamza.a", "saead.a", "mohand.a", "ahmed.alh",
                             "fayez.a", "ziad.j", "marwan.a", "mohamed.a", "abdul.a",
                             "waleed.a", "satam.s", "wail.a")
                .addEdges("salem.a", "nawaf.a", "hani.h", "majed.m")
                .addEdges("nawaf.a", "hani.h", "hani.h", "khalid.a")
                .addEdges("nawaf.a", "khalid.a", "nawaf.a", "ahmed.a", "nawaf.a", "hamza.a", "nawaf.a", "saead.a")
                .addEdges("ahmed.a", "hamza.a", "ahmed.a", "saead.a")
                .addEdges("ahmed.alg", "hamza.a")
                .addEdges("hamza.a", "saead.a", "hamza.a", "mohand.a", "hamza.a", "ahmed.alh")
                .addEdges("saead.a", "ahmed.alh")
                .addEdges("mohand.a", "fayez.a")
                .addEdges("ahmed.alh", "ziad.j")
                .addEdges("fayez.a", "marwan.a")
                .addEdges("ziad.j", "marwan.a", "ziad.j", "mohamed.a")
                .addEdges("marwan.a", "mohamed.a", "marwan.a", "abdul.a")
                .addEdges("mohamed.a", "abdul.a")
                .addEdges("abdul.a", "waleed.a")
                .addEdges("waleed.a", "wail.a", "waleed.a", "satam.s")
                .addEdges("wail.a", "satam.s")
                .graph(WeightedGraph.class);

        assertEquals(19, graph.vertexSet().size());
        assertEquals(27, graph.edgeSet().size());

        // save(graph, "src/test/resources/IDM-test-original.graphml");

        DirectedGraph<String, DefaultWeightedEdge> directed =IDMHierarchy.calculateDirected(graph);

        System.out.println("directed = " + directed);

        // save(directed, "src/test/resources/IDM-test-directed.graphml");

        assertTrue(directed.containsEdge("nawaf.a", "salem.a"));
        assertTrue(directed.containsEdge("hani.h", "majed.m"));
        assertTrue(directed.containsEdge("nawaf.a", "hani.h"));

        DirectedGraph<String, DefaultWeightedEdge> tree = IDMHierarchy.calculateHierarchyDAG(directed);

        System.out.println("tree     = " + tree);

        // save(tree, "src/test/resources/IDM-test-tree.graphml");

        System.out.println("atta: " + tree.edgesOf("mohamed.a"));

        assertEquals(0, tree.outDegreeOf("mohamed.a"));
        assertEquals(4, tree.outDegreeOf("marwan.a"));

        CentralityResult<String> r = new IDMHierarchy(graph).calculate();

        System.out.println("r = " + r);

    }

    @Test
    public void testKrebsTerror2() throws Exception {

        GraphMLImporter<String, DefaultWeightedEdge> e = GraphMLImporter.createFromClasspathResource("/Krebs-terror2.xml");

        WeightedGraph<String, DefaultWeightedEdge> graph = emptyWeighted();
        Map<String, String> map = new HashMap<String, String>();
        e.generateGraph(graph, stringFactory(), map);

        CentralityResult<String> r = new IDMHierarchy(graph).calculate();

        assertNotNull(r);
    }

    @Test
    public void testCalculateDirected() throws Exception {

        GraphMLImporter<String, DefaultWeightedEdge> e = GraphMLImporter.createFromClasspathResource("/Krebs-terror2.xml");

        WeightedGraph<String, DefaultWeightedEdge> graph = emptyWeighted();
        Map<String, String> map = new HashMap<String, String>();
        e.generateGraph(graph, stringFactory(), map);

        System.out.println("graph    = " + graph);

        DirectedGraph<String, DefaultWeightedEdge> directed = IDMHierarchy.calculateDirected(graph);

        System.out.println("directed = " + directed);

        // save(directed, "src/test/resources/Krebs-terror2-directed.xml");
    }

}
