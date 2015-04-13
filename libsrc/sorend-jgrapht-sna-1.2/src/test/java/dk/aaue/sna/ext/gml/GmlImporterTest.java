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

package dk.aaue.sna.ext.gml;

import dk.aaue.sna.alg.JGraphTTests;
import junit.framework.TestCase;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class GmlImporterTest extends TestCase {

    public void testParseNodeID() throws Exception {

    }

    public void testParseEdge() throws Exception {
        String edge = "edge\n[\n\t source src\n\t target tgt\n]";

        String srcDst[] = GmlImporter.parseEdge(edge);

        assertEquals("src", srcDst[0]);
        assertEquals("tgt", srcDst[1]);
    }


    public void testGenerateGraph_Zachary() throws Exception {

        Graph<String, DefaultWeightedEdge> G = JGraphTTests.emptyWeighted();

        String s = "Creator \"Mark Newman on Fri Jul 21 12:39:27 2006\"\n" +
                "graph\n" +
                "[\n" +
                "  node\n" +
                "  [\n" +
                "    id 1\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 2\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 3\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 4\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 5\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 6\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 7\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 8\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 9\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 10\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 11\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 12\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 13\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 14\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 15\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 16\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 17\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 18\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 19\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 20\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 21\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 22\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 23\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 24\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 25\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 26\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 27\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 28\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 29\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 30\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 31\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 32\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 33\n" +
                "  ]\n" +
                "  node\n" +
                "  [\n" +
                "    id 34\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 2\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 3\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 3\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 4\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 4\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 4\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 5\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 6\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 7\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 7\n" +
                "    target 5\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 7\n" +
                "    target 6\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 8\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 8\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 8\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 8\n" +
                "    target 4\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 9\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 9\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 10\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 11\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 11\n" +
                "    target 5\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 11\n" +
                "    target 6\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 12\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 13\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 13\n" +
                "    target 4\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 14\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 14\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 14\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 14\n" +
                "    target 4\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 17\n" +
                "    target 6\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 17\n" +
                "    target 7\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 18\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 18\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 20\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 20\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 22\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 22\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 26\n" +
                "    target 24\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 26\n" +
                "    target 25\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 28\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 28\n" +
                "    target 24\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 28\n" +
                "    target 25\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 29\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 30\n" +
                "    target 24\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 30\n" +
                "    target 27\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 31\n" +
                "    target 2\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 31\n" +
                "    target 9\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 32\n" +
                "    target 1\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 32\n" +
                "    target 25\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 32\n" +
                "    target 26\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 32\n" +
                "    target 29\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 3\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 9\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 15\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 16\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 19\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 21\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 23\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 24\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 30\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 31\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 33\n" +
                "    target 32\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 9\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 10\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 14\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 15\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 16\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 19\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 20\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 21\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 23\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 24\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 27\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 28\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 29\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 30\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 31\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 32\n" +
                "  ]\n" +
                "  edge\n" +
                "  [\n" +
                "    source 34\n" +
                "    target 33\n" +
                "  ]\n" +
                "]";

        Map<String, String> map = new HashMap();
        new GmlImporter(s).generateGraph(G, JGraphTTests.stringFactory(), map);

        assertEquals(34, G.vertexSet().size());
        assertEquals(78, G.edgeSet().size());
    }
}
