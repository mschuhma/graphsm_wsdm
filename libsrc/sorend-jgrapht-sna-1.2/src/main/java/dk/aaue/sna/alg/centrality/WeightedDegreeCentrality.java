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

import dk.aaue.sna.alg.EdgesOf;
import org.jgrapht.WeightedGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * Degree centrality for weighted graphs. Implements the measure from [1].
 * <p>
 * Use {@link WeightedDegreeCentrality#setAlpha(double)} to control the alpha parameter.
 * </p>
 * <p/>
 * Use {@link WeightedDegreeCentrality#setUseOutgoing(boolean)} to control if we want D_out (default) or D_in in the
 * case of a directed graph.
 * <p/>
 * [1] Opsahl, Tore and Agneessens, Filip and Skvoretz, John. Node centrality in weighted networks: Generalizing
 * degree and shortest paths. In Social Networks 33(3): pp. 245-251, doi:10.1016/j.socnet.2010.03.006, 2010.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class WeightedDegreeCentrality<V, E> implements CentralityMeasure<V> {

    private WeightedGraph<V, E> graph;
    private double alpha = 1.0;
    private boolean useOutgoing = true;

    public WeightedDegreeCentrality(WeightedGraph<V, E> graph) {
        this.graph = graph;
    }

    /**
     * Controls the alpha value (default value is 1.0)
     *
     * @param alpha see class decsription
     * @return this
     */
    public WeightedDegreeCentrality<V, E> setAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * Configure useOutgoing or indegree in case we're calculating on a directed network. (default is true = use outgoing)
     *
     * @param useOutgoing true = use outgoing edges, false = use incoming edges.
     * @return this
     */
    public WeightedDegreeCentrality<V, E> setUseOutgoing(boolean useOutgoing) {
        this.useOutgoing = useOutgoing;
        return this;
    }

    @Override
    public CentralityResult<V> calculate() {

        EdgesOf<V, E> edgesOf = EdgesOf.Factory.instance.create(graph, useOutgoing);

        Map<V, Double> r = new HashMap<V, Double>();
        for (V u : graph.vertexSet()) {
            double s_u = 0.0;
            double k_u = 0.0;
            for (E edge : edgesOf.edgesOf(u)) {
                k_u++;
                s_u += graph.getEdgeWeight(edge);
            }
            r.put(u, Math.pow(k_u, (1.0 - alpha)) * Math.pow(s_u, alpha));
        }

        return new CentralityResult<V>(r, false);
    }
}
