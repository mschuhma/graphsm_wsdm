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

import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import dk.aaue.sna.alg.VertexPair;
import org.jgrapht.*;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.*;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;

public class JGraphTUtil {

    public static <V, E> double averagePathLength(Graph<V, E> graph) {
        return averagePathLength(graph, new FloydWarshallAllShortestPaths<V, E>(graph));
    }

    public static <V, E> double averagePathLength(Graph<V, E> graph, FloydWarshallAllShortestPaths<V, E> fwAlg) {
        double sum = 0.0;
        double c = 0.0;
        int n = graph.vertexSet().size();
        for (V v : graph.vertexSet()) {
            for (V u : graph.vertexSet()) {
                if (v == u)
                    continue;
                double p = fwAlg.shortestDistance(v, u);
                if (Double.isInfinite(p))
                    continue;
                sum += p;
                c++;
            }
        }
        return sum / c;
    }

    public static interface PairVisitor<V> {
        public void visit(V v_i, V v_j);
    }

    public static <V, E> void vertexPairIterator(Graph<V, E> graph, PairVisitor<V> visitor) {
        List<V> V = new ArrayList<V>(graph.vertexSet());
        // directed graph, visit all combinations (v_i, v_j), (v_j, v_i)
        if (isDirected(graph)) {
            for (V v_i : V) {
                for (V v_j : V) {
                    if (v_i == v_j)
                        continue;
                    visitor.visit(v_i, v_j);
                }
            }
        }
        // undirected, only visit (v_i, v_j)
        else {
            for (int i = 0; i < V.size(); i++) {
                for (int j = i + 1; j < V.size(); j++) {
                    visitor.visit(V.get(i), V.get(j));
                }
            }
        }
    }

    public static <V, E> void inverseWeights(WeightedGraph<V, E> g) {
        // set inverse weight on all edges
        for (E e : g.edgeSet()) {
            double weight = g.getEdgeWeight(e);
            double inverse = 1.0 / weight;
            g.setEdgeWeight(e, inverse);
        }
    }

    public static <V, E> void alphacut(WeightedGraph<V, E> g, double alphaCut, Graph<V, E> alphaCutGraph) {

        if (!(alphaCutGraph instanceof WeightedGraph)) {
            throw new IllegalArgumentException("alphaCutGraph must be weighted");
        }

        // clone the graph
        Graphs.addGraph(alphaCutGraph, g);

        // remove edges with a weight less than the cut
        Iterator<E> edgeIterator = alphaCutGraph.edgeSet().iterator();
        while (edgeIterator.hasNext()) {
            E e = edgeIterator.next();
            double w = alphaCutGraph.getEdgeWeight(e);
            if (w < alphaCut)
                edgeIterator.remove();
        }
    }

    public static <V, E> V radius(Graph<V, E> graph) {

        List<V> V = new ArrayList<V>(graph.vertexSet());
        FloydWarshallAllShortestPaths<V, E> fwsp = new FloydWarshallAllShortestPaths(graph);

        double min = Double.POSITIVE_INFINITY;
        V minV = null;

        for (V v_i : V) {
            double cur = 0.0;
            // distance to all other nodes
            for (V v_j : V)
                cur += fwsp.shortestDistance(v_i, v_j);

            // less, then we mark it
            if (cur < min) {
                min = cur;
                minV = v_i;
            }
        }
        return minV;
    }

    public static <V, E> VertexPair<V> diameterVertices(Graph<V, E> graph) {
        FloydWarshallAllShortestPaths<V, E> fwsp = new FloydWarshallAllShortestPaths(graph);
        return diameterVertices(graph, fwsp);
    }

    public static <V, E> VertexPair<V> diameterVertices(Graph<V, E> graph, FloydWarshallAllShortestPaths<V, E> fwsp) {

        List<V> V = new ArrayList<V>(graph.vertexSet());

        double diameter = fwsp.getDiameter();

        double max = 0.0;

        VertexPair<V> res = null;
        for (int i = 0; i < V.size(); i++) {
            V v_i = V.get(i);
            for (int j = i + 1; j < V.size(); j++) {
                V v_j = V.get(j);

                double p = fwsp.shortestDistance(v_i, v_j);
                // System.out.println("sp(" + v_i + ", " + v_j + ") = " + p);
                if (p != Double.POSITIVE_INFINITY && p > max) {
                    res = new VertexPair<V>(v_i, v_j);
                    max = p;
                }
            }
        }
        return res;
    }

