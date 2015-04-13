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

import dk.aaue.sna.alg.VertexPair;
import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wraps an edge predictor as a centrality result for vertex pairs.
 *
 * @author Soren <soren@tanesha.net>
 * @param <V> node type
 * @param <E> edge type
 */
public class EdgePredictorCentralityWrapper<V, E> implements CentralityMeasure<VertexPair<V>> {

    private EdgePredictor<V, E> inner;
    private int max;
    private Graph<V, E> graph;

    public EdgePredictorCentralityWrapper(EdgePredictor<V, E> inner, int max, Graph<V, E> graph) {
        this.inner = inner;
        this.max = max;
        this.graph = graph;
    }

    @Override
    public CentralityResult<VertexPair<V>> calculate() {

        Set<EdgePrediction<V>> predictions = inner.predict(graph, max);

        // System.out.println("predictions(" + max + ", " + predictions.size() + ") = " + predictions);

        Map<VertexPair<V>, Double> raw = new HashMap<VertexPair<V>, Double>();
        for (EdgePrediction<V> prediction : predictions) {
            raw.put(new VertexPair<V>(prediction.u, prediction.v), prediction.val);
        }

        return new CentralityResult<VertexPair<V>>(raw, false);
    }
}
