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

import fuzzy4j.aggregation.Aggregation;
import fuzzy4j.aggregation.AlgebraicProduct;
import fuzzy4j.aggregation.Minimum;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public interface PathCostCalculator<V, E> {

    public double getCost(Graph<V, E> graph, GraphPath<V, E> path, E newEdge);

    public static Aggregation SUM = new Aggregation() {
        @Override
        public double calc(double... doubles) {
            double s = 0.0;
            for (double v : doubles)
                s += v;
            return s;
        }
    };

    public static class Factory {
        public static Factory instance = new Factory();

        private Factory() {}

        public <V, E> PathCostCalculator<V, E> distance() {
            return new DistanceCost<V, E>();
        }
        public <V, E> PathCostCalculator<V, E> unweightedDistance() {
            return new UnweightedDistanceCost<V, E>();
        }
        public <V, E> PathCostCalculator<V, E> prod() {
            return new AggregatedCost<V, E>(AlgebraicProduct.INSTANCE);
        }
        public <V, E> PathCostCalculator<V, E> min() {
            return new AggregatedCost<V, E>(Minimum.INSTANCE);
        }
    }

    public static class UnweightedDistanceCost<V, E> implements PathCostCalculator<V,E> {
        @Override
        public double getCost(Graph<V, E> veGraph, GraphPath<V, E> veGraphPath, E newEdge) {
            return veGraphPath.getWeight() + 1.0;
        }
    }

    public static class DistanceCost<V, E> implements PathCostCalculator<V,E> {
        @Override
        public double getCost(Graph<V, E> veGraph, GraphPath<V, E> veGraphPath, E newEdge) {
            return veGraphPath.getWeight() + veGraph.getEdgeWeight(newEdge);
        }
    }

    public static class AggregatedCost<V, E> implements PathCostCalculator<V, E> {
        private Aggregation aggregation;

        public AggregatedCost(Aggregation aggregation) {
            this.aggregation = aggregation;
        }

        @Override
        public double getCost(Graph<V, E> graph, GraphPath<V, E> veGraphPath, E newEdge) {
            if (veGraphPath == null)
                return aggregation.calc(graph.getEdgeWeight(newEdge));
            else {
                double[] values = new double[veGraphPath.getEdgeList().size() + 1];
                int i = 0;
                for (E edge : veGraphPath.getEdgeList())
                    values[i++] = graph.getEdgeWeight(edge);
                values[i] = graph.getEdgeWeight(newEdge);
                return aggregation.calc(values);
            }
        }
    }
}
