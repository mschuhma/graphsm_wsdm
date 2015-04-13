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

import fuzzy4j.aggregation.weighted.AIWA;
import fuzzy4j.aggregation.weighted.WeightedAggregation;
import fuzzy4j.aggregation.weighted.WeightedValue;
import fuzzy4j.sets.*;
import org.jgrapht.Graph;

import java.util.*;

/**
 * @author Soren <soren@tanesha.net>
 */
public class InfluenceIndependenceCentrality<V, E> implements CentralityMeasure<V> {

    private Graph<V, E> graph;
    private double influence;
    private WeightedAggregation aggregation;

    public InfluenceIndependenceCentrality(Graph<V, E> graph, double influence, WeightedAggregation aggregation) {
        this.graph = graph;
        this.influence = influence;
        this.aggregation = aggregation;
    }

    private Map<V, Double> merge(WeightedAggregation waggr, double[] weights, CentralityResult<V>... results) {

        Set<V> nodes = new LinkedHashSet<V>();
        for (CentralityResult r : results)
            nodes.addAll(r.getRaw().keySet());

        Map<V, Double> map = new LinkedHashMap<V, Double>();
        for (V node : nodes) {
            WeightedValue[] values = new WeightedValue[results.length];
            for (int i = 0; i < values.length; i++)
                values[i] = WeightedValue._(weights[i], results[i].get(node));
            map.put(node, waggr.calc(values));
        }

        return map;
    }

    @Override
    public CentralityResult<V> calculate() {

        double[] weights = new double[] {
                influence,
                influence,
                1.0 - influence,
                1.0 - influence
        };

        Map<V, Double> merged = merge(aggregation, weights,
                new DegreeCentrality<V, E>(graph).calculate(),
                new FreemanClosenessCentrality<V, E>(graph).calculate(),
                new BrandesBetweennessCentrality<V, E>(graph).calculate(),
                new EigenvectorCentrality<V, E>(graph).calculate()
        );

        return new CentralityResult<V>(merged, false);
    }
}
