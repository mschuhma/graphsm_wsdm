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

import dk.aaue.sna.ext.graphml.GraphMLImporter;
import dk.aaue.sna.util.StringContinousFactory;
import fuzzy4j.aggregation.weighted.AIWA;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Soren <soren@tanesha.net>
 */
public class InfluenceIndependenceCentralityTest {

    SimpleGraph<String, DefaultEdge> G;
    InfluenceIndependenceCentrality<String, DefaultEdge> impl;

    @Test
    public void testCalculate() throws Exception {

        G = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        GraphMLImporter importer = GraphMLImporter.createFromClasspathResource("/Krebs-terror2.xml")
                .useNodeIDAsNode();
        Map<String, String> nodeMap = new HashMap();
        importer.generateGraph(G, StringContinousFactory.FACTORY(), nodeMap);

        impl = new InfluenceIndependenceCentrality<String, DefaultEdge>(G, 1.0, new AIWA(0.8));

        CentralityResult<String> r = impl.calculate();
        int i = 1;
        for (String n : r.getSortedNodes()) {
            System.out.printf("%2d. %-30.30s %5.3f\n", i++, n, r.getRaw().get(n));
        }
    }
}
