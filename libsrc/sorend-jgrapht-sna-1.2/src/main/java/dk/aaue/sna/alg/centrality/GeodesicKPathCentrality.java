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
import dk.aaue.sna.alg.centrality.CentralityMeasure;
import dk.aaue.sna.alg.centrality.CentralityResult;
import org.jgrapht.Graph;

import java.util.*;

/**
 * Geodesic K-path centrality [1] is a generalisation of degree centrality [2].
 * <p>
 * Instead of counting only direct neighbours, it counts neighbours as those that
 * are on a geodesic path less than "k" away.
 * </p>
 * <p>
 * [1] Stephen P. Borgatti and Martin FluentGraphHelper. Everett, <i>A Graph-theoretic perspective on centrality</i>,
 *  Social Networks 28(4), pp. 466-484, 2006.<br />
 * [2] L. C. Freeman, <i>Centrality in social networks; conceptual clarification</i>,
 *  Social Networks 1, pp. 215-239, 1979.
 * </p>
 * @param <V> Type of vertices
 * @param <E> Type of edges
 */
public class GeodesicKPathCentrality<V, E> implements CentralityMeasure<V> {

	private Graph<V, E> graph;
    private double k;

	public GeodesicKPathCentrality(Graph<V, E> graph, double k) {
        this.graph = graph;
        this.k = k;
	}

    public CentralityResult<V> calculate() {
        return calculate_fw();
    }

    protected CentralityResult<V> calculate_fw() {

        Map<V, Double> r = new HashMap<V, Double>();

        List<V> V = new ArrayList<V>(graph.vertexSet());

        FloydWarshallAllShortestPaths<V, E> fw = new FloydWarshallAllShortestPaths<V, E>(graph);

        for (V v_i : V) {

            double C_i = 0.0;

            // calculateSubgroupCenters C^k
            for (V v_j : V) {
                if (v_i == v_j)
                    continue;
                double pl = fw.shortestDistance(v_i, v_j);
                // only interested in k paths
                if (pl > k)
                    continue;
                // increment the count.
                C_i += 1.0;
            }
            r.put(v_i, C_i);
        }

        return new CentralityResult(r, true);
    }

}