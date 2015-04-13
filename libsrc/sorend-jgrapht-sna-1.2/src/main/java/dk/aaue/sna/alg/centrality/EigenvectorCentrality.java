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
 * Implementation of eigenvector centrality [1].
 *
 * <p>
 *     P. Bonacich, Some unique properties of eigenvector centrality, Social Networks 29(4): 555-564, 2007.
 * </p>
 *
 * @author Soren A D <sdavid08@student.aau.dk>
 * @param <V> node type
 * @param <E> edge type
 */
public class EigenvectorCentrality<V, E> implements CentralityMeasure<V> {

	private int iterations = 100;
	private double tolerance = 0.0001;
	private Graph<V, E> graph;

	public EigenvectorCentrality(Graph<V, E> graph) {
		this.graph = graph;
	}

    /**
     * Set maximum number of iterations to wait for matrix to converge. (default 100)
     * @param iterations the value
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * Set tolerance to control when matrix is converged. (default 0.0001)
     * @param tolerance the value
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    public void normalize(Map<V, Double> map) {
		// create sum
		double sum = 0.0;
		for (double d : map.values()) {
			sum += d;
		}

		if (sum != 0.0)
			sum = 1.0 / sum;

		// update normalized map
		for (V n : map.keySet()) {
			map.put(n, map.get(n) * sum);
		}
	}

	private Map<V, Double> randomMap(Collection<V> nodes, Random rand) {
		Map<V, Double> m = new HashMap<V, Double>();
		for (V v : nodes) {
			m.put(v, rand.nextDouble());
		}
		return m;
	}

	public CentralityResult<V> calculate() {

		Map<V, Double> x = randomMap(graph.vertexSet(), new Random());
		normalize(x);

		for (int i = 0; i < iterations; i++) {

			Map<V, Double> x0 = new HashMap<V, Double>();
			x0.putAll(x);

			for (V n : x.keySet()) {

				for (V nbr : Graphs.neighborListOf(graph, n)) {

					// update the value x[n] += 0.01 + x0[n] * W[n][nbr] * r
					double v = x.get(n);
					v = v + 0.01 + x0.get(nbr);
					x.put(n, v);

					// System.out.println(String.format("v = %.10f, n = %s, nbr = %s, x0[v] = %.10f", v, n, nbr, x0.get(n)));
				}
			}
			normalize(x);

			// sum([abs(x[n]-x0[n]) for n in x])
			double e = 0.0;
			for (V n : x.keySet()) {
				e += Math.abs(x.get(n) - x0.get(n));
			}

			double tval = x.size() * tolerance;

			// System.out.println(String.format("e=%.10f, tval=%.10f", e, tval));
			if (e < tval) {
				// normalize between 0.0 and 1.0
				double max = 0.0;
				for (double d : x.values()) {
					max = Math.max(max, d);
				}
				if (max == 0.0) {
					max = 1.0;
				}
				for (V n : x.keySet()) {
					double v = x.get(n);
					x.put(n, v / max);
				}
                return new CentralityResult<V>(x, true);
			}
		}

		// eigenvector did not converge, set 0.0 values
		for (V n : x.keySet()) {
			x.put(n, 0.0);
		}

		return new CentralityResult<V>(x, true);
	}
}
