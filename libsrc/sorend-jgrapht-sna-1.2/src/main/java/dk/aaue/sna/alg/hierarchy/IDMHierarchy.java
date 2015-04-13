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
import dk.aaue.sna.alg.centrality.DegreeCentrality;
import dk.aaue.sna.alg.centrality.EigenvectorCentrality;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;
import java.util.logging.Logger;

/**
 * An implementation of the "Investigative data mining" algorithm, see [1]
 *
 * There are two parts in their algorithm:
 * <ol>
 *     <li>Given an undirected graph, use degree and eigenvector centrality to construct a directed graph {@link IDMHierarchy#calculateDirected(org.jgrapht.Graph)}.</li>
 *     <li>Given a directed graph, construct a DAG, see {@link IDMHierarchy#calculateHierarchyDAG(org.jgrapht.DirectedGraph)}.</li>
 * </ol>
 *
 * In this implementation, we take the step further, by finding the roots of the DAG and construct a label which corresponds to the
 * placement in the hierarchy, see {@link dk.aaue.sna.alg.hierarchy.IDMHierarchy#calculate()}
 *
 * [1] M. A. Shaikh et al., Investigative data mining for counterterrorism, In Proc. of ICHIT 2006, LNAI 4413, pp. 31-41, 2007.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class IDMHierarchy<V, E> implements CentralityMeasure<V> {

    private static Logger LOG = Logger.getLogger(IDMHierarchy.class.getName());

    private Graph<V, E> graph;

    public IDMHierarchy(Graph<V, E> graph) {
        this.graph = graph;
    }

    @Override
    public CentralityResult<V> calculate() {

        DirectedGraph<V, E> directed = calculateDirected(graph);

        DirectedGraph<V, E> dag = calculateHierarchyDAG(directed);

        Map<V, Double> labels = new HashMap<V, Double>();

        Set<V> R = new HashSet<V>();
        for (V node : dag.vertexSet()) {
            if (dag.inDegreeOf(node) == 0)
                R.add(node);
        }

        double val = 0.0;
        while (!R.isEmpty()) {
            Set<V> cur = new HashSet<V>();
            cur.addAll(R);
            R.clear();

            double max = 0.0;
            for (V node : cur) {
                labels.put(node, val);
                for (E edge : dag.outgoingEdgesOf(node)) {
                    max = Math.max(max, dag.getEdgeWeight(edge));
                    R.add(dag.getEdgeTarget(edge));
                }
            }

            val += max;
        }

        return new CentralityResult<V>(labels, false);
    }

    public static <V, E> DirectedGraph<V, E> calculateHierarchyDAG(DirectedGraph<V, E> graph) {

        DirectedGraph<V, E> tree = new DefaultDirectedWeightedGraph<V, E>(graph.getEdgeFactory());

        // copy all nodes
        for (V node : graph.vertexSet())
            tree.addVertex(node);

        List<V> Q = new LinkedList<V>();
        Q.addAll(graph.vertexSet());

        while (!Q.isEmpty()) {

            // taking the node with minimum numbers of links originating and traverse it's each link.
            int minVal = Integer.MAX_VALUE;
            // take the first node from the list per default (avoid that we don't find any with an outdegree)
            V minNode = Q.get(0);
            for (V node : Q) {
                int originating = graph.outDegreeOf(node);
                if (originating > 0 && originating < minVal) {
                    minNode = node;
                    minVal = originating;
                }
            }

            if (minVal == Integer.MAX_VALUE) {
                LOG.info("No originating nodes found, using: " + minNode);
            }

            Q.remove(minNode);

            for (E edge : graph.outgoingEdgesOf(minNode)) {
                V otherNode = Graphs.getOppositeVertex(graph, edge, minNode);
                Set<E> otherEdges = graph.incomingEdgesOf(otherNode);
                // 3. every node adjacent to the current link will be placed under its predecessor
                //    if no other link is pointing towards it.
                if (otherEdges.size() == 1) {
                    tree.addEdge(minNode, otherNode);
                    Q.remove(otherNode);
                }
                // 4. if any ther link is pointing towards it, then it wlil be placed under the node
                //    that has more links directing towards its neighborhood.
                else {
                    V maxLinkingTowardsNode = null;
                    int maxLinkingTowardsVal = 0;
                    for (E otherEdge : otherEdges) {
                        V otherNeighbor = Graphs.getOppositeVertex(graph, otherEdge, otherNode);
                        if (graph.outDegreeOf(otherNeighbor) > maxLinkingTowardsVal) {
                            maxLinkingTowardsNode = otherNeighbor;
                            maxLinkingTowardsVal = graph.outDegreeOf(otherNeighbor);
                        }
                    }
                    tree.addEdge(maxLinkingTowardsNode, otherNode);
                    Q.remove(otherNode);
                }
            }

        }

        return tree;
    }

    public static <V, E> DirectedGraph<V, E> calculateDirected(Graph<V, E> graph) {

        final CentralityResult<V> degree = new DegreeCentrality<V, E>(graph).calculate();
        final CentralityResult<V> eigenv = new EigenvectorCentrality<V, E>(graph).calculate();

        Comparator<V> degreeEigenvCmp = new Comparator<V>() {
            @Override
            public int compare(V o1, V o2) {
                int rc = Double.compare(degree.get(o1), degree.get(o2));
                if (rc == 0)
                    rc = Double.compare(eigenv.get(o1), eigenv.get(o2));
                return rc;
            }
        };

        DefaultDirectedWeightedGraph<V, E> directed = new DefaultDirectedWeightedGraph<V, E>(graph.getEdgeFactory());

        for (E edge : graph.edgeSet()) {

            V source = graph.getEdgeSource(edge);
            V target = graph.getEdgeTarget(edge);
            double w = graph.getEdgeWeight(edge);

            if (!directed.containsVertex(source))
                directed.addVertex(source);
            if (!directed.containsVertex(target))
                directed.addVertex(target);

            int compare = degreeEigenvCmp.compare(source, target);
            if (compare == 0) {
                LOG.info("Ignored " + edge);
                continue;
            }

            E newEdge = compare > 0 ? directed.addEdge(source, target) : directed.addEdge(target, source);

            if (directed instanceof WeightedGraph && newEdge instanceof DefaultWeightedEdge)
                directed.setEdgeWeight(newEdge, w);
        }

        return directed;
    }


}
