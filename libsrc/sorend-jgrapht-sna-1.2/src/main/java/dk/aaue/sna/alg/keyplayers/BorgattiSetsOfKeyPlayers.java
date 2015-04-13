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

package dk.aaue.sna.alg.keyplayers;

import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import org.jgrapht.Graph;
import org.jgrapht.graph.Subgraph;

import java.util.*;

/**
 * This is an implementation of Borgatti's sets of key players algorithm, presented in [1]
 *
 * [1] S. P. Borgatti, Identifying sets of key players in a social network, Comput Math Organiz Theor 12, p. 21-34, 2006.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class BorgattiSetsOfKeyPlayers<V, E> implements CentralityMeasure<V> {

    public static enum Type {
        KPP_POS,
        KPP_NEG;
    }

    private int k;
    private Type type;
    private Graph<V, E> graph;

    public BorgattiSetsOfKeyPlayers(Graph<V, E> graph ,int k, Type type) {
        this.graph = graph;
        this.k = k;
        this.type = type;
    }

    private interface FitnessFunction<V> {
        public double fitness(List<V> V, Set<V> S);
    }

    public static <V, E> Set<V> optimize(Graph<V, E> graph, int k, FitnessFunction<V> ff) {

        Random rand = new Random();

        List<V> V = new ArrayList<V>(graph.vertexSet());

        Set<V> S = new HashSet<V>();

        if (V.size() <= k)
            throw new RuntimeException("Graph has less or equal to " + k + " nodes.");

        // initialize S with k random nodes.
        while (S.size() < k)
            S.add(V.get(rand.nextInt(V.size())));

        List<V> U = new ArrayList<V>(V);
        U.removeAll(S);

        System.out.println("S=" + S);

        FloydWarshallAllShortestPaths<V, E> fw = new FloydWarshallAllShortestPaths<V, E>(graph, false);

        while (true) {

            double F = ff.fitness(V, S);

            double F_best = Double.NEGATIVE_INFINITY;
            V U_best = null;
            V S_worst = null;

            for (V s : S) {
                for (V u : U) {

                    // construct new S, with s<->u
                    Set<V> S_new = new HashSet<V>(S);
                    S_new.remove(s);
                    S_new.add(u);

                    double deltaf = ff.fitness(V, S_new) - F;

                    // System.out.println("deltaf(s=" + s + ", u=" + u + ") = " + deltaf);

                    if (deltaf > F_best) {
                        U_best = u;
                        S_worst = s;
                        F_best = deltaf;
                    }
                }
            }

            if (F_best <= 0.0) {

                if (F_best == 0.0) {
                    S.remove(S_worst);
                    S.add(U_best);

                    U.add(S_worst);
                    U.remove(U_best);
                }

                break;
            }

            S.remove(S_worst);
            S.add(U_best);

            U.add(S_worst);
            U.remove(U_best);

            System.out.println("S' = " + S);

        }

        System.out.println("final S = " + S);

        return S;
    }

    @Override
    public CentralityResult<V> calculate() {
        Set<V> res;
        if (Type.KPP_POS == type) {
            res = calculateKPPPos(graph, k);
        }
        else {
            res = calculateKPPNeg(graph, k);
        }

        Map<V, Double> r = new HashMap<V, Double>();
        for (V v : graph.vertexSet()) {
            if (res.contains(v))
                r.put(v, 1.0);
            else
                r.put(v, 0.1);
        }

        return new CentralityResult<V>(r, false);
    }

    public static <V, E> Set<V> calculateKPPPos(Graph<V, E> graph, int k) {

        // cache the shortest paths.
        final FloydWarshallAllShortestPaths<V, E> fw = new FloydWarshallAllShortestPaths<V, E>(graph, false);

        // create a fitness function which uses the DR measure.
        FitnessFunction<V> fitness = new FitnessFunction<V>() {
            @Override
            public double fitness(List<V> V, Set<V> S) {
                return calculateDRMeasure(V, fw, S);
            }
        };

        // run the Borgatti greedy optimizer.
        return optimize(graph, k, fitness);
    }

    public static <V, E> Set<V> calculateKPPNeg(final Graph<V, E> graph, int k) {

        FitnessFunction<V> fitness = new FitnessFunction<V>() {
            @Override
            public double fitness(List<V> V, Set<V> S) {
                return calculateDFWithoutS(graph, S);
            }
        };

        return optimize(graph, k, fitness);
    }

    protected static <V, E> double calculateDFWithoutS(Graph<V, E> graph, Set<V> S) {

        // construct subgraph without S.
        Set<V> V = new HashSet<V>(graph.vertexSet());
        V.removeAll(S);
        Graph<V, E> subGraph = new Subgraph(graph, V);

        // calculate DF measure on subgraph
        return calculateDFMeasure(new ArrayList<V>(V), new FloydWarshallAllShortestPaths<V, E>(subGraph, false));
    }

    protected static <V, E> double calculateDRMeasure(List<V> V, FloydWarshallAllShortestPaths<V, E> sp, Set<V> S) {

        int n = V.size();

        double sum = 0.0;
        for (int j = 0; j < n; j++) {
            V v_j = V.get(j);

            // v_j is part of S
            if (S.contains(v_j)) {
                continue;
            }

            double d_K_j = Double.POSITIVE_INFINITY;
            for (V s_v : S) {
                d_K_j = Math.min(d_K_j, sp.shortestDistance(v_j, s_v));
            }

            sum += 1.0 / d_K_j;
        }

        return sum / (n - S.size());
    }

    protected static <V, E> double calculateDFMeasure(List<V> V, FloydWarshallAllShortestPaths<V, E> sp) {

        int n = V.size();

        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d_i_j = sp.shortestDistance(V.get(i), V.get(j));
                sum += 1.0 / d_i_j;
            }
        }

        double D_F = 1 - ((2.0 * sum) / (n * (n - 1)));

        return D_F;
    }
}
