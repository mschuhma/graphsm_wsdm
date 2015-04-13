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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Soren <soren@tanesha.net>
 */
public class FuzzyUtil {

    public static interface NormalizedHandler<V> {
        public void normalized(V element, double value);
    }

    public static <V> void minMaxNormalize(Map<V, Double> map, NormalizedHandler<V> handler) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Double d : map.values()) {
            if (d > max)
                max = d;
            if (d < min)
                min = d;
        }
        double max_min = max - min;
        if (Double.compare(max_min, 0.0) == 0)
            max_min = 1.0;

        for (Entry<V, Double> e : map.entrySet()) {
            handler.normalized(e.getKey(), (e.getValue() - min) / (max_min));
        }
    }

    public static <V> Map<V, Double> minMaxNormalize(Map<V, Double> map) {
        final Map<V, Double> res = new HashMap<V, Double>();
        minMaxNormalize(map, new NormalizedHandler<V>() {
            @Override
            public void normalized(V element, double value) {
                res.put(element, value);
            }
        });
        return res;
    }
}
