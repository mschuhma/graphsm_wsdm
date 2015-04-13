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

import org.jgrapht.Graph;
import org.jgrapht.Graphs;

import java.util.*;

/**
 * EdmondsKarp maximum flow [1] calculator. Calculates the maximum flow between
 * two nodes. Complexity is known to be O(n m^2).
 * <p>
 * Note: There is already an implementation of Edmonds-Karp algorithm in JGraphT,
 * which is probably what we want. However, it only works on directed graphs.
 * Here we consider both where undirected has flow in both directions.
 * </p>
 * <p>
 * [1] Jack Edmonds and Richard M. Karp, <i>Theoretical improvements in algorithmic efficiency for network flow problems</i>,
 *  Journal of the ACM 19(2), pp. 248–264, 1972.
 * </p>
 * @author Soren <soren@tanesha.net>
 */
public class EdmondsKarpMaximumFlow<V, E> {

    private List<V> V;
    private int[][] E;
    private int[][] C;

    public EdmondsKarpMaximumFlow(Graph<V, E> graph, int edgeCapacity) {

        // cache the vertex lists
        this.V = new ArrayList(graph.vertexSet());

        // create neighbour lists.
        this.E = new int[V.size()][];
        for (int i = 0; i < V.size(); i++) {
            List<V> nbr = Graphs.neighborListOf(graph, V.get(i));
            E[i] = new int[nbr.size()];
            for (int j = 0; j < nbr.size(); j++)
                E[i][j] = V.indexOf(nbr.get(j));
        }

        // create capacity array
        C = new int[V.size()][V.size()];
        for (int i = 0; i < V.size(); i++) {
            Arrays.fill(C[i], edgeCapacity);
        }
    }

    public int maximumFlow(V s, V t) {

        if (!(V.contains(s) && V.contains(t)))
            throw new RuntimeException("Graph does not contain s, t");

        int sIdx = V.indexOf(s);
        int tIdx = V.indexOf(t);

        return edmondsKarp(E, C, sIdx, tIdx);
    }

    /**
     * Finds the maximum flow in a flow network.
     *
     * @param E neighbour lists
     * @param C capacity matrix (must be n by n)
     * @param s source
     * @param t sink
     * @return maximum flow
     */
    private static int edmondsKarp(int[][] E, int[][] C, int s, int t) {
        int n = C.length;
        // Residual capacity from u to v is C[u][v] - F[u][v]
        int[][] F = new int[n][n];
        while (true) {
            int[] P = new int[n]; // Parent table
            Arrays.fill(P, -1);
            P[s] = s;
            int[] M = new int[n]; // Capacity of path to node
            M[s] = Integer.MAX_VALUE;
            // BFS queue
            Queue<Integer> Q = new LinkedList<Integer>();
            Q.offer(s);
            LOOP:
            while (!Q.isEmpty()) {
                int u = Q.poll();
                for (int v : E[u]) {
                    // There is available capacity,
                    // and v is not seen before in search
                    if (C[u][v] - F[u][v] > 0 && P[v] == -1) {
                        P[v] = u;
                        M[v] = Math.min(M[u], C[u][v] - F[u][v]);
                        if (v != t)
                            Q.offer(v);
                        else {
                            // Backtrack search, and write flow
                            while (P[v] != v) {
                                u = P[v];
                                F[u][v] += M[t];
                                F[v][u] -= M[t];
                                v = u;
                            }
                            break LOOP;
                        }
                    }
                }
            }
            if (P[t] == -1) { // We did not find a path to t
                int sum = 0;
                for (int x : F[s])
                    sum += x;
                return sum;
            }
        }
    }
}
