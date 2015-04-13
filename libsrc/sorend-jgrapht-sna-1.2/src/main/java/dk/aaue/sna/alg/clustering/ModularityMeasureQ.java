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

package dk.aaue.sna.alg.clustering;

import org.jgrapht.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementationof the Q Modularity Measure.
 *
 * See:
 *  Newman 2004
 *
 * @author Soren <soren@tanesha.net>
 */
public class ModularityMeasureQ<V, E> {

    private List<Set<V>> clusters;
    private Graph<V, E> graph;

    public ModularityMeasureQ(Graph<V, E> graph, Set<Set<V>> clusters) {
        this.clusters = new ArrayList<Set<V>>();
        this.clusters.addAll(clusters);
        this.graph = graph;
    }

    public double calculate() {

        double allEdges = graph.edgeSet().size();

        double[][] E = new double[clusters.size()][clusters.size()];
        double[] A = new double[clusters.size()];

        for (int i = 0; i < E.length; i++) {
            Set<V> cluster_i = clusters.get(i);
            for (int j = 0; j < E.length; j++) {
                Set<V> cluster_j = clusters.get(j);
                double count = 0.0;
                for (V v_i : cluster_i) {
                    for (V v_j : cluster_j) {
                        if (graph.containsEdge(v_i, v_j))
                            count += 1.0;
                    }
                }
                E[i][j] = count / allEdges;
            }
        }

        for (int i = 0; i < A.length; i++) {
            A[i] = 0.0;
            for (int j = 0; j < A.length; j++) {
                A[i] += E[i][j];
            }
        }

        double Q = 0.0;
        for (int i = 0; i < A.length; i++) {
            Q += (E[i][i] - (A[i] * A[i]));
        }

        return Q;
    }
}
