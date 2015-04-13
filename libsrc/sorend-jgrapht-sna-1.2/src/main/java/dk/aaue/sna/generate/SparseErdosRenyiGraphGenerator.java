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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * An optimized implementation to generate ER graphs, based on that <code>p</code> will be small, hence the
 * resulting graph will be sparse.
 *
 * @author Soren <soren@tanesha.net>
 */
public class SparseErdosRenyiGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    private double p;
    private Random random;
    private int n;

    /**
     * Constructor
     *
     * @param p    Probability for edge creation
     * @param n the number of nodes to generate
     * @param seed Random seed, <code>null</code> for none.
     */
    public SparseErdosRenyiGraphGenerator(int n, double p, Long seed) {
        this.n = n;
        this.random = new Random();
        if (seed != null)
            this.random.setSeed(seed);
        this.p = p;
    }

    /**
     * Create an Erdos-Renyi random graph (also called binominal random graph).
     * This is a sparse version, that works faster when <code>p</code> is small.
     *
     * @param g The graph to operate on
     * @param vVertexFactory a vertex factory
     * @param map not used
     */
    public void generateGraph(Graph<V, E> g, VertexFactory<V> vVertexFactory, Map<String, V> map) {

        List<V> nodes = new ArrayList<V>();
        for (int i = 0; i < n; i++) {
            V node = vVertexFactory.createVertex();
            g.addVertex(node);
            nodes.add(node);
        }

        int v = 1;
        int w = -1;
        double lp = Math.log(1.0 - p);

        while (v < n) {
            double lr = Math.log(1.0 - random.nextDouble());

            w = w + 1 + new Double(lr / lp).intValue();

            while (w >= v && v < n) {
                w = w - v;
                v = v + 1;
            }

            if (v < n) {
                // add an edge
                g.addEdge(nodes.get(v), nodes.get(w));
            }
        }
    }
}
