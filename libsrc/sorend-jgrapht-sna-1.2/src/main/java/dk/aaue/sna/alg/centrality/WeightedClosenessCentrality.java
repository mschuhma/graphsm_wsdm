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

import dk.aaue.sna.alg.DijkstraForClosures;
import dk.aaue.sna.alg.PathCostCalculator;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This implements a closeness centrality for weighted networks, as proposed in [1]. This algorithm assumes that weights
 * are positive. a weight 0 means absense of edge, and a weight of 10 is twice as strong as a weight of 5.
 * <p>
 * See {@link WeightedClosenessCentrality#setAlpha(double)} to control the alpha parameter (default 1.0).
 * For {@code 0 < alpha < 1}, the number of edges are penalized and for {@code alpha > 1} the number of edges are
 * favorized.
 * </p>
 * [1] Opsahl, Tore and Agneessens, Filip and Skvoretz, John. Node centrality in weighted networks: Generalizing
 * degree and shortest paths. In Social Networks 33(3): pp. 245-251, doi:10.1016/j.socnet.2010.03.006, 2010.
 *
 * @param <V> Node type
 * @param <E> Edge type
 *  @author Soren A. Davidsen <soren@tanesha.net>
 */
public class WeightedClosenessCentrality<V, E> implements CentralityMeasure<V> {

    private WeightedGraph<V, E> graph;
    private double alpha = 1.0;

    public WeightedClosenessCentrality(WeightedGraph<V, E> graph) {
        this.graph = graph;
    }

    /**
     * Set the alpha parameter. Controls how much weights counts. For 0 = no value to weight, 1 = use weight's value,
     * > 1 weight has more value.
     * @param alpha see description
     */
    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    private class WeightedPathCost implements PathCostCalculator<V, E> {
        @Override
        public double getCost(Graph<V, E> veGraph, GraphPath<V, E> veGraphPath, E newEdge) {
            return veGraphPath.getWeight() + Math.pow(1.0 / graph.getEdgeWeight(newEdge), alpha);
        }
    }

    public CentralityResult<V> calculate() {

        Map<V, Double> cc = new HashMap<V, Double>();

        Set<V> V = graph.vertexSet();

        for (V n : V) {

            DijkstraForClosures<V, E> sp = new DijkstraForClosures<V, E>(
                    graph,
                    DijkstraForClosures.SHORTEST_PATH,
                    new WeightedPathCost(),
                    0.0,
                    n
            );

            System.out.println("sp = " + sp);

            double sum = 0.0;
            double s = 0.0;
            for (V p : V) {
                // skip reflexiveness
                if (n == p)
                    continue;

                // get length of the path
                Double length = sp.get(p);

                // infinite -> there is no path.
                if (length == null || Double.isInfinite(length)) {
                    continue;
                }

                sum += length;
                s++;
            }

            if (sum == 0.0)
                cc.put(n, Double.NEGATIVE_INFINITY);
            else
                cc.put(n, s / sum);
        }

        return new CentralityResult<V>(cc, true);
    }

}
