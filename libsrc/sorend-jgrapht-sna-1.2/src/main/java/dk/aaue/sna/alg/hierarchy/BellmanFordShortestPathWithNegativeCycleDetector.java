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

package dk.aaue.sna.alg.hierarchy;

import org.jgrapht.Graph;

import java.util.*;

/**
 * A slightly modified version of the bellman ford algorithm, implemented to detect negative cycles, by adding an additional step.
 * (Note, this is implemented from scratch, not by modifying JGraphT's BellmanFordShortestPath)
 * For information on the algorithm, see {@linkplain <a href="http://en.wikipedia.org/wiki/Bellman%E2%80%93Ford_algorithm">Bellman-Ford on Wikipedia</a>}.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class BellmanFordShortestPathWithNegativeCycleDetector<V, E> {

    private Graph<V, E> graph;
    private V source;

    private Map<V, Double> distance = null;
    private Map<V, V> predecessor = null;
    private boolean negativeCycle = false;
    private List<E> negativeCycleVertices = null;

    /**
     * Constructor.
     *
     * @param graph The graph
     * @param source The source
     */
    public BellmanFordShortestPathWithNegativeCycleDetector(Graph<V, E> graph, V source) {
        this.graph = graph;
        this.source = source;
    }

    protected void lazyCalculate() {

        if (distance != null)
            return;

        distance = new HashMap<V, Double>();
        predecessor = new HashMap<V, V>();

        // initialize
        for (V v : graph.vertexSet()) {
            if (v.equals(source))
                distance.put(v, 0.0);
            else
                distance.put(v, Double.POSITIVE_INFINITY);
        }

        // relax n-1 times.
        int n = graph.vertexSet().size();
        for (int i = 0; i < n - 1; i++) {
            for (E e : graph.edgeSet()) {
                V u = graph.getEdgeSource(e);
                V v = graph.getEdgeTarget(e);
                double weight = graph.getEdgeWeight(e);
                if (distance.get(u) + weight < distance.get(v)) {
                    distance.put(v, distance.get(u) + weight);
                    if (predecessor != null)
                        predecessor.put(v ,u);
                }
            }
        }

        // check Vth time if we can relax any more (=negative cycle detected)
        for (E e : graph.edgeSet()) {
            V u = graph.getEdgeSource(e);
            V v = graph.getEdgeTarget(e);
            double weight = graph.getEdgeWeight(e);
            if (distance.get(u) + weight < distance.get(v)) {
                negativeCycle = true;
                negativeCycleVertices = constructPath(v, v);
                break;
            }
        }
    }

    private List<E> constructPath(V start, V end) {

        System.out.println("cost start=" + start + ", end=" + end);

        // System.out.println("Constructing path: start=" + start + ", end=" + end);
        List<E> R = new ArrayList<E>();

        V cur = end;
        while (true) {
            R.add(graph.getEdge(predecessor.get(cur), cur));

            cur = predecessor.get(cur);
            if (cur.equals(start))
                break;
        }

        return R;
    }

    /**
     * Returns the cost of the path from source to sink (destination).
     * @param sink The destination
     * @return the cost
     */
    public double getCost(V sink) {
        lazyCalculate();
        return distance.get(sink);
    }

    /**
     * Evaluates if the graph contains a negative cycle.
     * @return
     */
    public boolean hasNegativeCycle() {
        lazyCalculate();
        return negativeCycle;
    }

    /**
     * Returns the edges of the negative cycle (use {@link dk.aaue.sna.alg.hierarchy.BellmanFordShortestPathWithNegativeCycleDetector#hasNegativeCycle()} first.
     * @return
     */
    public List<E> getNegativeCycleVertices() {
        return negativeCycleVertices;
    }

}
