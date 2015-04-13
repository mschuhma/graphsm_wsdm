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
// import dk.aaue.fuzzy.MembershipFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Core/periphery graph synthesizing.
 *
 * @author Soren <soren@tanesha.net>
 */
public class CorePeripheryGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    private int n;
    // protected MembershipFunction P;
    private Random random;

    public CorePeripheryGraphGenerator(int n, Long seed) {
        this.n = n;
        // this.P = P;
        this.random = new Random();
        if (seed != null)
            random.setSeed(seed);
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        // add all the nodes
        List<V> nl = new ArrayList<V>();
        double[] dl = new double[n];
        for (int i = 0; i < n; i++) {
            double Pi = 1.0; // P.membership(i);
            V node = vVertexFactory.createVertex();

            nl.add(node);
            dl[i] = Pi;
            veGraph.addVertex(node);
        }

        for (int i = 0; i < dl.length; i++) {
            // probability of the node
            double Pi = dl[i];
            for (int j = i + 1; j < dl.length; j++) {
                double Pj = dl[j];
                // if randomly more than the probability then add edge
                double rP = random.nextDouble();
                if (rP < Math.min(Pi, Pj)) {
                    veGraph.addEdge(nl.get(i), nl.get(j));
                }
            }
        }
    }
}
