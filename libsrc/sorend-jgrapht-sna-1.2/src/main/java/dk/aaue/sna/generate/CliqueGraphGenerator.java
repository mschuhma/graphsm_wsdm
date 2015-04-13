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

package dk.aaue.sna.generate;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.GraphGenerator;

import java.util.*;

/**
 * Synthesizes a clique graph, see [1]
 *
 * [1] Søren Atmakuri Davidsen and Daniel Ortiz-Arroyo, Robustness and Link Prediction in Complex Social Networks, Social Networks 2011, 2011.
 *
 * @author Soren <soren@tanesha.net>
 */
public class CliqueGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    private int n;
    protected double cliqueSize;
    protected double cliqueDensity;
    protected double cliqueConnectDensity;
    private Random random;

    public CliqueGraphGenerator(int n, double cliqueDensity, double cliqueSize, double cliqueConnectDensity) {
        this.n = n;
        this.cliqueDensity = cliqueDensity;
        this.cliqueSize = cliqueSize;
        this.cliqueConnectDensity = cliqueConnectDensity;
        this.random = new Random();
    }

    @Override
    public void generateGraph(Graph<V, E> veGraph, VertexFactory<V> vVertexFactory, Map<String, V> stringVMap) {

        // holds nodes of each clique
        Map<Integer, List<V>> cliqueNodesMap = new HashMap<Integer, List<V>>();
        Set<V> prevNodes = new HashSet<V>();

        int graphSize = 0;
        int cliques = 0;
        int cliqueSizeR = (int) (n * cliqueSize);

        // create a graph with the cliques
        while (graphSize < n) {

            int newCliqueSize = Math.min(cliqueSizeR, n - graphSize);

            // generate the clique as a random graph
            new ErdosRenyiGraphGenerator<V, E>(newCliqueSize, cliqueDensity, null).generateGraph(veGraph, vVertexFactory, stringVMap);

            // find the new nodes generated
            List<V> newNodes = new ArrayList<V>();
            for (V node : veGraph.vertexSet()) {
                if (prevNodes.contains(node))
                    continue;
                prevNodes.add(node);
                newNodes.add(node);
            }

            // cache this clique's nodes.
            cliqueNodesMap.put(cliques, newNodes);

            graphSize = veGraph.vertexSet().size();
            cliques++;
        }

        // connect the cliques
        for (int i = 0; i < cliques; i++) {

            List<V> cliqueNodes = cliqueNodesMap.get(i);

            List<V> otherNodes = new ArrayList<V>(veGraph.vertexSet());
            otherNodes.removeAll(cliqueNodes);

            for (V cNode : cliqueNodes) {

                for (V oNode : otherNodes) {

                    if (veGraph.containsEdge(cNode, oNode))
                        continue;

                    if (random.nextDouble() < cliqueConnectDensity) {
                        veGraph.addEdge(cNode, oNode);
                    }
                }
            }
        }
    }
}
