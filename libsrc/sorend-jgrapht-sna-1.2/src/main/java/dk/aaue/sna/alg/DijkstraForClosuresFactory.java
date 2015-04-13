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

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.graph.GraphPathImpl;

import java.util.*;

/**
 * Factory to hold a specific configuraiton of the dijkstraforclosures.
 *
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class DijkstraForClosuresFactory<V, E> {

    public static <V, E> DijkstraForClosuresFactory<V, E> newFactory(Comparator<Double> costComparator, PathCostCalculator<V, E> pathCostCalculator, double initialValue) {
        return new DijkstraForClosuresFactory<V, E>(costComparator, pathCostCalculator, initialValue);
    }

    private Comparator<Double> costComparator;
    private PathCostCalculator<V, E> pathCostCalculator;
    private double initialValue;

    private DijkstraForClosuresFactory(Comparator<Double> costComparator, PathCostCalculator<V, E> pathCostCalculator, double initialValue) {
        this.costComparator = costComparator;
        this.pathCostCalculator = pathCostCalculator;
        this.initialValue = initialValue;
    }

    public DijkstraForClosures<V, E> create(Graph<V, E> graph, V source) {
        return new DijkstraForClosures<V, E>(graph, costComparator, pathCostCalculator, initialValue, source);
    }
}
