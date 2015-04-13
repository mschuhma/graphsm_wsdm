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

package dk.aaue.sna.ext.graphml;

import dk.aaue.sna.util.StringContinousFactory;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.junit.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class GraphMLImporterTest {

    GraphMLImporter<String, DefaultEdge> impl;

    @Test
    public void testCreate() throws Exception {

        String graphML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\"><key id=\"d0\" for=\"node\" attr.name=\"label\" attr.type=\"string\"></key><graph id=\"G\" edgedefault=\"directed\"><node id=\"n0\"><data key=\"d0\">hello</data></node><node id=\"n1\"><data key=\"d0\">world</data></node><node id=\"n2\"><data key=\"d0\">universe</data></node><edge id=\"e0\" source=\"n0\" target=\"n1\"></edge><edge id=\"e1\" source=\"n0\" target=\"n2\"></edge></graph></graphml>";
        StringReader sr = new StringReader(graphML);

        impl = GraphMLImporter.create(sr);

        impl.nodeAttributeHandler(new AttributeHandler<String>() {
            @Override
            public void handle(String obj, String id, AttributeGetter getter) {
                System.out.println("label: " + getter.has(String.class, "label"));
                System.out.println("label: " + getter.get(String.class, "label"));
                System.out.println("id: " + id);
            }
        });

        Graph<String, DefaultEdge> g = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
        Map<String, String> map = new HashMap<String, String>();
        impl.generateGraph(g, new StringContinousFactory(), map);

        System.out.println("graph=" + g);
    }

    @Test
    public void testReadKrebs() throws Exception {

        impl = GraphMLImporter.createFromClasspathResource("/Krebs-terror2.xml");

        Map<String, String> map = new HashMap<String, String>();
        Graph<String, DefaultEdge> g = new SimpleWeightedGraph<String, DefaultEdge>(DefaultEdge.class);
        impl.generateGraph(g, new StringContinousFactory(), map);

        String ziadNode = map.get("Ziad_Jarrah");
        assertNotNull(ziadNode);

        System.out.println("graph = " + g);
        System.out.println("Ziad = " + ziadNode);

        System.out.println("edges of ziad: " + g.edgesOf(ziadNode));

        assertEquals(5, g.edgesOf(ziadNode).size());
    }
}
