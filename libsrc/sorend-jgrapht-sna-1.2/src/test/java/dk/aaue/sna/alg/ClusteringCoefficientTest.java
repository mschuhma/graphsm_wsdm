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

import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.generate.FixedNumberOfRandomEdgesGraphGenerator;
import dk.aaue.sna.util.GraphBuilder;
import dk.aaue.sna.util.JGraphTUtil;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.ScaleFreeGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static dk.aaue.sna.alg.JGraphTTests.*;
import static junit.framework.Assert.assertEquals;

/**
 * @author Soren <soren@tanesha.net>
 */
public class ClusteringCoefficientTest {

    @Test
    public void testCalculateUndirected() throws Exception {

        GraphBuilder<String, DefaultWeightedEdge, String> g = builder(emptyWeighted());

        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addVertices("n4");

        g.addEdges("n1", "n2");
        g.addEdges("n1", "n3");
        g.addEdges("n1", "n4");

        CentralityResult<String> r = new ClusteringCoefficient(g.graph()).calculate();
        assertEquals(0.0, r.getRaw().get("n1"));

        // add one edge between the neighbours
        g.addEdges("n2", "n3");

        CentralityResult<String> r2 = new ClusteringCoefficient(g.graph()).calculate();
        assertEquals(0.33, r2.getRaw().get("n1"), 0.01);

        // add one more between the neighbours
        g.addEdges("n2", "n4");

        CentralityResult<String> r3 = new ClusteringCoefficient(g.graph()).calculate();
        assertEquals(0.66, r3.getRaw().get("n1"), 0.01);

        // add the last possible
        g.addEdges("n3", "n4");

        CentralityResult<String> r4 = new ClusteringCoefficient(g.graph()).calculate();
        assertEquals(1.0, r4.getRaw().get("n1"));

    }

    @Test
    public void testClusteringForWholeSystem() {

        GraphBuilder<String, DefaultWeightedEdge, String> g = builder(emptyWeighted());

        g.addVertices("n1");
        g.addVertices("n2");
        g.addVertices("n3");
        g.addVertices("n4");

        g.addEdges("n1", "n2");
        g.addEdges("n1", "n3");
        g.addEdges("n1", "n4");

        CentralityResult<String> r = new ClusteringCoefficient(g.graph()).calculate();

        System.out.println("r = " + r.getRaw());
        double s = ClusteringCoefficient.globalClusteringCoefficient(r);
        System.out.println("s = " + s);

        // add one edge between the neighbours
        g.addEdges("n2", "n3");

        r = new ClusteringCoefficient(g.graph()).calculate();
        s = ClusteringCoefficient.globalClusteringCoefficient(r);
        System.out.println("r = " + r.getRaw());
        System.out.println("s = " + s);

        // add one more between the neighbours
        g.addEdges("n2", "n4");

        r = new ClusteringCoefficient(g.graph()).calculate();
        s = ClusteringCoefficient.globalClusteringCoefficient(r);
        System.out.println("r = " + r.getRaw());
        System.out.println("s = " + s);

        // add the last possible
        g.addEdges("n3", "n4");

        r = new ClusteringCoefficient(g.graph()).calculate();
        s = ClusteringCoefficient.globalClusteringCoefficient(r);
        System.out.println("r = " + r.getRaw());
        System.out.println("s = " + s);
    }

    @Test
    public void testCompareRandomScaleFree() {

        WeightedGraph<String, DefaultWeightedEdge> sf = generate(new ScaleFreeGraphGenerator(20));

        WeightedGraph<String, DefaultWeightedEdge> rand = generate(new FixedNumberOfRandomEdgesGraphGenerator(20, 19, null));

        CentralityResult<String> sfR = new ClusteringCoefficient(sf).calculate();
        CentralityResult<String> randR = new ClusteringCoefficient(rand).calculate();

        System.out.println("density(sf)=" + JGraphTUtil.density(sf));
        System.out.println("density(er)=" + JGraphTUtil.density(rand));

        System.out.println("sfR = " + sfR.getRaw() + ", global=" + ClusteringCoefficient.globalClusteringCoefficient(sfR));
        System.out.println("randR = " + randR.getRaw() + ", global=" + ClusteringCoefficient.globalClusteringCoefficient(randR));
    }

}
