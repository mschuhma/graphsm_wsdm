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

import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immediate effect centrality [1] is a type of closeness centrality [2], which
 * counts all path-lengths instead of only the geodesics.
 * <p>
 * TODO: Unfinished!!
 * </p>
 * @param <V>
 * @param <E>
 */
public class ImmediateEffectCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
    private Map<VertexPair<V>, List<List<V>>> pathsCache = new HashMap<VertexPair<V>, List<List<V>>>();
    AllPaths<V, E> allPaths;

	public ImmediateEffectCentrality(Graph<V, E> graph) {
		this.graph = graph;
        this.allPaths = new AllPaths<V, E>(graph);
	}

    private List<List<V>> paths(V start, V end) {
        VertexPair<V> pair = new VertexPair<V>(start, end);
        if (pathsCache.containsKey(pair))
            return pathsCache.get(pair);
        else {
            List<List<V>> paths = allPaths.calculate(start, end);
            pathsCache.put(pair, paths);
            return paths;
        }
    }

	public CentralityResult<V> calculate() {

		Map<V, Double> cc = new HashMap<V, Double>();

		Set<V> V = graph.vertexSet();

		for (V n : V) {

			double sum = 0.0;
			double s = 0.0;
			for (V p : V) {
				// skip reflexiveness
				if (n == p)
					continue;

                List<List<V>> paths = paths(n, p);

                s += paths.size();

                for (List<V> path : paths) {
                    sum += path.size();
                }
			}

			if (sum == 0.0)
				cc.put(n, Double.NEGATIVE_INFINITY);
			else
				cc.put(n, s / sum);
		}

		return new CentralityResult(cc, true);
	}

}