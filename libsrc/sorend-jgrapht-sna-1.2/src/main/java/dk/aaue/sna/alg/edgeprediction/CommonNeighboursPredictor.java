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

package dk.aaue.sna.alg.edgeprediction;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.BellmanFordShortestPath;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Performs random prediction using a uniform random distribution given by "Random"
 *
 * @author Soren A D <sdavid08@student.aau.dk>
 */
public class CommonNeighboursPredictor<V, E> implements EdgePredictor<V, E> {

    private static Logger LOG = Logger.getLogger(CommonNeighboursPredictor.class.getName());

    public static <V, E> void allCommonNeighbours(Graph<V, E> graph) {

        int count = 0;
        for (V v : graph.vertexSet()) {
            BellmanFordShortestPath<V, E> bfAlg = new BellmanFordShortestPath<V, E>(graph, v, 2);
            for (V u : graph.vertexSet()) {
                if (v.equals(u))
                    continue;
                if (graph.containsEdge(v, u))
                    continue;
                if (bfAlg.getCost(u) > 2.0)
                    continue;
                count += commonNeighbours(graph, v, u).size();
            }
        }
    }

    public static <V, E> List<V> commonNeighbours(Graph<V, E> graph, V n1, V n2) {
        List<V> nbr_s = Graphs.neighborListOf(graph, n1);
        nbr_s.retainAll(Graphs.neighborListOf(graph, n2));
        return nbr_s;
    }

    @Override
    public Set<EdgePrediction<V>> predict(Graph<V, E> G, int num) {

        SortedMaxCapacityTreeSet<EdgePrediction<V>> s = new SortedMaxCapacityTreeSet<EdgePrediction<V>>(num, new EdgePrediction.ReverseEdgePredictoinComparator<V>());

        for (V v : G.vertexSet()) {
            for (V u : G.vertexSet()) {
                List<V> C = CommonNeighboursPredictor.commonNeighbours(G, v, u);
                s.add(new EdgePrediction<V>(v, u, C.size()));
            }
        }

        return s.theSet();
    }
}