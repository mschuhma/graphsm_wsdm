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

package dk.aaue.sna.util;

import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;

/**
 * This is a helper class which wraps a graph for making it easy to
 * build graphs in the code.
 *
 * @author Soren <soren@tanesha.net>
 */
public class GraphBuilder<V, E, K> {

	private Graph<V, E> graph;
    private NodeFinder<V, K> nodeFinder = new ToStringNodeFinder<V, K>();

	public GraphBuilder(Graph<V, E> graph) {
		this.graph = graph;
	}

    public GraphBuilder<V, E, K> nodeFinder(NodeFinder<V, K> newNodeFinder) {
        this.nodeFinder = newNodeFinder;
        return this;
    }

    public GraphBuilder<V, E, K> addVertices(V... nodes) {
        for (V node : nodes)
            graph.addVertex(node);
        return this;
    }

    public GraphBuilder<V, E, K> addEdges(V... nodePairs) {

        if (nodePairs.length % 2 != 0)
            throw new IllegalArgumentException("Nodepairs must be pairs (uneven number of nodes given)");

        for (int i = 0; i < nodePairs.length; i += 2) {
            V src = nodePairs[i];
            V dst = nodePairs[i+1];
            E edge = graph.addEdge(src, dst);
        }

        return this;
    }

    public GraphBuilder<V, E, K> addWeightedEdges(NewWeightedEdge<V>... edges) {
        for (NewWeightedEdge<V> e : edges) {
            E edge = graph.addEdge(e.src, e.dst);
            ((WeightedGraph) graph).setEdgeWeight(edge, e.w);
        }
        return this;
    }

    public V node(K key) {
        // delegate to nodefinder.
        return nodeFinder.find(graph, key);
    }

    public Graph<V, E> graph() {
        return graph;
    }

    public <V, E, T extends Graph<V, E>> T graph(Class<T> clz) {
        return (T) graph;
    }

    public static <V> NewWeightedEdge<V> WE(V src, V dst, double w) {
        return new NewWeightedEdge<V>(src, dst, w);
    }

    public static class NewWeightedEdge<V> {
        private V src;
        private V dst;
        private double w;

        private NewWeightedEdge(V src, V dst, double w) {
            this.src = src;
            this.dst = dst;
            this.w = w;
        }
    }


}
