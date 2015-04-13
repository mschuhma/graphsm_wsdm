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
import dk.aaue.sna.alg.DijkstraForClosuresFactory;
import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import dk.aaue.sna.alg.PathCostCalculator;
import fuzzy4j.aggregation.AlgebraicProduct;
import fuzzy4j.aggregation.weighted.WeightedAggregation;
import fuzzy4j.aggregation.weighted.WeightedValue;
import fuzzy4j.sets.FuzzyFunction;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnweightedGraph;

import java.util.*;

import static fuzzy4j.util.FuzzyUtil.asArray;

/**
 * A closeness centrality measure, with a fuzzy distance measure in the graph.
 *
 * @param <V> The node type
 * @param <E> The edge type
 * @author Soren A. Davidsen <sda@es.aau.dk></sda@es.aau.dk>
 */
public class FuzzyClosenessCentrality<V, E> implements CentralityMeasure<V> {

    private Graph<V, E> graph;
    private double diameter = 1.0;

    private FuzzyFunction pathMeasure = new FuzzyFunction() {
        @Override
        public double membership(double d) {
            if (Double.isInfinite(d))
                return 0.0;
            else
                return 1.0 / d; //- (d / diameter);
        }
    };
    private WeightedAggregation aggregation;

    public FuzzyClosenessCentrality<V, E> withPathMeasure(FuzzyFunction pathMeasure) {
        this.pathMeasure = pathMeasure;
        return this;
    }

    public FuzzyClosenessCentrality(Graph<V, E> graph, WeightedAggregation aggregation) {
        this.graph = graph;
        this.aggregation = aggregation;
    }

    public CentralityResult<V> calculate() {

        // diameter = new FloydWarshallAllShortestPaths<V, E>(new AsUnweightedGraph(graph)).getDiameter();

        System.out.println("diameter = " + diameter);

        double min_s = Double.POSITIVE_INFINITY;
        double max_s = Double.NEGATIVE_INFINITY;

        Map<V, Double> cc = new HashMap<V, Double>();

        DijkstraForClosuresFactory<V, E> sF = DijkstraForClosuresFactory.newFactory(
                DijkstraForClosures.STRONGEST_PATH,
                new PathCostCalculator.AggregatedCost<V, E>(AlgebraicProduct.INSTANCE),
                1.0
        );

        DijkstraForClosuresFactory<V, E> dF = DijkstraForClosuresFactory.newFactory(
                DijkstraForClosures.SHORTEST_PATH,
                PathCostCalculator.Factory.instance.<V, E>unweightedDistance(),
                0.0
        );

        Set<V> V = graph.vertexSet();

        for (V u : V) {

            DijkstraForClosures<V, E> maxProd = sF.create(graph, u);
            DijkstraForClosures<V, E> sp = dF.create(graph, u);

            List<WeightedValue> values = new ArrayList<WeightedValue>();
            for (V v : V) {
                // skip reflexiveness
                if (u == v)
                    continue;

                // get length of the path
                Double distance = sp.get(v);

                // infinite -> there is no path.
                if (distance == null || Double.isInfinite(distance)) {
                    continue;
                }

                double closeDistance = pathMeasure.membership(distance);

                Double closeStrengthstrength = maxProd.get(v);

                // no strength or 0.0 = no path
                if (closeStrengthstrength == null || closeStrengthstrength == 0.0)
                    continue;

                values.add(WeightedValue._(closeStrengthstrength, closeDistance));

                min_s = Math.min(min_s, closeStrengthstrength);
                max_s = Math.max(max_s, closeStrengthstrength);
            }

            System.out.println("values(" + u + ") = [" + values.size() + "] " + values);

            if (values.size() == 0)
                cc.put(u, Double.NEGATIVE_INFINITY);
            else
                cc.put(u, aggregation.calc(asArray(values)));
        }

        System.out.println("min_s, max_s = " + min_s + ", " + max_s);

        return new CentralityResult<V>(cc, true);
    }


}
