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

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of degree centrality [1].
 * <p>
 * [1] Freeman, Linton, A set of measures of centrality based upon betweenness, Sociometry 40: 35–41, 1977.
 * </p>
 *
 * @author Soren A D <sdavid08@student.aau.dk>
 * @param <V> the node type
 * @param <E> the edge type
 */
public class DegreeCentrality<V, E> implements CentralityMeasure<V> {

    Graph<V, E> graph;

    public DegreeCentrality(Graph<V, E> graph) {
        this.graph = graph;
    }

    public CentralityResult<V> calculate() {

        Map<V, Double> r = new HashMap<V, Double>();

        int n_1 = graph.vertexSet().size() - 1;
        for (V n : graph.vertexSet()) {
            double sum = 0.0;
            for (E e : graph.edgesOf(n)) {
                sum += graph.getEdgeWeight(e);
            }
            r.put(n, sum / n_1);
        }

        return new CentralityResult<V>(r, true);
    }

}
