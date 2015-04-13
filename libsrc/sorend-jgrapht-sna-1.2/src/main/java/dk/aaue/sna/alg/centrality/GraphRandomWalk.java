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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Creates a random walk [1] between two vertices in a graph.
 * <p>
 * Random walks are created using a uniform probability distribution to select the next node in the walk, see
 * {@link java.util.Random#nextInt(int)}.
 * </p>
 * <p>
 * [1] L. Lovasz, Random walks on graphs: A survey, Combinatorics; Paul Erdos is
 *  Eighty (2), pp. 1-26, 1993.
 * </p>
 *
 * @author Soren <sdavid08@student.aau.dk>
 */
public class GraphRandomWalk<V, E> {

    private Random rand = new Random();
    private Graph<V, E> graph;

    public GraphRandomWalk(Graph<V, E> graph) {
        this.graph = graph;
    }

    public List<V> randomWalk(V start, V end, int steps) {

        V current = start;
        List<V> res = new ArrayList<V>();
        res.add(current);

        // find the walk using maximum number of steps.
        for (int i = 0; i < steps; i++) {

            // algorithm has converted, the drunkard found his way home.
            if (current.equals(end))
                break;

            // next nodes for what direction we can take.
            List<V> nextOpt = new ArrayList<V>(Graphs.neighborListOf(graph, current));

            if (nextOpt.size() == 0) {
                throw new RuntimeException("Dead end node (can happen in directed graphs, and no backtracking, sorry).");
            }

            // select a random next node.
            int nextIdx = rand.nextInt(nextOpt.size());

            // goto next node, and add it to the walk we're creating.
            current = nextOpt.get(nextIdx);
            res.add(current);
        }

        // algorithm didn't converge, drunkard didn't find his way home within the allowed number of steps.
        if (!res.contains(end))
            throw new RuntimeException("Walk didn't converge in " + steps + " steps.");

        return res;
    }
}
