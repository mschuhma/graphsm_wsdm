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

import org.jgrapht.VertexFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A node which can contain properties. This node is used in a few places where we need a specific node implementation. It can hold
 * properties of any type, so it is a very general node container.
 *
 * @author Soren <soren@tanesha.net>
 */
public class PropertyNode implements PropertyContainer {

    public static PropertyNodeContinousFactory FACTORY() {
        return new PropertyNodeContinousFactory();
    }

    public static class PropertyNodeContinousFactory implements VertexFactory<PropertyNode> {
        private long n;

        public PropertyNodeContinousFactory() {
            this(1);
        }

        public PropertyNodeContinousFactory(long start) {
            this.n = start;
        }

        @Override
        public PropertyNode createVertex() {
            return new PropertyNode(n++);
        }
    }

    private Map<String, Object> attr = new HashMap<String, Object>();
    private Long id;

	public PropertyNode(long id) {
        this.id = id;
	}

    @Override
    public <T> T getProperty(Class<T> clazz, String key) {
        return (T) attr.get(key);
    }

    @Override
    public <T> void setProperty(String key, T property) {
        attr.put(key, property);
    }

    @Override
    public Iterable<String> propertyNames() {
        return attr.keySet();
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		else if (!(obj instanceof PropertyNode))
			return false;
		else {
			PropertyNode that = (PropertyNode) obj;
            return this.id.equals(that.id);
		}
	}

	@Override
	public int hashCode() {
		return this.id.hashCode() ^ 414213;
	}
}
