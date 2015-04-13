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
import org.jgrapht.Graphs;
import org.jgrapht.event.TraversalListenerAdapter;
import org.jgrapht.traverse.BreadthFirstIterator;

import java.util.*;

/**
 * TODO: Not finished!
 *
 * @author Soren <soren@tanesha.net>
 */
public class AllPaths<V, E> {

    private Graph<V,E> graph;

    public AllPaths(Graph<V, E> graph) {
        this.graph = graph;
    }

    // use bfs to find all paths
    private List<List<V>> findAllPaths(V start, V end, List<V> path) {

        Queue<V> Q = new LinkedList<V>();
        Set<V> marked = new HashSet<V>();

        Q.add(start);
        marked.add(start);

        while (!Q.isEmpty()) {
            V v = Q.remove();
            for (E adj : graph.edgesOf(v)) {
                V w = Graphs.getOppositeVertex(graph, adj, v);
                if (w.equals(end)) {
                    // find the marked nodes.
                }
                if (!marked.contains(w)) {
                    marked.add(w);
                    Q.add(w);
                }
            }
        }

        path = new ArrayList<V>(path);
        path.add(start);
        List<List<V>> paths = new ArrayList<List<V>>();
        if (start == end) {
            paths.add(path);
            return paths;
        }
        if (!graph.vertexSet().contains(start)) {
            return paths;
        }
        for (V node : Graphs.neighborListOf(graph, start)) {
            if (!path.contains(node)) {
                List<List<V>> newpaths = findAllPaths(node, end, path);
                paths.addAll(newpaths);
            }
        }
        return paths;
    }


    public List<List<V>> calculate(V start, V end) {
        List<List<V>> paths = findAllPaths(start, end, new ArrayList<V>());
        return paths;
    }
}
