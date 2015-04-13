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
import org.apache.commons.math.linear.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Subgraph;

import java.util.*;

/**
 * Find hierarchial structure in complex networks, an implementation of [1].
 *
 * [1] F. Chen et al., Finding and evaluating the hierarchial structure in complex networks, J. Phys A: Math. Theor. 40, 2007.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class ChenComplexNetworksHierarchy<V, E> implements CentralityMeasure<V> {

    private Graph<V, E> graph;
    private CentralityMeasure<V> innerMeasure;
    private int iterations = 100;
    private double tolerance = 0.0001;

    /**
     * Constructor. Note, the centrality measure should be created for graph.
     *
     * @param graph        the graph
     * @param innerMeasure the chosen centrality measure on graph.
     */
    public ChenComplexNetworksHierarchy(Graph<V, E> graph, CentralityMeasure<V> innerMeasure) {
        this.graph = graph;
        this.innerMeasure = innerMeasure;
    }

    @Override
    public CentralityResult<V> calculate() {

        // STEP 1: Calculate centrality measure
        // calculate the centrality
        CentralityResult<V> result = innerMeasure.calculate();

        Map<V, Integer> V = new HashMap<V, Integer>();
        int i = 0;
        for (V v : graph.vertexSet())
            V.put(v, i++);

        int n = V.size();

        // construct initial EC
        RealMatrix EC = new Array2DRowRealMatrix(n, n);
        for (V u : V.keySet()) {
            int uIdx = V.get(u);
            for (V v : V.keySet()) {
                int vIdx = V.get(v);
                EC.setEntry(uIdx, vIdx, Math.abs(result.get(u) - result.get(v)));
            }
        }

        LinkedList<PartitionArgs<V, E>> Q = new LinkedList<PartitionArgs<V, E>>();
        Q.add(new PartitionArgs<V, E>(V, EC, graph));

        while (!Q.isEmpty()) {

            PartitionArgs<V, E> args = Q.removeFirst();

            Map<String, Set<V>> partitions = partition(args.V, args.EC, args.graph);

            System.out.println("partitioned (" + args.graph + "): " + partitions);

            if (partitions == null)
                continue;

            for (Set<V> partition : partitions.values()) {
                Subgraph<V, E, Graph<V, E>> subgraph = new Subgraph<V, E, Graph<V, E>>(graph, partition);
                Q.add(new PartitionArgs<V, E>(V, EC, subgraph));
            }
        }

        return null;
    }

    private static class PartitionArgs<V, E> {
        Map<V, Integer> V;
        RealMatrix EC;
        Graph<V, E> graph;

        private PartitionArgs(Map<V, Integer> V, RealMatrix EC, Graph<V, E> graph) {
            this.V = V;
            this.EC = EC;
            this.graph = graph;
        }
    }

    private static <V, E> Map<String, Set<V>> partition(Map<V, Integer> V, RealMatrix EC, Graph<V, E> graph) {

        // consistent ordering of the nodes for the Avg matrix.
        List<V> localV = new ArrayList<V>(graph.vertexSet());
        int n = localV.size();

        // STEP 2: Calculate the Matrix "B" (B_ij = Avg_ij - EC_ij)
        //

        // construct Avg matrix
        RealMatrix Avg = new Array2DRowRealMatrix(n, n);
        double c = 1.0 / (n * (n - 1));
        for (int i = 0; i < localV.size(); i++) {
            int iEC = V.get(localV.get(i));

            for (int j = 0; j < localV.size(); j++) {
                int jEC = V.get(localV.get(j));
                if (i == j)
                    Avg.setEntry(i, j, 0.0);
                else {
                    // Avg_ij = c * (Sum_i,j EC_ij)
                    double sum = 0.0;
                    for (int k = 0; k < n; k++) {
                        sum += EC.getEntry(iEC, k);
                        sum += EC.getEntry(k, jEC);
                    }
                    Avg.setEntry(i, j, c * sum);
                }
            }
        }

        // construct B matrix
        RealMatrix B = Avg.copy();
        for (int i = 0; i < B.getRowDimension(); i++) {
            V v_i = localV.get(i);
            int ecIdxI = V.get(v_i);
            for (int j = 0; j < B.getColumnDimension(); j++) {
                V v_j = localV.get(j);
                int ecIdxJ = V.get(v_j);
                double v = B.getEntry(i, j);
                B.setEntry(i, j, v - EC.getEntry(ecIdxI, ecIdxJ));
            }
        }

        // STEP 3: Calculate eigenvalues and eigenvectors of B
        EigenDecomposition eigen = new EigenDecompositionImpl(B, 0.001);

        // find v_l
        double[] eigenvalues = eigen.getRealEigenvalues();
        double largestVal = Double.NEGATIVE_INFINITY;
        RealVector v_l = null;
        for (int i = 0; i < eigenvalues.length; i++) {
            if (eigenvalues[i] > largestVal) {
                largestVal = eigenvalues[i];
                v_l = eigen.getEigenvector(i);
            }
        }

        Set<V> s_pos = new HashSet<V>();
        Set<V> s_neg = new HashSet<V>();
        for (int i = 0; i < v_l.getDimension(); i++) {
            double v_l_i = v_l.getEntry(i);
            if (v_l_i >= 0)
                s_pos.add(localV.get(i));
            else
                s_neg.add(localV.get(i));
        }


        // STEP 4: Check for empty group
        if (s_pos.size() < 1 || s_neg.size() < 1)
            return null;

        Map<String, Set<V>> res = new HashMap<String, Set<V>>();
        res.put("pos", s_pos);
        res.put("neg", s_neg);

        return res;
    }
}
