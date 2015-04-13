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

import dk.aaue.sna.ext.graphml.GraphMLExporter;
import dk.aaue.sna.util.GraphBuilder;
import dk.aaue.sna.util.StringContinousFactory;
import org.jgrapht.Graph;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.GraphGenerator;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableUndirectedWeightedGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class JGraphTTests {

    public static DefaultDirectedWeightedGraph<String, DefaultWeightedEdge> emptyDirectedWeighted() {
        return new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    }

    public static WeightedGraph<String, DefaultWeightedEdge> emptyWeighted() {
        return new ListenableUndirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
    }

    public static StringContinousFactory stringFactory(String... optionalPrefix) {
        if (optionalPrefix != null && optionalPrefix.length > 0)
            return new StringContinousFactory(optionalPrefix[0], 1);
        else
            return new StringContinousFactory();
    }

    public static WeightedGraph<String, DefaultWeightedEdge> generate(GraphGenerator generator) {
        WeightedGraph<String, DefaultWeightedEdge> g = emptyWeighted();
        Map<String, String> m = new HashMap();
        generator.generateGraph(g, stringFactory(), m);
        return g;
    }

    public static GraphBuilder<String, DefaultWeightedEdge, String> builder(Graph<String, DefaultWeightedEdge> graph) {
        return new GraphBuilder<String, DefaultWeightedEdge, String>(graph);
    }

    public static <V, E> void save(Graph<V, E> graph, String path) throws IOException {
        // save it, to let us visualize it in Gephi
        GraphMLExporter<V, E> exp = new GraphMLExporter();
        FileWriter fw = new FileWriter(path);
        exp.export(fw, graph);
        fw.close();
    }
}
