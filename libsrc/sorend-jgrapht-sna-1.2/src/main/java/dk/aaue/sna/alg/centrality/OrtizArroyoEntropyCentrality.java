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

import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import dk.aaue.sna.util.FuzzyUtil;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.WeightedGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entropy centrality [1] measures centrality of nodes depending on their contribution to the entropy of the
 * graph.
 * <p>
 * [1] Daniel Ortiz-Arroyo and D. M. Akbar Hussain, An information theory approach to identify sets of key players,
 * Intelligence and Security Informatics, LNCS 5376, pp. 15-26, 2008.
 * </p>
 * <p>
 * NOTE: This has a computational complexity of O(n^4)
 * </p>
 * <p>
 * NOTE: This is not thread-safe, the graph will be modified while algorithm is running.
 * </p>
 * <p>
 * Changes: Shortest paths to use floyd-warshall instead of dijkstra for each path. Also assume undirected
 * graphs for further speedup.
 * </p>
 *
 * @author do <do@cs.aaue.dk>
 * @author Soren A D <sdavid08@student.aau.dk>
 */
public class OrtizArroyoEntropyCentrality<V, E> implements CentralityMeasure<V> {

    private Graph<V, E> graph;

    public OrtizArroyoEntropyCentrality(Graph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public CentralityResult<V> calculate() {

        Map<V, Double> map = new HashMap<V, Double>();

        List<V> allNodes = new ArrayList<V>(graph.vertexSet());

        for (V node : allNodes) {

            // save edges of the node
            List<EdgeCopy> edges = new ArrayList<EdgeCopy>();
            for (E edge : graph.edgesOf(node)) {
                edges.add(new EdgeCopy(edge, graph.getEdgeSource(edge), graph.getEdgeTarget(edge), graph.getEdgeWeight(edge)));
            }

            // remove node
            graph.removeVertex(node);

            GraphEntropy ent = getGraphEntropySP(graph);

            if (ent == null) {
                map.put(node, 0.0);
            } else {
                map.put(node, ent.getGraphValue());
            }

            // restore graph, add vertex and edges
            graph.addVertex(node);
            for (EdgeCopy oldEdge : edges) {
                graph.addEdge(oldEdge.n1, oldEdge.n2, oldEdge.edge);
                if (graph instanceof WeightedGraph)
                    ((WeightedGraph<V, E>) graph).setEdgeWeight(oldEdge.edge, oldEdge.w);
            }
        }

        return new CentralityResult<V>(FuzzyUtil.<V>minMaxNormalize(map), false);
    }

    /**
     * calculates the probability distribution in terms of the number of shortest paths
     */
    static <V, E> GraphEntropy<V> getGraphEntropySP(Graph<V, E> graph) {

        FloydWarshallAllShortestPaths<V, E> fwPaths = new FloydWarshallAllShortestPaths<V, E>(graph, true);
        fwPaths.lazyCalculatePaths();

        // make sure we have the shortest paths
        int numSP = fwPaths.shortestPathsCount();

        // there are no shortest paths
        if (numSP <= 0)
            return null;

        GraphEntropy<V> result = new GraphEntropy<V>();

        double centralityEntropy = 0.0;

        for (V node : fwPaths.getGraph().vertexSet()) {
            List<GraphPath<V, E>> paths = fwPaths.getShortestPaths(node);
            if (paths != null && paths.size() > 0) {
                double pi = (double) paths.size() / numSP;
                result.getVertexValue().put(node, pi);
                if (pi > 0.0)
                    centralityEntropy += pi * (Math.log10(1.0 / pi) / Math.log10(2));
            } else {
                result.getVertexValue().put(node, 0.0);
            }
        }

        result.setGraphValue(centralityEntropy);

        return result;
    }

    private class EdgeCopy {
        private final E edge;
        private final V n1;
        private final V n2;
        private final double w;

        private EdgeCopy(E edge, V n1, V n2, double w) {
            this.edge = edge;
            this.n1 = n1;
            this.n2 = n2;
            this.w = w;
        }
    }

}
//