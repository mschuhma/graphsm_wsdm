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

import dk.aaue.sna.util.FuzzyUtil;
import org.jgrapht.Graph;

import java.util.*;

/**
 * Betweenness centrality based on the Brandes algorithm [1], which evaluates
 * in O(n*m*log(n)).
 * <p>
 * Betweenness measures the number of shortest-paths a node is on. It can be used
 * to indicate if a vertex has a gatekeeper role in the graph.
 * </p>
 * <p>
 * [1] Ulrik Brandes, A faster algorithm for betweenness centrality,
 *  Journal of Mathematical Society 25, pp. 163-177, 2001.
 * </p>
 * @param <V>
 * @param <E>
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class BrandesBetweennessCentrality<V, E> implements CentralityMeasure<V> {

	private static <V> Map<V, List<V>> createP(Set<V> nodes) {
		Map<V, List<V>> P = new HashMap<V, List<V>>();
		for (V v : nodes) {
			P.put(v, new ArrayList<V>());
		}
		return P;
	}

	private static <V> Map<V, Integer> initIntMap(Collection<V> nodes, V s, int defval, int selval) {
		Map<V, Integer> m = new HashMap<V, Integer>();
		for (V v : nodes) {
			m.put(v, v.equals(s) ? selval : defval);
		}
		return m;
	}

	private Graph<V, E> graph;

	public BrandesBetweennessCentrality(Graph<V, E> graph) {
		this.graph = graph;
	}

	public CentralityResult<V> calculate() {

		Set<V> V = graph.vertexSet();

		Map<V, Double> CB = new HashMap<V, Double>();
		for (V v : V)
			CB.put(v, 0.0);

		for (V s : V) {

			Stack<V> S = new Stack<V>();
			Map<V, List<V>> P = createP(V);
			Map<V, Integer> rho = initIntMap(V, s, 0, 1);
			Map<V, Integer> d = initIntMap(V, s, -1, 0);

			Queue<V> Q = new LinkedList<V>();

			Q.add(s);

			while (!Q.isEmpty()) {

				V v = Q.poll();
				S.push(v);

				for (E edge : graph.edgesOf(v)) {

					V w = graph.getEdgeSource(edge);
					if (w.equals(v)) {
						w = graph.getEdgeTarget(edge);
					}

					// System.out.println("v neighbour w: " + v + ", " + w + " d[w] = " + d.get(w));

					// w found for the first time?
					if (d.get(w) < 0) {
						Q.add(w);
						d.put(w, d.get(v) + 1);
					}
					// shortest path to w via v?
					if ((int) d.get(w) == (int) d.get(v) + 1) {
						rho.put(w, rho.get(w) + rho.get(v));
						P.get(w).add(v);
					}
				}
			}

			Map<V, Double> delta = new HashMap<V, Double>();
			for (V v : V)
				delta.put(v, 0.0);

			while (!S.isEmpty()) {
				V w = S.pop();
				for (V v : P.get(w)) {
					double tmp = delta.get(v) + (((double) rho.get(v) / rho.get(w)) * (1 + delta.get(w)));

					delta.put(v, tmp);
				}

				if (!w.equals(s)) {
					CB.put(w, CB.get(w) + delta.get(w));
				}
			}
		}

		// normalize
		/*
		double H = (V.size() - 1)*(V.size() - 2)/2;
		for (V n : CB.keySet()) {
			CB.put(n, CB.get(n) / H);
		}
		*/

        //CentralityResult<V> r = new CentralityResult<V>(CB, true);

        CentralityResult<V> r = new CentralityResult<V>(FuzzyUtil.minMaxNormalize(CB), true);

		return r;
	}
}
