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

/**
 * Representation of a pair of vertices.
 *
 * @author Soren <soren@tanesha.net>
 */
public class VertexPair<V> {

    private V n1;
    private V n2;

    public VertexPair(V n1, V n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public V getFirst() {
        return n1;
    }

    public V getSecond() {
        return n2;
    }

    /**
     * Assess if this pair contains the vertex.
     * @param v The vertex in question
     * @return true if contains, false otherwise
     */
    public boolean hasVertex(V v) {
        return v.equals(n1) || v.equals(n2);
    }

    public V getOther(V one) {
        if (one.equals(n1))
            return n2;
        else if (one.equals(n2))
            return n1;
        else
            return null;
    }

    @Override
    public String toString() {
        return n1 + "," + n2;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VertexPair that = (VertexPair) o;

        if (n1.equals(that.n1) && n2.equals(that.n2))
            return true;
        else if (n1.equals(that.n2) && n2.equals(that.n1))
            return true;
        else
            return false;
    }

    @Override
    public int hashCode() {
        return (n1.hashCode() + n2.hashCode()) ^ 31;
    }
}
