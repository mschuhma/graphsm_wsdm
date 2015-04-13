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

import junit.framework.TestCase;
import org.jgrapht.WeightedGraph;
import org.jgrapht.generate.LinearGraphGenerator;
import org.jgrapht.generate.RingGraphGenerator;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.junit.Test;

import java.util.List;

import static dk.aaue.sna.alg.JGraphTTests.generate;

/**
 * @author Soren <soren@tanesha.net>
 */
public class AllPathsTest extends TestCase {

    @Test
    public void testWater() {
        // do nothing
    }

    /*
    public void testCalculate_01_Linear() {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new LinearGraphGenerator(10));

        AllPaths<String, DefaultWeightedEdge> ap = new AllPaths<String, DefaultWeightedEdge>(g);

        List<List<String>> r = ap.calculate("n1", "n5");

        assertEquals(1, r.size());
        assertEquals(5, r.get(0).size());

    }

    public void testCalculate_02_Circle() {

        WeightedGraph<String, DefaultWeightedEdge> g = generate(new RingGraphGenerator(10));

        AllPaths<String, DefaultWeightedEdge> ap = new AllPaths<String, DefaultWeightedEdge>(g);

        List<List<String>> r = ap.calculate("n1", "n6");

        System.out.println("r = " + r);

        assertEquals(2, r.size());
        assertEquals(6, r.get(0).size());
        assertEquals(6, r.get(1).size());

    }
    */
}
