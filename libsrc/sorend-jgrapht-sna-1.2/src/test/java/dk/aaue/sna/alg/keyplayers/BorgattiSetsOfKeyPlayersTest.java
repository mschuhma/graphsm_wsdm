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

package dk.aaue.sna.alg.keyplayers;

import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import dk.aaue.sna.ext.graphml.GraphMLImporter;
import dk.aaue.sna.util.StringContinousFactory;
import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;
import static org.junit.Assert.*;
import static dk.aaue.sna.alg.JGraphTTests.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: sorend
 * Date: Dec 7, 2010
 * Time: 8:32:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class BorgattiSetsOfKeyPlayersTest {


    @Test
    public void testCalculate() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> e = emptyWeighted();
        GraphGenerator<String, DefaultWeightedEdge, String> gg = new CompleteGenerator<String, DefaultWeightedEdge>(3);
        VertexFactory<String> vf =   new StringContinousFactory();
        gg.generateGraph(e, vf, null);

        e.addVertex("i1");
        e.addVertex("i2");
        e.addVertex("i3");

        e.addEdge("n1", "i1");
        e.addEdge("n2", "i2");
        e.addEdge("n3", "i3");

        System.out.println("graph=" + e);

        BorgattiSetsOfKeyPlayers.calculateKPPNeg(e, 3);
    }

    @Test
    public void testCalculateKPPPos() throws Exception {

        WeightedGraph<String, DefaultWeightedEdge> e = emptyWeighted();
        GraphGenerator<String, DefaultWeightedEdge, String> gg = new CompleteGenerator<String, DefaultWeightedEdge>(3);
        VertexFactory<String> vf =   new StringContinousFactory();
        gg.generateGraph(e, vf, null);

        e.addVertex("i1");
        e.addVertex("i2");
        e.addVertex("i3");

        e.addEdge("n1", "i1");
        e.addEdge("n2", "i2");
        e.addEdge("n3", "i3");

        System.out.println("graph=" + e);

        BorgattiSetsOfKeyPlayers.calculateKPPPos(e, 3);
    }

    private class CompleteGenerator<V, E> implements GraphGenerator<V, E, V> {
        int n;

        private CompleteGenerator(int n) {
            this.n = n;
        }

        @Override
        public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {
            List<V> nn = new ArrayList<V>();
            for (int i = 0; i < n; i++) {
                V a = vVertexFactory.createVertex();
                veGraph.addVertex(a);
                nn.add(a);
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++)
                    veGraph.addEdge(nn.get(i), nn.get(j));
            }
        }
    }

    @Test
    public void testCalculateDRMeasure() {

        WeightedGraph<String, DefaultWeightedEdge> e = emptyWeighted();
        GraphGenerator<String, DefaultWeightedEdge, String> gg = new CompleteGenerator<String, DefaultWeightedEdge>(3);
        VertexFactory<String> vf =   new StringContinousFactory();
        gg.generateGraph(e, vf, null);

        Set<String> S = new HashSet<String>(e.vertexSet());

        e.addVertex("i1");
        e.addVertex("i2");
        e.addVertex("i3");

        System.out.println("graph=" + e);

        List<String> V = new ArrayList<String>(e.vertexSet());
        FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw = new FloydWarshallAllShortestPaths(e);
        double complete = BorgattiSetsOfKeyPlayers.calculateDRMeasure(V, fw, S);

        System.out.println("DR=" + complete);

        e.addEdge("n1", "i1");
        e.addEdge("n2", "i2");
        e.addEdge("n3", "i3");

        FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw2 = new FloydWarshallAllShortestPaths(e);
        double complete2 = BorgattiSetsOfKeyPlayers.calculateDRMeasure(V, fw2, S);

        System.out.println("DR2=" + complete2);

    }

    @Test
    public void testCalculateKPPOnKrebs() {

        WeightedGraph<String, DefaultWeightedEdge> e = emptyWeighted();
        GraphMLImporter gg = GraphMLImporter.createFromClasspathResource("/Krebs-terror1.xml");
        VertexFactory<String> vf =   new StringContinousFactory();
        Map<String, String> m = new HashMap<String, String>();
        gg.generateGraph(e, vf, m);

        Set<String> neg = BorgattiSetsOfKeyPlayers.calculateKPPNeg(e, 3);
        for (String n : neg) {
            System.out.println("neg(" + n + ") -> " + m.get(n));
        }

        Set<String> pos = BorgattiSetsOfKeyPlayers.calculateKPPPos(e, 3);
        for (String n : pos) {
            System.out.println("pos(" + n + ") -> " + m.get(n));
        }

    }

    @Test
    public void testCalculateDFMeasure() {

        WeightedGraph<String, DefaultWeightedEdge> e = emptyWeighted();
        GraphGenerator<String, DefaultWeightedEdge, String> gg = new CompleteGenerator<String, DefaultWeightedEdge>(5);
        VertexFactory<String> vf =   new StringContinousFactory();
        gg.generateGraph(e, vf, null);
        gg.generateGraph(e, vf, null);

        System.out.println("graph=" + e);

        List<String> V = new ArrayList<String>(e.vertexSet());
        FloydWarshallAllShortestPaths<String, DefaultWeightedEdge> fw = new FloydWarshallAllShortestPaths(e);
        double complete = BorgattiSetsOfKeyPlayers.calculateDFMeasure(V, fw);

        assertEquals(0.5555, complete, 0.001);
        System.out.println("complete=" + complete);

    }
}
