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
import org.jgrapht.generate.CompleteGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static dk.aaue.sna.alg.JGraphTTests.generate;
import static dk.aaue.sna.alg.JGraphTTests.stringFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RandomErrorBuilderTest {

	BorgattiRandomErrorBuilder impl;
	WeightedGraph<String, DefaultWeightedEdge> C;
	WeightedGraph<String, DefaultWeightedEdge> S;

    @Before
	public void setUp() throws Exception {
		impl = new BorgattiRandomErrorBuilder(stringFactory("e"));
		C = generate(new CompleteGraphGenerator(5));
		S = generate(new ErdosRenyiGraphGenerator(10, 0.1, null));
	}

    @Test
	public void testErrorsNodeAdd() {
		// 5 * 5-1 / 2 = 5 * 4 / 2 = 10
		impl.errorsNodeAdd(C, 0.5);
		assertEquals(8, C.vertexSet().size());

		// all must be complete, except the new node, which ahs only 6 edges
		for (String node : C.vertexSet()) {
			System.out.println("deg(" + node + ") = " + C.edgesOf(node).size());
			assertTrue(C.edgesOf(node).size() > 3 && C.edgesOf(node).size() < 8);
		}
	}

    @Test
	public void testErrorsEdgeAdd() {

		try {
			impl.errorsEdgeAdd(C, 0.5);
			// fail("Graph is complete, cant add edges");
		}
		catch (Exception e) {
			assertTrue(true);
		}

		impl.errorsEdgeAdd(S, 0.5);
	}

    @Test
	public void testErrorsEdgeRemove() {

		int edges = 5 * (5 - 1) / 2;
		assertEquals(edges, C.edgeSet().size());
		impl.errorsEdgeRemove(C, 0.5);
		int half = edges / 2;
		assertEquals(half, C.edgeSet().size());
	}

    @Test
	public void testErrorsNodeRemove() {

		System.out.println("C.density = " + JGraphTUtil.density(C));
		System.out.println("S.density = " + JGraphTUtil.density(S));

		// remove 0.8 of the nodes = 1 node left.
		impl.errorsNodeRemove(C, 0.8, new ArrayList<String>());

		assertEquals(1, C.vertexSet().size());
		assertEquals(0, C.edgeSet().size());
	}

}
