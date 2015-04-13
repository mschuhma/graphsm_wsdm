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

import dk.aaue.sna.util.JGraphTUtil;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomGraphBuilderTest {

    @Test
	public void testCircleGraph() {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new RingGraphGenerator(10));

		assertEquals(10, g.vertexSet().size());
		assertEquals(10, g.edgeSet().size());

		// TODO: check that it is a circle
		System.out.println("Circle Graph = " + g);
	}

    @Test
	public void testFixedEdges() {
		// 100 nodes network
		// total number of edges; 4950, 2475

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new FixedNumberOfRandomEdgesGraphGenerator(100, 2475, null));

		assertEquals(100, g.vertexSet().size());
		assertEquals(2475, g.edgeSet().size());

		assertEquals(0.50, JGraphTUtil.density(g), 0.001);
	}

	// test if the average density is correct...
    @Test
	public void testERDensity() {

		double sum = 0;
		for (int i = 0; i < 1000; i++) {
            WeightedGraph<String, DefaultWeightedEdge> g = generate(new SparseErdosRenyiGraphGenerator(100, 0.10, 1L));
			sum += JGraphTUtil.density(g);
		}

		// allow [0.09, 0.11]
		assertEquals(0.10, sum / 1000, 0.01);
		System.out.println("avg = " + (sum / 1000));
	}

    @Test
	public void testSparseErdosRenyi() {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new SparseErdosRenyiGraphGenerator(1000, 0.01, 1L));

		assertEquals(1000, g.vertexSet().size());

		// System.out.println("g = " + g);

		// check expected number of edges within a range
		// n*(n-1)/2 = 1000*999/2 = 499500, * 0.01 = 4995, +/- 100 = [4895, 5095]
		assertTrue(g.edgeSet().size() >= 4895);
		assertTrue(g.edgeSet().size() <= 5095);
	}

    @Test
	public void testDenseErdosRenyi() {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new DenseErdosRenyiGraphGenerator(10, 0.1, 1L));

		assertEquals(10, g.vertexSet().size());

		System.out.println("g = " + g);

        g = generate(new DenseErdosRenyiGraphGenerator(10, 1.0, 1L));

		assertEquals(10, g.vertexSet().size());

		System.out.println("g = " + g);

        g = generate(new DenseErdosRenyiGraphGenerator(100, 0.3, 1L));

		assertEquals(100, g.vertexSet().size());

		// System.out.println("g = " + g);

	}

}
