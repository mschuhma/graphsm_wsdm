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

package dk.aaue.sna.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.aaue.sna.util.PropertyNode;

public class MapSortingHelper {

	public static <T extends Object, V> List<V> stripValues(List<Entry<V, T>> list) {
		List<V> r = new ArrayList<V>();
		for (Entry<V, ?> e : list) {
			r.add(e.getKey());
		}
		return r;
	}

/*
	public static Double sum(Class<Double> clz, Collection<Double> collection) {
		double sum = 0.0;
		for (Double d : collection) {
			sum += d;
		}
		return sum;
	}

	public static Integer sum(Collection<Integer> collection) {
		int sum = 0;
		for (Integer i : collection) {
			sum += i;
		}
		return sum;
	}

	public static Long sum(Collection<Long> collection) {
		long sum = 0;
		for (Long l : collection) {
			sum += l;
		}
		return sum;
	}
*/

	public static <V> List<Entry<V, Double>> sortedListD(Map<V, Double> map) {

		List<Entry<V, Double>> list = new ArrayList<Entry<V, Double>>();
		list.addAll(map.entrySet());

		Collections.sort(list, CMP_DOUBLE);
		Collections.reverse(list);

		return list;
	}

	public static List<Entry<PropertyNode, Integer>> sortedListI(Map<PropertyNode, Integer> map) {

		List<Entry<PropertyNode, Integer>> list = new ArrayList<Entry<PropertyNode, Integer>>();
		list.addAll(map.entrySet());

		Collections.sort(list, CMP_INTEGER);
		Collections.reverse(list);

		return list;
	}

	public static CompareDouble CMP_DOUBLE = new CompareDouble();
	public static CompareInteger CMP_INTEGER = new CompareInteger();

	public static class CompareDouble<V> implements Comparator<Entry<V, Double>> {
		@Override
		public int compare(Entry<V, Double> o1, Entry<V, Double> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

	public static class CompareInteger implements Comparator<Entry<PropertyNode, Integer>> {
		@Override
		public int compare(Entry<PropertyNode, Integer> o1, Entry<PropertyNode, Integer> o2) {
			return o1.getValue().compareTo(o2.getValue());
		}
	}

}
