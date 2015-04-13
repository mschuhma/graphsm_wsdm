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
import org.jgrapht.alg.BellmanFordShortestPath;
import org.jgrapht.graph.AsUnweightedGraph;

import java.util.*;

/**
 * K-path centrality [1] is a generalisation of degree centrality [2].
 * <p>
 * Instead of counting only neighbours reachable through 1 step, it counts neighbours
 * reachable through "k" steps.
 * </p>
 * <p>
 * [1] Stephen P. Borgatti and Martin G. Everett, <i>A Graph-theoretic perspective on centrality</i>,
 *  Social Networks 28(4), pp. 466-484, 2006.<br />
 * [2] L. C. Freeman, <i>Centrality in social networks; conceptual clarification</i>,
 *  Social Networks 1, pp. 215-239, 1979.
 * </p>
 * @author Soren A D <sdavid08@student.aau.dk>
 * @param <V> Type of vertices
 * @param <E> Type of edges
 */
public class KPathCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
    private int k;

	public KPathCentrality(Graph<V, E> graph, int k) {
        this.graph = graph;
        this.k = k;
	}

    public CentralityResult<V> calculate() {
        return calculate_queue();
    }

    @SuppressWarnings("unused")
    protected CentralityResult<V> calculate_sp() {

        Map<V, Double> r = new HashMap<V, Double>();

        AsUnweightedGraph<V, E> unweighted = new AsUnweightedGraph<V, E>(graph);

        List<V> V = new ArrayList<V>(graph.vertexSet());

        int n = V.size();

        for (V u : V) {
            BellmanFordShortestPath<V, E> sp = new BellmanFordShortestPath<V, E>(unweighted, u, this.k);
            int found = 0;
            for (V v : V) {
                if (v == u)
                    continue;
                if (sp.getCost(v) <= this.k)
                    found++;
            }
            r.put(u, (double) found);
        }

        return new CentralityResult<V>(r, true);
    }

    protected CentralityResult<V> calculate_queue() {

        Map<V, Double> r = new HashMap<V, Double>();

        List<V> V = new ArrayList<V>(graph.vertexSet());

        for (V v_i : V) {

            HashSet<V> P = new HashSet<V>();
            HashSet<V> Q = new HashSet<V>();

            // head of queue, we start with v_i
            Q.add(v_i);

            // find with-in k steps
            for (int i = 0; i < k; i++) {
                // System.out.println("processing k = " + (i+1));

                HashSet<V> newQ = new HashSet<V>();
                for (V v_k : Q) {
                    List<V> nbr = Graphs.neighborListOf(graph, v_k);
                    // System.out.println("nbr(" + v_k + ") = " + nbr);
                    for (V v_k_nbr : nbr) {
                        if (v_k_nbr == v_i)
                            continue;
                        // is it a new node?  add it to Q if yes.
                        if (!P.contains(v_k_nbr)) {
                            newQ.add(v_k_nbr);
                            P.add(v_k_nbr);
                        }
                    }
                }
                Q = newQ;
            }

            // P is now all the neighbors upto k steps away.
            if (P.size() == 0) {
                r.put(v_i, Double.NEGATIVE_INFINITY);
            }
            else {
                r.put(v_i, (double) P.size());
            }
        }

        return new CentralityResult<V>(r, true);
	}

}