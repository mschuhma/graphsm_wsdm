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

package dk.aaue.sna.generate;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.generate.GraphGenerator;

import java.util.*;

/**
 * @author Soren <soren@tanesha.net>
 */
public class FixedNumberOfRandomEdgesGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    private int n;
    private int m;
    private Random random;

    /**
     * @param n    number of nodes to generate
     * @param m    the number of edges
     * @param seed a seed for the random generator
     */
    public FixedNumberOfRandomEdgesGraphGenerator(int n, int m, Long seed) {
        this.n = n;
        this.m = m;
        this.random = new Random();
        if (seed != null)
            this.random.setSeed(seed);
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringTMap) {

        int maxDegree = n * (n - 1) / 2;

        if (m >= maxDegree) {
            new CompleteGraphGenerator<V, E>(n).generateGraph(veGraph, vVertexFactory, stringTMap);
            return;
        }

        // populate graph with nodes
        List<V> nodes = new ArrayList<V>();
        for (int i = 0; i < n; i++) {
            V node = vVertexFactory.createVertex();
            veGraph.addVertex(node);
            nodes.add(node);
        }

        for (int i = 0; i < m; i++) {

            // shuffle node list.
            Collections.shuffle(nodes, random);

            V found = null;
            for (V node : nodes) {
                if (veGraph.edgesOf(node).size() < maxDegree) {
                    found = node;
                    break;
                }
            }

            if (found == null)
                throw new RuntimeException("No node found with deg(v) < maxDegree");

            // find not-neighbors
            List<V> notNeighbors = new ArrayList<V>(nodes);
            notNeighbors.remove(found);
            notNeighbors.removeAll(Graphs.neighborListOf(veGraph, found));

            // pick random and add edge.
            V newNeighbor = notNeighbors.get(random.nextInt(notNeighbors.size()));
            veGraph.addEdge(found, newNeighbor);
        }
    }

}
