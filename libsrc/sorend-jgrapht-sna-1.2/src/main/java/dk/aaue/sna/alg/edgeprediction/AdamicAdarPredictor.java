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

import java.util.List;
import java.util.Set;

/**
 * Implements the Adamic/Adar edge prediction measure.
 *
 *
 * @author Soren A D <sdavid08@student.aau.dk>
 */
public class AdamicAdarPredictor<V, E> implements EdgePredictor<V, E> {

    private EdgePrediction<V> adamicAdarPrediction(Graph<V, E> G, V v, V u, List<V> C) {
        double s = 0.0;
        for (V z : C) {
            s += 1.0 / Math.log(G.edgesOf(z).size());
        }
        return new EdgePrediction<V>(v, u, s);
    }

    @Override
    public Set<EdgePrediction<V>> predict(Graph<V, E> G, int max) {

        SortedMaxCapacityTreeSet<EdgePrediction<V>> s = new SortedMaxCapacityTreeSet<EdgePrediction<V>>(max, new EdgePrediction.ReverseEdgePredictoinComparator<V>());

        for (V v : G.vertexSet()) {
            for (V u : G.vertexSet()) {
                if (v == u)
                    continue;
                List<V> C = CommonNeighboursPredictor.commonNeighbours(G, v, u);
                s.add(adamicAdarPrediction(G, v, u, C));
            }
        }

        return s.theSet();
    }
}
