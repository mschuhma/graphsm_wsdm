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

import dk.aaue.sna.alg.FloydWarshallAllShortestPaths;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of closeness centrality according to latora. Uses floyd-warshall internally to calculate shortest paths.
 *
 * <p>
 * [1] Freeman, Linton, A set of measures of centrality based upon betweenness, Sociometry 40: 35–41, 1977.
 * </p>
 * @param <V> node type
 * @param <E> edge type
 */
public class LatoraClosenessCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
	private FloydWarshallAllShortestPaths<V, E> fw;

	public LatoraClosenessCentrality(Graph<V, E> graph) {
        this(graph, new FloydWarshallAllShortestPaths<V, E>(graph));
	}

	public LatoraClosenessCentrality(Graph<V, E> graph, FloydWarshallAllShortestPaths<V, E> fw) {
		this.graph = graph;
        this.fw = fw;
	}

	public CentralityResult<V> calculate() {

		Map<V, Double> cc = new HashMap<V, Double>();

		Set<V> V = graph.vertexSet();

		for (V u : V) {

			double sum = 0.0;
			for (V v : V) {
				// skip reflexiveness
				if (u == v)
					continue;

				// get length of the path
				double length = fw.shortestDistance(u, v);

				// infinite -> there is no path.
				if (Double.isInfinite(length)) {
                    sum += 0;
				}
                else {
                    sum += (1.0 / length);
                }
			}

            cc.put(u, (1.0 / (V.size() * (V.size() - 1))) * sum);
		}

        return new CentralityResult<V>(cc, true);
	}

}
