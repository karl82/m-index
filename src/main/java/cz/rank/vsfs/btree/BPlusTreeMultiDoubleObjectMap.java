/*
 * Copyright © 2012 Karel Rank All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *  Neither the name of Karel Rank nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 */
package cz.rank.vsfs.btree;

import java.util.List;

/**
 * @author rank
 */
public class BPlusTreeMultiDoubleObjectMap<V> {
    private final int degree;
    private DoubleObjectNode<V> root;

    /**
     * Construct B+Tree with degree
     *
     * @param degree
     */
    public BPlusTreeMultiDoubleObjectMap(final int degree) {
        this.degree = degree;

        root = new LeafDoubleObjectNode<>(degree);
    }

    /**
     * Degree of this B+Tree
     *
     * @return the degree
     */
    public final int getDegree() {
        return degree;
    }

    /**
     * @param key
     */
    public void insert(final double key, V value) {
        final DoubleObjectNode<V> r = root;

        if (r.isFull()) {
            final InternalDoubleObjectNode<V> s = new InternalDoubleObjectNode<>(getDegree());

            root = s;
            s.setChild(0, r);

            r.splitChild(s, 0);
            s.insertNonFull(key, value);
        } else {
            r.insertNonFull(key, value);
        }
    }

    /**
     * @param key
     * @return first matching item
     */
    public V search(double key) {
        return root.search(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return root.toString();
    }

    public List<V> rangeSearch(double from, double to) {
        doCheckRange(from, to);

        return root.rangeSearch(from, to);
    }

    private void doCheckRange(double from, double to) {
        if (from > to) {
            throw new IllegalArgumentException("From: " + from + " cannot be greater than to: " + to);
        }
    }

    public String toGraph() {
        final DotDoubleObjectNodeVisitor<V> visitor = new DotDoubleObjectNodeVisitor<>();
        root.accept(visitor);

        return visitor.getGraphDefinition();
    }
}
