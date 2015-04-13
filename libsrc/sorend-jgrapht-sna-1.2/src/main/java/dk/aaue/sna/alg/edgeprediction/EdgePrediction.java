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

package dk.aaue.sna.alg.edgeprediction;

import dk.aaue.sna.alg.VertexPair;

import java.util.Comparator;

/**
 * Representation of a predicted edge. Contains the two nodes, and a "confidence" (val) in the prediction.
 *
 * @author Soren A D <sdavid08@student.aau.dk>
 */
public class EdgePrediction<V> implements Comparable<EdgePrediction<V>> {

    public static class ReverseEdgePredictoinComparator<V> implements Comparator<EdgePrediction<V>> {
        @Override
        public int compare(EdgePrediction<V> o1, EdgePrediction<V> o2) {
            return o2.compareTo(o1);
        }
    }

    public final V v;
    public final V u;
    public final double val;
    public final Object data;

    EdgePrediction(V v, V u, double val, Object data) {
        this.v = v;
        this.u = u;
        this.val = val;
        this.data = data;
    }

    EdgePrediction(V v, V u, double val) {
        this(v, u, val, null);
    }

    public Object getData() {
        return data;
    }

    @Override
    public int compareTo(EdgePrediction<V> o) {
        return Double.compare(val, o.val);
    }

    public String toString() {
        return String.format("Pi(%s, %s) = %f", v, u, val);
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        else if (!(obj instanceof EdgePrediction))
            return false;
        else {
            EdgePrediction p = (EdgePrediction) obj;
            if (v.equals(p.v) && u.equals(p.u))
                return true;
            else if (v.equals(p.u) && u.equals(p.v))
                return true;
            else
                return false;
        }
    }

    @Override
    public int hashCode() {
        return (v.hashCode() + u.hashCode()) ^ 1327;
    }

    public boolean equalsVertexPair(VertexPair<V> vp) {
        if (vp.getFirst().equals(v) && vp.getSecond().equals(u))
            return true;
        else if (vp.getFirst().equals(u) && vp.getSecond().equals(v))
            return true;
        else
            return false;
    }
}