    public static <V, E> AdjacencyMatrix<V> adjacencyMatrix(Graph<V, E> graph) {
        List<V> V = new ArrayList<V>(graph.vertexSet());
        int n = V.size();
        // build map (faster lookup)
        Map<V, Integer> vMap = new HashMap<V, Integer>();
        for (int i = 0; i < n; i++)
            vMap.put(V.get(i), i);

        double[][] A = new double[n][n];

        boolean undirected = !isDirected(graph);

        for (E e : graph.edgeSet()) {
            V src = graph.getEdgeSource(e);
            V dst = graph.getEdgeTarget(e);
            double w = graph.getEdgeWeight(e);

            A[vMap.get(src)][vMap.get(dst)] = w;
            if (undirected) {
                A[vMap.get(dst)][vMap.get(src)] = w;
            }
        }

        AdjacencyMatrix<V> r = new AdjacencyMatrix<V>();
        r.A = A;
        r.M = vMap;
        r.V = V;
        r.N = n;

        return r;
    }

    public static class AdjacencyMatrix<V> {
        public Map<V, Integer> M;
        public List<V> V;
        public double[][] A;
        public int N;
    }

    public static <V,E> Map<Integer, List<V>> degreeDistribution(Graph<V, E> graph) {

        Map<Integer, List<V>> map = new HashMap<Integer, List<V>>();

        for (V v : graph.vertexSet()) {
            int degree = graph.edgesOf(v).size();
            if (!map.containsKey(degree))
                map.put(degree, new ArrayList<V>());
            map.get(degree).add(v);
        }

        return map;
    }

    public static <V> Map<Integer, Integer> sumList(Map<Integer, List<V>> map) {
        Map<Integer, Integer> res = new HashMap<Integer, Integer>();
        for (Entry<Integer, List<V>> e : map.entrySet()) {
            res.put(e.getKey(), e.getValue().size());
        }
        return res;
    }

    public static <V, E> double effectivediameter(Graph<V, E> graph, FloydWarshallAllShortestPaths<V, E> fwsp) {

        List<V> V = new ArrayList<V>(graph.vertexSet());

        // find "true" diameter.
        List<Double> diameters = new ArrayList<Double>();
        for (V v : V) {
            for (V u : V) {
                if (v == u)
                    continue;
                double p = fwsp.shortestDistance(v, u);
                if (!Double.isInfinite(p))
                    diameters.add(p);
            }
        }

        // sort the diameters
        Collections.sort(diameters);

        // cut away the first 0.9
        List<Double> effective = diameters.subList((int) (0.9 * diameters.size()), diameters.size() - 1);

        // grab the 0.9 diameter
        return effective.get(0);
    }

    public static <V> V[] toArray(Collection<V> n, Class<V> clazz) {
        V[] arr = (V[]) Array.newInstance(clazz, n.size());
        int i = 0;
        for (Iterator<V> iterator = n.iterator(); iterator.hasNext();) {
            arr[i++] = iterator.next();
        }
        return arr;
    }

    public static <V, E> boolean isDirected(Graph<V, E> graph) {
        if (graph instanceof DirectedGraph)
            return true;
        else
            return false;
    }

    public static <V, E> int maxEdges(Graph<V, E> graph) {
        int vs = graph.vertexSet().size();

        if (graph instanceof UndirectedGraph) {
            return vs * (vs - 1) / 2;
        }
        else if (graph instanceof DirectedGraph) {
            return vs * (vs - 1);
        }
        else {
            throw new RuntimeException("Unknown graph type");
        }
    }

	/**
	 * Calculates the density of a graph
	 * @param graph
	 * @return
	 */
	public static <V, E> double density(Graph<V, E> graph) {
		int es = graph.edgeSet().size();
		return (double) es / maxEdges(graph);
	}

    public static <V, E> boolean isConnected(Graph<V, E> g) {
        if (isDirected(g))
            return new ConnectivityInspector<V, E>((DirectedGraph<V, E>)g).isGraphConnected();
        else
            return new ConnectivityInspector<V, E>((UndirectedGraph<V, E>)g).isGraphConnected();
    }

    public static <V, E, G extends Graph<V, E>> G clone(G graph) {

        try {
            G cloned = null;

            EdgeFactory ef = graph.getEdgeFactory();
            try {
                // lookup a constructor which takes an edge-factory as parameter
                Constructor efCon = graph.getClass().getConstructor(new Class[]{EdgeFactory.class});
                cloned = (G) efCon.newInstance(ef);
            }
            catch (NoSuchMethodException me) {

                // lookup a constructor which takes an edge-type as parameter
                Constructor etCon = graph.getClass().getConstructor(new Class[]{ Class.class });
                Object edge = ef.createEdge(null, null);
                cloned = (G) etCon.newInstance(edge.getClass());
            }

            // add all data to the cloned from the original
            Graphs.addGraph(cloned, graph);

            // done
            return cloned;
        }
        catch (Exception e) {
            throw new RuntimeException("Error cloning: " + e.getMessage(), e);
        }
    }


}
