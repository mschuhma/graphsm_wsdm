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
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

import java.util.*;

/**
 * Creates a tree with scale-free structuring.
 *
 * @author Soren <soren@tanesha.net>
 */
public class ScaleFreeTreeGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    private Random random;
    private int n;
    private long seed;

    protected ScaleFreeTreeGraphGenerator(int n, Long seed) {
        this.random = new Random();
        if (seed != null)
            this.random.setSeed(seed);
        this.n = n;
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        if (n > 1) {
            V n = vVertexFactory.createVertex();
            veGraph.addVertex(n);
        }

        int maxDegree = 0;

        for (int i = 1; i < n; i++) {
            // create random list of nodes
            List<V> currentNodes = new ArrayList<V>(veGraph.vertexSet());
            Collections.shuffle(currentNodes, random);

            // add the new vertex
            V newNode = vVertexFactory.createVertex();
            veGraph.addVertex(newNode);

            for (V node : currentNodes) {
                double p = 1.0;
                int nodeDegree = veGraph.edgesOf(node).size();
                if (maxDegree > 0) {
                    p = (double) nodeDegree / maxDegree;
                }

                if (random.nextDouble() < p) {
                    // add edge
                    veGraph.addEdge(newNode, node);

                    // update max degree
                    if (nodeDegree + 1 > maxDegree)
                        maxDegree = nodeDegree + 1;

                    // only attach one node, so break the loop now.
                    break;
                }
            }
        }
    }
}
