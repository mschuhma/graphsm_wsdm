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

package dk.aaue.sna.ext.pajek;

import dk.aaue.sna.ext.graphml.AttributeProvider;
import dk.aaue.sna.ext.graphml.AttributeSetter;
import dk.aaue.sna.ext.graphml.GraphMLExporter;
import org.jgrapht.VertexFactory;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.Test;

import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Set;

import static junit.framework.Assert.assertEquals;

/**
 * @author Soren <soren@tanesha.net>
 */
public class PajekImporterTest {

    PajekImporter<String, DefaultWeightedEdge> impl;

    @Test
    public void testGetNetworks() throws Exception {
        impl = PajekImporter.createImporter(new InputStreamReader(this.getClass().getResourceAsStream("/EIES-simple.paj")));

        Set<String> networks = impl.getNetworks();

        System.out.println("networks: " + networks);

        assertEquals(3, networks.size());

        final SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> G = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        VertexFactory<String> vf = new VertexFactory<String>() {
            int i = 0;
            @Override
            public String createVertex() {
                return "v" + (i++);
            }
        };

        impl.selectNetwork("Messages->_default");
        impl.generateGraph(G, vf, null);

        assertEquals(32, G.vertexSet().size());
        assertEquals(440, G.edgeSet().size());

        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        for (DefaultWeightedEdge e : G.edgeSet()) {
            double w = G.getEdgeWeight(e);
            min = Math.min(w, min);
            max = Math.max(w, max);
        }
        double max_min = max - min;
        if (Double.compare(max_min, 0.0) == 0)
            max_min = 1.0;

        for (DefaultWeightedEdge e : G.edgeSet()) {
            double w = G.getEdgeWeight(e);
            G.setEdgeWeight(e, 1.0 - ((w - min) / max_min));
        }

        StringWriter sw = new StringWriter();
        GraphMLExporter<String, DefaultWeightedEdge> exporter = new GraphMLExporter<String, DefaultWeightedEdge>();
        exporter.edgeAttributeProvider(new AttributeProvider<DefaultWeightedEdge>() {
            @Override
            public void provide(DefaultWeightedEdge obj, AttributeSetter setter) {
                setter.set(Double.class, "weight", G.getEdgeWeight(obj));
            }
        });
        exporter.export(sw, G);

        FileWriter fw = new FileWriter("EIES.xml");
        fw.write(sw.toString());
        fw.close();;
    }

    @Test
    public void testGenerateGraph() throws Exception {

        SimpleDirectedWeightedGraph<String, DefaultWeightedEdge> G = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
        VertexFactory<String> vf = new VertexFactory<String>() {
            int i = 0;
            @Override
            public String createVertex() {
                return "v" + (i++);
            }
        };

        impl = PajekImporter.createImporter(new InputStreamReader(this.getClass().getResourceAsStream("/EIES.paj")));
        impl.generateGraph(G, vf, null);

        assertEquals(48, G.vertexSet().size());
        assertEquals(695, G.edgeSet().size());

        System.out.println("G = " + G);
    }
}
