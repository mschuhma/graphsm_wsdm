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

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import org.jgrapht.graph.GraphPathImpl;

/**
 * This is an implementation of the Floyd-Warshall all shortest paths
 * algorithm.
 *
 * There is already a FW shortest paths algorithm in JGraphT, but this one is
 * almost a factor 20 faster (I don't know why it is so slow, but I suspect the
 * index(node) instead of caching.
 *
 * Further, this one supports access to the shortest paths {@link FloydWarshallAllShortestPaths#shortestPath(Object, Object)}, which is not
 * available in the other one.
 *
 * @author Soren Davidsen <soren@tanesha.net>
 */
public class FloydWarshallAllShortestPaths<V, E> {

	private Graph<V, E> graph;
    private boolean doBacktrace;
	private List<V> vertices;
	private int countShortestPaths = 0;
    private double diameter = 0.0;

	private double[][] d = null;
    private int[][] backtrace = null;
    private Map<VertexPair<V>, GraphPath<V,E>> paths = null;

    public FloydWarshallAllShortestPaths(Graph<V, E> graph, boolean doBacktrace) {
        this.doBacktrace = doBacktrace;
		this.graph = graph;
        this.vertices = new ArrayList<V>(graph.vertexSet());
	}

    public FloydWarshallAllShortestPaths(Graph<V, E> graph) {
        this(graph, true);
	}

	public Graph<V, E> getGraph() {
		return graph;
	}

	public int shortestPathsCount() {
		return countShortestPaths;
	}

	/**
	 * Calculates all shortest paths.
	 */
	public void lazyCalculate() {

		int n = vertices.size();

        // init the backtrace matrix
        if (doBacktrace) {
            backtrace = new int[n][n];
            for (int i = 0; i < n; i++)
                Arrays.fill(backtrace[i], -1);
        }

        // initialize matrix, 0
        d = new double[n][n];
        for (int i = 0; i < n; i++)
            Arrays.fill(d[i], Double.POSITIVE_INFINITY);

		// initialize matrix, 1
		for (int i = 0; i < n; i++) {
			d[i][i] = 0.0;
		}

		// initialize matrix, 2
        Set<E> edges = graph.edgeSet();
		for (E edge : edges) {
			V v1 = graph.getEdgeSource(edge);
			V v2 = graph.getEdgeTarget(edge);

			int v_1 = vertices.indexOf(v1);
			int v_2 = vertices.indexOf(v2);

			d[v_1][v_2] = graph.getEdgeWeight(edge);
            d[v_2][v_1] = d[v_1][v_2];
		}

        // run fw alg
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					double ik_kj = d[i][k] + d[k][j];
					if (ik_kj < d[i][j]) {
						d[i][j] = ik_kj;
                        d[j][i] = ik_kj;
                        if (doBacktrace) {
                            backtrace[i][j] = k;
                            backtrace[j][i] = k;
                        }
                        if (d[i][j] > diameter)
                            diameter = d[i][j];
					}
				}
			}
		}
	}

    public double averagePathLength() {

        if (d == null)
            lazyCalculate();

        double sum = 0.0;
        double c = 0.0;

        for (int i = 0; i < d.length; i++) {
            for (int j = 0; j < d[i].length; j++) {
                if (!Double.isInfinite(d[i][j])) {
                    sum += d[i][j];
                    c += 1.0;
                }
            }
        }

        return sum / c;
    }

	/**
	 * Get the length of a shortest path.
	 * @param a
	 * @param b
	 * @return
	 */
	public double shortestDistance(V a, V b) {

        // lazy
        if (d == null)
            lazyCalculate();

		return d[vertices.indexOf(a)][vertices.indexOf(b)];
	}

    public double getDiameter() {
        // lazy
        if (d == null)
            lazyCalculate();

        return diameter;
    }

    private void shortestPathRecur(List<E> edges, int v_a, int v_b) {
        int k = backtrace[v_a][v_b];
		if (k == -1) {
			E edge = graph.getEdge(vertices.get(v_a), vertices.get(v_b));
			if (edge != null)
				edges.add(edge);
		}
		else {
			shortestPathRecur(edges, v_a, k);
			shortestPathRecur(edges, k, v_b);
		}
	}

    /**
     * Get the shortest path between two vertices.
     *
     * Note: The paths are calculated using a recursive algorithm. It *will* give problems on paths longer than
     * the heap allows.
     *
     * @param a From vertice
     * @param b To vertice
     * @return the path
     */
	public GraphPath<V, E> shortestPath(V a, V b) {

        if (!doBacktrace)
            throw new IllegalArgumentException("Backtrace not enabled, cannot find shortest path.");

        if (d == null)
            lazyCalculatePaths();

        int v_a = vertices.indexOf(a);
        int v_b = vertices.indexOf(b);

		List<E> edges = new ArrayList<E>();
		shortestPathRecur(edges, v_a, v_b);

        // no path, return null
        if (edges.size() < 1)
            return null;

        GraphPathImpl<V, E> path = new GraphPathImpl<V, E>(graph, a, b, edges, edges.size());

		return path;
	}

    /**
     * Calculate the shortest paths (not done per default)
     * @return the number of shortest paths.
     */
	public int lazyCalculatePaths() {

        // already we have calculated it once.
		if (paths != null) {
			return countShortestPaths;
		}

        // we don't have shortest paths.. lazyCalculate it.
        if (d == null)
            lazyCalculate();

        Map<VertexPair<V>, GraphPath<V, E>> sps = new HashMap<VertexPair<V>, GraphPath<V, E>>();
        int n = vertices.size();

		countShortestPaths = 0;
		for (int i = 0; i < n; i++) {

			for (int j = 0; j < n; j++) {

                // don't count this.
                if (i == j)
                    continue;

                V v_i = vertices.get(i);
                V v_j = vertices.get(j);

				GraphPath<V, E> path = shortestPath(v_i, v_j);

                // we got a path
                if (path != null) {
                    sps.put(new VertexPair<V>(v_i, v_j), path);
                    countShortestPaths++;
                }
			}
		}

        this.paths = sps;

		return countShortestPaths;
	}

    /**
     * Get shortest paths from a vertex to all other vertices in the graph.
     * @param v the originating vertex
     * @return List of paths
     */
	public List<GraphPath<V, E>> getShortestPaths(V v) {
		if (v == null)
			return null;

		List<GraphPath<V, E>> found = new ArrayList<GraphPath<V, E>>();
        for (VertexPair<V> pair : paths.keySet()) {
            if (pair.hasVertex(v)) {
                found.add(paths.get(pair));
            }
        }

		return found;
	}
}
