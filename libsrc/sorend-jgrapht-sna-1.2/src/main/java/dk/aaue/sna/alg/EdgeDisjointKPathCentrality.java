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
import dk.aaue.sna.alg.centrality.EdmondsKarpMaximumFlow;
import org.jgrapht.Graph;
import org.jgrapht.alg.BellmanFordShortestPath;

import java.util.*;

/**
 * Edge Disjoint K-path centrality [1] is a generalisation of degree centrality [2].
 * <p>
 * Instead of counting only neighbours reachable through 1 step, it counts neighbours
 * reachable through "k" steps.
 * </p>
 * <p>
 * [1] Stephen P. Borgatti and Martin G. Everett, <i>A Graph-theoretic perspective on centrality</i>,
 *  Social Networks 28(4), pp. 466-484, 2006.<br />
 * [2] L. C. Freeman, <i>Centrality in social networks; conceptual clarification</i>,
 *  Social Networks 1, pp. 215-239, 1979.
 * </p>
 *
 * TODO: Unfinished!!
 *
 * @param <V> Type of vertices
 * @param <E> Type of edges
 */
public class EdgeDisjointKPathCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
    private int k;

	public EdgeDisjointKPathCentrality(Graph<V, E> graph, int k) {
        this.graph = graph;
        this.k = k;
	}

    public void setK(int k) {
        this.k = k;
    }

    public CentralityResult<V> calculate() {
        Map<V, Double> r = new HashMap<V, Double>();

        List<V> V = new ArrayList<V>(graph.vertexSet());

        EdmondsKarpMaximumFlow<V, E> flow = new EdmondsKarpMaximumFlow<V, E>(graph, 1);

        for (V v_i : V) {

            BellmanFordShortestPath<V, E> bf = new BellmanFordShortestPath<V, E>(graph, v_i, k);

            int v_i_sum = 0;

            for (V v_j : V) {

                if (v_i == v_j)
                    continue;

                // get the shortest distance between v_i and v_k, we only want upto "k"
                double sp = bf.getCost(v_j);
                if (Double.isInfinite(sp) || sp > k)
                    continue;

                // calculateSubgroupCenters maximum flow between the two, and increment.
                v_i_sum += flow.maximumFlow(v_i, v_j);
            }

            // P is now all the neighbors upto k steps away.
            r.put(v_i, (double) v_i_sum);
        }

        return new CentralityResult(r, true);
	}

}