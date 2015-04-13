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

import dk.aaue.sna.util.GraphBuilder;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.Test;

import java.io.StringWriter;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class GraphMLExporterTest {


    @Test
    public void testExport() throws Exception {

        GraphMLExporter<String, DefaultEdge> impl = new GraphMLExporter<String, DefaultEdge>();
        // impl.vertexIDProvider(new StringNameProvider<String>());
        impl.vertexAttributeProvider(new AttributeProvider<String>() {
            @Override
            public void provide(String obj, AttributeSetter setter) {
                setter.set(String.class, "label", obj);
            }
        });

        GraphBuilder<String, DefaultEdge, String> builder = new GraphBuilder<String, DefaultEdge, String>(new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class));
        builder.addVertices("hello", "world", "universe")
               .addEdges("hello", "world", "hello", "universe");

        StringWriter sw = new StringWriter();
        impl.export(sw, builder.graph());

        //assertTrue(sw.toString().contains("hello"));
        //assertTrue(sw.toString().contains("world"));
        //assertTrue(sw.toString().contains("universe"));

        System.out.println("graph=" + sw.toString());
    }
}
