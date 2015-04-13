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

package dk.aaue.sna.alg.centrality;

import org.jgrapht.Graph;

import java.util.*;

/**
 * Random Walk Betweenness centrality [1], based on random walks between two nodes.
 * <p>
 * Random Walk Betweenness measures how many times a vertex appears on random
 * walks between all vertex-pairs in the graph. It is an alternative measure
 * for Betweenness, see {@link dk.aaue.sna.alg.centrality.BrandesBetweennessCentrality}.
 * </p>
 * <p>
 * [1] S. P. Borgatti and M. Everett, A graph-theoretic perspective on centrality,
 *  Social Networks 28(4): 466-484, 2006.
 * </p>
 * @param <V>
 * @param <E>
 */
public class RandomWalkBetweennessCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
    private int randomWalkMaxLength;

    public RandomWalkBetweennessCentrality(Graph<V, E> graph, int randomWalkMaxLength) {
        this.graph = graph;
        this.randomWalkMaxLength = randomWalkMaxLength;
    }

	public RandomWalkBetweennessCentrality(Graph<V, E> graph) {
        this(graph, graph.vertexSet().size() * 50);
	}

	public CentralityResult<V> calculate() {

        // create random walks
        List<V> V = new ArrayList<V>(graph.vertexSet());
        GraphRandomWalk<V, E> grw = new GraphRandomWalk<V, E>(graph);

        double[] res = new double[V.size()];
        Arrays.fill(res, 0.0);

        // iterate node pairs (v_i, v_j)
        for (V v_i : V) {
            for (V v_j : V) {
                // skip v_i == v_j
                if (v_i == v_j)
                    continue;
                // create randomwalk from v_i to v_j
                List<V> randomWalk = grw.randomWalk(v_i, v_j, randomWalkMaxLength);

                // update table with nodes in this walk
                for (V v_k : randomWalk) {
                    res[V.indexOf(v_k)]++;
                }
            }
        }

        Map<V, Double> raw = new HashMap<V, Double>();
        for (int i = 0; i < V.size(); i++) {
            raw.put(V.get(i), res[i]);
        }

		return new CentralityResult<V>(raw, true);
	}
}