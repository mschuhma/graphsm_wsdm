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

import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.util.JGraphTUtil;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates clustering coefficient for nodes in a graph.
 *
 * @param <V> node type
 * @param <E> edge type
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class ClusteringCoefficient<V, E> implements CentralityMeasure<V> {

    /**
     * Calculates the global clustering coefficient given the output from the clustering-coefficient mesure.
     * @param clusteringCoefficients output from {@link dk.aaue.sna.alg.ClusteringCoefficient#calculate()}
     * @param <V> node type
     * @return global clustering coefficient
     */
    public static <V> double globalClusteringCoefficient(CentralityResult<V> clusteringCoefficients) {
        int n = clusteringCoefficients.getRaw().size();
        double C = 0.0;
        for (Double C_i : clusteringCoefficients.getRaw().values())
            C += C_i;
        return C / n;
    }

    private Graph<V, E> graph;

	public ClusteringCoefficient(Graph<V, E> graph) {
		this.graph = graph;
	}

    @SuppressWarnings("unused")
    public CentralityResult<V> calculate_FastUntested() {

        Map<V, Double> r = new HashMap<V, Double>();

        JGraphTUtil.AdjacencyMatrix<V> A = JGraphTUtil.adjacencyMatrix(graph);

        for (int i = 0; i < A.N; i++) {

            List<Integer> nbr = new ArrayList<Integer>();

            // find neighbours
            for (int j = 0; j < A.N; j++) {
                if (i == j)
                    continue;
                if (A.A[i][j] > 0.0)
                    nbr.add(j);
            }

            int[] nbr_i = new int[nbr.size()];
            for (int j = 0; j < nbr_i.length; j++)
                nbr_i[j] = nbr.get(j);

            int sum = 0;
            for (int p = 0; p < nbr_i.length; p++) {
                for (int q = 0; q < nbr_i.length; q++) {
                    if (p == q)
                        continue;
                    if (A.A[p][q] > 0.0)
                        sum++;
                }
            }

            double C_i = (2.0 * sum) / (nbr_i.length * (nbr_i.length - 1.0));

            r.put(A.V.get(i), C_i);
        }

        return new CentralityResult<V>(r, true);
    }

    public CentralityResult<V> calculate() {

        Map<V, Double> r = new HashMap<V, Double>();

        for (V n : graph.vertexSet()) {

            List<V> nbr = Graphs.neighborListOf(graph, n);
            int nbr_n = nbr.size();
            int sum_e_jk = 0;

            if (nbr.size() < 2) {
                r.put(n, 0.0);
                continue;
            }

            for (int i = 0; i < nbr_n; i++) {
                for (int j = i + 1; j < nbr_n; j++) {
                    if (graph.containsEdge(nbr.get(i), nbr.get(j)))
                        sum_e_jk++;
                }
            }

            double C_i = (2.0 * sum_e_jk) / (nbr_n * (nbr_n - 1.0));

            r.put(n, C_i);
        }

        return new CentralityResult<V>(r, true);
    }

}