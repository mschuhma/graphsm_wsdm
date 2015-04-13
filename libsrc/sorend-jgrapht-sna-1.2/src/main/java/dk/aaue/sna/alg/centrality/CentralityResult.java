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

import dk.aaue.sna.util.MapSortingHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Representation of a centrality result.
 *
 * @param <V> graph node type
 * @author Soren A. Davidsen <soren@tanesha.net>
 */
public class CentralityResult<V> {

	private Map<V, Double> raw;
	private List<Entry<V, Double>> sorted = null;
	private List<V> sortedNodes = null;
	private boolean inverse;

	public CentralityResult(Map<V, Double> raw, boolean inverse) {
		this.inverse = inverse;
		this.raw = raw;
	}

    /**
     * Get result for a node
     * @param v the node
     * @return the result value
     */
    public Double get(V v) {
        return raw.get(v);
    }

    /**
     * Access to the map of results.
     * @return the map
     */
	public Map<V, Double> getRaw() {
		return raw;
	}

    /**
     * Get a sorted version of the entries and their values.
     * @return the entries, first is most central
     */
	public List<Entry<V, Double>> getSorted() {
		if (sorted == null) {
			sorted = MapSortingHelper.sortedListD(raw);
			if (!inverse)
				Collections.reverse(sorted);
		}
		return sorted;
	}

    /**
     * Get a sorted version of the nodes only.
     * @return the nodes, first is most central
     */
	public List<V> getSortedNodes() {
		if (sortedNodes == null) {
			sortedNodes = MapSortingHelper.stripValues(getSorted());
		}
		return sortedNodes;
	}

    /**
     * Inverse results are results where the first entry is the one with the highest centrality value.
     * @return if this is inverse or not
     */
	public boolean isInverse() {
		return inverse;
	}

	@Override
	public String toString() {
		return "[CentralityResult " + getSorted() + "]";
	}
}
