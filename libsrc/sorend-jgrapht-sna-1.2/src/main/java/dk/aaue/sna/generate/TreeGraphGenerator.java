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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates a tree with a fixed number of leaves for each branch.
 *
 * @author Soren <soren@tanesha.net>
 */
public class TreeGraphGenerator<V, E> implements GraphGenerator<V, E, V> {

    protected int leafs;
    private int n;

    public TreeGraphGenerator(int n, int leafs) {
        this.n = n;
        this.leafs = leafs;
    }

    /**
     *
     * @param graph the graph to populate
     * @param vVertexFactory a vertex factory
     * @param stringTMap not used
     */
    @Override
    public void generateGraph(Graph<V, E> graph, VertexFactory<V> vVertexFactory, Map<String, V> stringTMap) {

        if (n < 1) {
            return;
        }

        List<V> queue = new ArrayList<V>();

        V root = vVertexFactory.createVertex();
        graph.addVertex(root);

        queue.add(root);

        int i = 1;
        while (i < n) {
            int toAdd = Math.min(leafs, n - i);

            V parent = queue.remove(0);

            for (int j = 0; j < toAdd; j++) {

                V newNode = vVertexFactory.createVertex();

                graph.addVertex(newNode);
                graph.addEdge(parent, newNode);

                queue.add(newNode);
                i++;
            }
        }
    }
}
