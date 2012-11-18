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

import java.lang.reflect.Array;

/**
 * @author rank
 */
public class Node<T extends Comparable<? super T>> {

    boolean leaf = false;
    private int keysCount = 0;
    private final int degree;
    private Node<T>[] children;
    private T[] keys;

    @SuppressWarnings("unchecked")
    public Node(final int degree) {
        if (degree < 2) {
            throw new IllegalArgumentException("Node degree must be at least 2. Current degree: " + degree);
        }

        this.degree = degree;

        children = new Node[maxChildren()];
        keys = (T[]) Array.newInstance(Comparable.class, maxKeys());
    }

    /**
     * @return
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @param i
     * @return
     */
    public Node<T> getChild(final int i) {
        return children[i];
    }

    /**
     * @param i
     * @return
     */
    public T getKey(final int i) {
        return keys[i];
    }

    /**
     * @return
     */
    public int getKeysCount() {
        return keysCount;
    }

    /**
     * @return
     */
    public boolean isFull() {
        return keysCount == maxKeys();
    }

    /**
     * @return
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * @return
     */
    private int maxChildren() {
        return maxKeys() + 1;
    }

    /**
     * @return
     */
    private int maxKeys() {
        return 2 * degree - 1;
    }

    /**
     * @param r
     */
    public void setChild(final int index, final Node<T> r) {
        children[index] = r;
    }

    /**
     * @param j
     * @param key
     */
    public void setKey(final int j, final T key) {
        keys[j] = key;
    }

    /**
     * @param i
     */
    public void setKeysCount(final int keysCount) {
        this.keysCount = keysCount;
    }

    /**
     * @param b
     */
    public void setLeaf(final boolean leaf) {
        this.leaf = leaf;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < getKeysCount(); i++) {
            if (!isLeaf()) {
                builder.append(children[i]).append(',');
            }
            builder.append(keys[i].toString()).append(',');
        }

        if (!isLeaf()) {
            builder.append(children[getKeysCount()]).append(',');
        }
        builder.append("]");
        return builder.toString();
    }

}
