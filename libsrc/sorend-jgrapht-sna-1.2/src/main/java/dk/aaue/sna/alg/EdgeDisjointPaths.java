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

import dk.aaue.sna.util.JGraphTUtil;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO: Unfinished!!
 *
 * @author Soren <soren@tanesha.net>
 */
public class EdgeDisjointPaths<V, E> {

    private Graph<V, E> graph;
    private int k;
    private double maxLength;
    private List<V> V;

    public EdgeDisjointPaths(Graph<V, E> graph, int k) {
        this(graph, k, -1.0);
    }

    public EdgeDisjointPaths(Graph<V, E> graph, int k, double maxLength) {
        this.graph = graph;
        this.k = k;
        this.maxLength = maxLength;
        this.V = new ArrayList<V>(graph.vertexSet());
    }


    public Map<V, List<GraphPath<V, E>>> calculate(V source) {

        Map<V, List<GraphPath<V, E>>> r = new HashMap<V, List<GraphPath<V, E>>>();

        for (V sink : V) {
            if (source == sink)
                continue;
            r.put(sink, calculatePair(source, sink));
        }

        return r;
    }

    public List<GraphPath<V, E>> calculatePair(V source, V sink) {

        List<GraphPath<V, E>> paths = new ArrayList<GraphPath<V, E>>();

        Graph<V, E> cloned = JGraphTUtil.clone(graph);

        for (int i = 0; i < k; i++) {

            DijkstraShortestPath<V, E> dAlg = null;
            if (maxLength > 0.0) {
                dAlg = new DijkstraShortestPath<V, E>(cloned, source, sink, maxLength);
            }
            else {
                dAlg = new DijkstraShortestPath<V, E>(cloned, source, sink);
            }

            GraphPath<V, E> sp = dAlg.getPath();

            if (sp == null)
                break;

            // save this path
            paths.add(sp);

            // remove the edges in this path
            for (E e : sp.getEdgeList())
                cloned.removeEdge(e);
        }

        return paths;
    }


}
