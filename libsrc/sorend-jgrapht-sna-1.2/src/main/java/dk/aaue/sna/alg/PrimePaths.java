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

import org.jgrapht.DirectedGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Find all prime paths[1] in a graph.
 *
 * 1) A prime path is a simple path which is not a proper subpath of another simple path in the graph.
 * 2) A path is simple if no node appears more than once, with the exception of the start and end nodes.
 *
 * [1] Introduction to Software Testing, P. Ammann and J. Offutt, Cambridge Univ. Press, 2008.
 *
 * @author Soren A. D. <sda@es.aau.dk>
 */
public class PrimePaths<V, E> {

    private DirectedGraph<V, E> graph;

    public PrimePaths(DirectedGraph<V, E> graph) {
        this.graph = graph;
    }

    public List<SimplePath<V>> calculate() {

        // Q is the queue of "in process" paths
        LinkedList<SimplePath<V>> Q = new LinkedList<SimplePath<V>>();
        // F is the found simple paths
        LinkedList<SimplePath<V>> F = new LinkedList<SimplePath<V>>();

        // initialize queue
        for (V v : graph.vertexSet()) {
            Q.add(new SimplePath<V>(v));
        }

        while (!Q.isEmpty()) {

            SimplePath<V> p = Q.removeFirst();

            V last = p.last();
            if (graph.outDegreeOf(last) == 0) {
                F.add(p);
                continue;
            }

            for (E outgoing : graph.outgoingEdgesOf(p.last())) {
                V target = graph.getEdgeTarget(outgoing);
                SimplePath<V> np = p.add(target);
                if (p.has(target)) {
                    if (p.start().equals(target))
                        F.add(np);
                    else
                        F.add(p);
                }
                else {
                    Q.add(np);
                }
            }
        }

        // prune sub-paths
        List<SimplePath<V>> prune = new ArrayList<SimplePath<V>>();
        for (SimplePath<V> p : F) {
            for (SimplePath<V> q : F) {
                if (p == q)
                    continue;
                if (p.subpathOf(q))
                    prune.add(p);
            }
        }
        for (SimplePath<V> p : prune)
            F.remove(p);

        return F;
    }

    public static class SimplePath<V> {
        private ArrayList<V> nodes = new ArrayList<V>();

        public SimplePath() {
        }

        public SimplePath(V start) {
            nodes.add(start);
        }

        public V start() {
            return nodes.get(0);
        }

        public boolean has(V node) {
            return nodes.contains(node);
        }

        public List<V> nodes() {
            return nodes;
        }

        /**
         * Determine if <code>this</code> path is a subpath of <code>other</code>.
         * @param other The (possible) super-path
         * @return
         */
        public boolean subpathOf(SimplePath<V> other) {
            if (this.nodes.size() > other.nodes.size())
                return false;
            int iter = other.nodes.size() - this.nodes.size() + 1;
            for (int i = 0; i < iter; i++) {
                boolean match = true;
                for (int j = 0; j < this.nodes.size(); j++) {
                    if (!this.nodes.get(j).equals(other.nodes.get(i+j))) {
                        match = false;
                        break;
                    }
                }
                if (match)
                    return true;
            }
            return false;
        }

        public SimplePath<V> add(V node) {
            SimplePath<V> added = new SimplePath<V>();
            added.nodes.addAll(this.nodes);
            added.nodes.add(node);
            return added;
        }

        public V last() {
            return nodes.get(nodes.size() - 1);
        }

        @Override
        public String toString() {
            return "SimplePath{" +
                    "nodes=" + nodes +
                    '}';
        }
    }

}
