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

import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.AsWeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.Subgraph;

import java.util.*;
import java.util.logging.Logger;

/**
 * Implementation of [1], which seeks to detect hierarchy by minimizing social agony in the network.
 * <p>
 * The algorithm consists of two parts:
 * <ol>
 *    <li>Create hierarchy levels for the nodes, see {@link dk.aaue.sna.alg.hierarchy.AgonyMinimizingHierarchy#calculate()}.</li>
 *    <li>Create a weighted mapping of the graph where edge weights corresponds to agony in the network, see {@link dk.aaue.sna.alg.hierarchy.AgonyMinimizingHierarchy#calculateAgony()}.</li>
 * </ol>
 * </p>
 * <p>
 * [1] M. Gupte et al., Finding Hierarchy in Directed Online Social Networks, In Proc. of WWW 2011, 2011 (to Appear).
 * </p>
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class AgonyMinimizingHierarchy<V, E> implements CentralityMeasure<V> {

    private Logger LOG = Logger.getLogger(AgonyMinimizingHierarchy.class.getName());
    private DirectedGraph<V, E> graph;

    /**
     * Constructor
     * @param graph The graph to work on. Notice requires a directed graph.
     */
    public AgonyMinimizingHierarchy(DirectedGraph<V, E> graph) {
        this.graph = graph;
    }

    public CentralityResult<V> calculate() {

        LOG.info("1/2. Calculating max eulerian subgraph .. ");
        DefaultDirectedWeightedGraph<V, E> marked = calculateMaxEulerianSubgraph(graph);

        LOG.info("2/2. Calculating agony labels .. ");
        Map<V, Double> agonyLabels = calculateAgonyLabels(marked);

        return new CentralityResult<V>(agonyLabels, false);
    }

    public AsWeightedGraph<V, E> calculateAgony() {

        //LOG.info("1/4. Calculating max eulerian subgraph .. ");
        DefaultDirectedWeightedGraph<V, E> marked = calculateMaxEulerianSubgraph(graph);

        //LOG.info("2/4. Creating subgraphs on original graph .. ");
        Subgraph[] dag_h = createSubgraphs(marked, graph);

        Subgraph<V, E, DefaultDirectedWeightedGraph<V, E>> DAG = dag_h[0];
        Subgraph<V, E, DefaultDirectedWeightedGraph<V, E>> H = dag_h[1];

        //LOG.info("3/4. Calculating agony labels .. ");
        Map<V, Double> agonyLabels = calculateAgonyLabels(marked);

        //LOG.info("4/4. Calculating agony on edges .. ");
        Map<E, Double> edgeWeights = new HashMap<E, Double>();

        for (E edge : DAG.edgeSet()) {
            edgeWeights.put(edge, 0.0);
        }
        for (E edge : H.edgeSet()) {
            V u = graph.getEdgeSource(edge);
            V v = graph.getEdgeTarget(edge);
            double agony = agonyLabels.get(u) - agonyLabels.get(v) + 1.0;
            edgeWeights.put(edge, agony);
        }

        AsWeightedGraph<V, E> weighted = new AsWeightedGraph<V, E>(graph, edgeWeights);

        return weighted;
    }

    /**
     * Second step, calculate lables from the marked graph (Algorithm 2 in paper)
     * @param marked The marked graph.
     * @return
     */
    protected static <V, E> Map<V, Double> calculateAgonyLabels(DefaultDirectedWeightedGraph<V, E> marked) {

        Map<V, Double> labels = new HashMap<V, Double>();

        for (V v : marked.vertexSet())
            labels.put(v, 0.0);

        boolean found = true;

        while (found) {

            found = false;

            for (E e : marked.edgeSet()) {
                V u = marked.getEdgeSource(e);
                V v = marked.getEdgeTarget(e);
                double w = marked.getEdgeWeight(e);

                if (labels.get(v) < labels.get(u) - w) {
                    labels.put(v, labels.get(u) - w);
                    found = true;
                }
            }
        }

        return labels;
    }

    /**
     * First step: Calculate eulerian subgraph and DAG. (Algorithm 1 in paper)
     */
    protected static <V, E> DefaultDirectedWeightedGraph<V, E> calculateMaxEulerianSubgraph(DirectedGraph<V, E> graph) {

        DefaultDirectedWeightedGraph<V, E> clone = new DefaultDirectedWeightedGraph<V, E>(graph.getEdgeFactory());

        Graphs.addAllVertices(clone, graph.vertexSet());
        for (E e : graph.edgeSet()) {
            V src = graph.getEdgeSource(e);
            V dst = graph.getEdgeTarget(e);
            clone.addEdge(src, dst);
        }

        // 1. set weight of all edges to -1
        for (E e : clone.edgeSet()) {
            clone.setEdgeWeight(e, -1.0);
        }

        while (true) {

            V firstNode = clone.vertexSet().iterator().next();
            System.out.println("iterating ... ");
            BellmanFordShortestPathWithNegativeCycleDetector<V, E> cd = new BellmanFordShortestPathWithNegativeCycleDetector<V, E>(clone, firstNode);

            if (!cd.hasNegativeCycle())
                break;

            for (E e : cd.getNegativeCycleVertices()) {
                double w = clone.getEdgeWeight(e);
                clone.setEdgeWeight(e, -w);
            }
        }

        return clone;

    }

    /**
     * Helper method to produce the subgraphs DAG and H. They are required for putting agony on the edges.
     * @param markedGraph The marked graph
     * @param graph The original graph
     * @return
     */
    protected static <V, E> Subgraph<V, E, DirectedGraph<V, E>>[] createSubgraphs(DefaultDirectedWeightedGraph<V, E> markedGraph, DirectedGraph<V, E> graph) {
        // construct V and E for DAG and H
        Set<V> DAG_V = new HashSet<V>();
        Set<V> H_V = new HashSet<V>();
        Set<E> DAG_E = new HashSet<E>();
        Set<E> H_E = new HashSet<E>();
        for (E e : markedGraph.edgeSet()) {
            V source = markedGraph.getEdgeSource(e);
            V target = markedGraph.getEdgeTarget(e);
            double w = markedGraph.getEdgeWeight(e);

            if (w == 1.0) {
                H_V.add(source);
                H_V.add(target);
                H_E.add(graph.getEdge(source, target));
            }
            else {
                DAG_V.add(source);
                DAG_V.add(target);
                DAG_E.add(graph.getEdge(source, target));
            }
        }
        Subgraph<V, E, DirectedGraph<V, E>> DAG = new Subgraph<V, E, DirectedGraph<V, E>>(graph, DAG_V, DAG_E);
        Subgraph<V, E, DirectedGraph<V, E>> H = new Subgraph<V, E, DirectedGraph<V, E>>(graph, H_V, H_E);

        return new Subgraph[]{ DAG, H };
    }
}
