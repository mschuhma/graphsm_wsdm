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
import org.jgrapht.Graph;

import java.util.Set;

/**
 * Wrapper for getting edges of respectively directed or undirected networks. Defaults to outgoing edges of
 * directed networks.
 *
 * @author Soren <soren@tanesha.net>
 */
public interface EdgesOf<V, E> {

    public static class Factory {
        public static final Factory instance = new Factory();

        private Factory() {}

        /**
         * Construct EdgesOf helper for a graph. Will use outgoing edges in case of a directed graph.
         * @param graph The graph
         * @param <V> Node type
         * @param <E> Edge type
         * @return EdgesOf helper
         */
        public <V, E> EdgesOf<V, E> create(Graph<V, E> graph) {
            return create(graph, true);
        }

        /**
         * Construct EdgesOf helper for a graph.
         * @param graph The graph
         * @param outgoingInDirected Decide if outgoing edges should be used in a directed graph (true=outgoing, false=incoming)
         * @param <V> Node type
         * @param <E> Edge type
         * @return EdgesOf helper
         */
        public <V, E> EdgesOf<V, E> create(Graph<V, E> graph, boolean outgoingInDirected) {
            if (graph instanceof DirectedGraph)
                return new DirectedEdgesOf<V, E>((DirectedGraph) graph, outgoingInDirected);
            else
                return new UndirectedEdgesOf<V, E>(graph);
        }
    }

    /**
     * Returns edges of a node.
     * @param v
     * @return
     */
    public Set<E> edgesOf(V v);

    static class UndirectedEdgesOf<V, E> implements EdgesOf<V, E> {
        private Graph<V, E> graph;
        public UndirectedEdgesOf(Graph<V, E> graph) {
            this.graph = graph;
        }
        @Override
        public Set<E> edgesOf(V v) {
            return graph.edgesOf(v);
        }
    }

    static class DirectedEdgesOf<V, E> implements EdgesOf<V, E> {
        private DirectedGraph<V, E> graph;
        private boolean outgoing;
        public DirectedEdgesOf(DirectedGraph<V, E> graph, boolean outgoing) {
            this.graph = graph;
            this.outgoing = outgoing;
        }
        @Override
        public Set<E> edgesOf(V v) {
            if (outgoing)
                return graph.outgoingEdgesOf(v);
            else
                return graph.incomingEdgesOf(v);
        }
    }
}

