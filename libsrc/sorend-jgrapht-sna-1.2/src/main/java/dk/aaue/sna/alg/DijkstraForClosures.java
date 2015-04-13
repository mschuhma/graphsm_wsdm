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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphPathImpl;

import java.util.*;

/**
 * Naive implementation of Dijkstra, which will be used for computing the closure of a graph.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class DijkstraForClosures<V, E> {

    /**
     * Selects path which is strongest (i.e. highest value)
     */
    public static Comparator<Double> STRONGEST_PATH = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return Double.compare(o2, o1);
        }
    };

    /**
     * Selects path which is shortest (i.e. lowest value)
     */
    public static Comparator<Double> SHORTEST_PATH = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return Double.compare(o1, o2);
        }
    };

    private Graph<V, E> graph;
    private V source;
    private Comparator<Double> costComparator;
    private PathCostCalculator<V, E> pathCostCalculator;
    private double initialValue;

    private Map<V, GraphPath<V, E>> P = null;
    private EdgesOf<V, E> edgesOf;

    public DijkstraForClosures(Graph<V, E> graph, Comparator<Double> costComparator, PathCostCalculator<V, E> pathCostCalculator, double initialValue, V source) {
        this.graph = graph;
        this.source = source;
        this.costComparator = costComparator;
        this.pathCostCalculator = pathCostCalculator;
        this.initialValue = initialValue;
        this.edgesOf = EdgesOf.Factory.instance.create(graph); // edgesof helper.
        calculate();
    }

    private void calculate() {

        // lazy not needed.
        if (P != null)
            return;

        P = new HashMap<V, GraphPath<V, E>>();

        // init priority queue
        PriorityQueue<V> Q = new PriorityQueue<V>(1, new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                return costComparator.compare(P.get(o1).getWeight(), P.get(o2).getWeight());
            }
        });

        // Settled nodes
        Set<V> S = new HashSet<V>();

        // initialize
        P.put(source, new GraphPathImpl<V, E>(graph, source, source, new ArrayList<E>(), initialValue));
        Q.add(source);

        //
        while (!Q.isEmpty()) {
            // get from queue (this is the minimum value we extract)
            V u = Q.poll();

            // add settled
            S.add(u);

            // get neighbors
            Set<E> edges = this.edgesOf.edgesOf(u);

            GraphPath<V, E> pathU = P.get(u);

            // relax neighbors
            for (E edge : edges) {
                V v = Graphs.getOppositeVertex(graph, edge, u);

                // already settled
                if (S.contains(v))
                    continue;

                // double edgeWeight = graph.getEdgeWeight(edge);

                // cost through U to V
                double distanceThroughU = pathCostCalculator.getCost(graph, pathU, edge);
                // double distanceThroughU = costAccumulator.addCosts(d.get(u), edgeWeight);

                // System.out.println(pathU + " + " + edge + " = " + distanceThroughU);

                int compared;
                if (!P.containsKey(v)) { // yet unseen node.
                    compared = -1;
                } else {
                    double distanceNow = P.containsKey(v) ? P.get(v).getWeight() : initialValue;
                    compared = costComparator.compare(distanceThroughU, distanceNow);
                }

                // distanceThroughU < d.get(v)
                if (compared < 0) {
                    Q.remove(v);
                    P.put(v, pathThroughU(pathU, source, v, edge, distanceThroughU));
                    Q.add(v);
                }
            }
        }
    }

    private GraphPath<V, E> pathThroughU(GraphPath<V, E> path, V src, V newDst, E newEdge, double newCost) {
        List<E> newList = new ArrayList<E>(path.getEdgeList());
        newList.add(newEdge);
        return new GraphPathImpl<V, E>(this.graph, src, newDst, newList, newCost);
    }

    /**
     * Get the distance to target, or null if no distance.
     *
     * @param target The target
     * @return the value or null
     */
    public Double get(V target) {
        return P.containsKey(target) ? P.get(target).getWeight() : null;
    }

    /**
     * Get the path representing "shortest path" to target, null if no found path.
     *
     * @param target The target
     * @return The path or null
     */
    public GraphPath<V, E> getPath(V target) {
        return P.get(target);
    }

    public String toString() {
        Map<V, String> buf = new HashMap<V, String>();
        for (Map.Entry<V, GraphPath<V, E>> e : P.entrySet())
            buf.put(e.getKey(), e.getValue().toString() + "/" + e.getValue().getWeight());
        return "Dijkstra(" + source + ") = " + buf;
    }

}
