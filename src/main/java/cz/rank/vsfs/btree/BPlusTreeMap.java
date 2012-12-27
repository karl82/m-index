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
public class BPlusTreeMap<K extends Comparable<K>, V> {
    private final int degree;
    private Node<K, V> root;

    /**
     * Construct B+Tree with degree
     *
     * @param degree
     */
    public BPlusTreeMap(final int degree) {
        this.degree = degree;

        root = Node.allocateRoot(getDegree());
    }

    public static <T extends Comparable<T>, V> void splitChild(final Node<T, V> node, final int index, final Node<T, V> fullNode) {
        final int nodeDegree = fullNode.getDegree();
        final Node<T, V> z = new Node<>(nodeDegree);

        z.leaf = fullNode.leaf;
        z.setKeysCount(nodeDegree - 1);

        for (int j = 0; j < nodeDegree - 1; j++) {
            z.setKey(j, fullNode.getKey(j + nodeDegree));
            z.setValue(j, fullNode.getValue(j + nodeDegree));
        }

        if (!fullNode.isLeaf()) {
            for (int j = 0; j < nodeDegree; j++) {
                z.setChild(j, fullNode.getChild(j + nodeDegree));
            }
        }

        fullNode.setKeysCount(nodeDegree - 1);

        for (int j = node.getKeysCount(); j >= index + 1; j--) {
            node.setChild(j + 1, node.getChild(j));
        }

        node.setChild(index + 1, z);

        for (int j = node.getKeysCount() - 1; j >= index; j--) {
            node.setKey(j + 1, node.getKey(j));
            node.setValue(j + 1, node.getValue(j));
        }

        node.setKey(index, fullNode.getKey(nodeDegree - 1));
        node.setValue(index, fullNode.getValue(nodeDegree - 1));

        node.setKeysCount(node.getKeysCount() + 1);
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
    public void insert(final K key, V value) {
        final Node<K, V> r = root;

        if (r.isFull()) {
            final Node<K, V> s = new Node<>(getDegree());

            root = s;
            s.setLeaf(false);
            s.setKeysCount(0);

            s.setChild(0, r);

            BPlusTreeMap.splitChild(s, 0, r);
            insertNonFull(s, key, value);
        } else {
            insertNonFull(r, key, value);
        }
    }

    /**
     * @param s
     * @param key
     */
    private void insertNonFull(final Node<K, V> s, final K key, final V value) {
        int i = s.getKeysCount() - 1;

        if (s.isLeaf()) {
            while (i >= 0 && key.compareTo(s.getKey(i)) < 0) {
                s.setKey(i + 1, s.getKey(i));
                s.setValue(i + 1, s.getValue(i));
                i--;
            }

            s.setKey(i + 1, key);
            s.setValue(i + 1, value);
            s.setKeysCount(s.getKeysCount() + 1);
        } else {
            while (i >= 0 && key.compareTo(s.getKey(i)) < 0) {
                i--;
            }

            i++;

            if (s.getChild(i).isFull()) {
                splitChild(s, i, s.getChild(i));
                if (key.compareTo(s.getKey(i)) > 0) {
                    i++;
                }
            }

            insertNonFull(s.getChild(i), key, value);
        }
    }

    /**
     * @param d
     * @return
     */
    public V search(final K d) {
        return searchNode(root, d);
    }

    /**
     * @param node
     * @param d
     * @return
     */
    private V searchNode(final Node<K, V> node, final K d) {
        int i = 0;
        while (i < node.getKeysCount() && d.compareTo(node.getKey(i)) > 0) {
            i++;
        }

        if (i < node.getKeysCount() && d.equals(node.getKey(i))) {
            return node.getValue(i);
        }

        if (node.isLeaf()) {
            return null;
        } else {
            return searchNode(node.getChild(i), d);
        }
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

    static class Node<T extends Comparable<? super T>, V> {

        private final int degree;
        private final Node<T, V>[] children;
        private final T[] keys;
        private final V[] values;
        boolean leaf = false;
        private int keysCount = 0;

        @SuppressWarnings("unchecked")
        public Node(final int degree) {
            this.degree = degree;

            doCheckDegree();

            children = new Node[maxChildren()];
            keys = (T[]) Array.newInstance(Comparable.class, maxKeys());
            values = (V[]) new Object[maxKeys()];
        }

        private static <T extends Comparable<T>, V> Node<T, V> allocateRoot(final int degree) {
            final Node<T, V> node = new Node<>(degree);
            node.setLeaf(true);
            node.setKeysCount(0);

            return node;
        }

        /**
         * @throws IllegalArgumentException if {@code degree} is less than 2
         */
        private void doCheckDegree() {
            if (degree < 2) {
                throw new IllegalArgumentException("Node degree must be at least 2. Current degree: " + degree);
            }
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
        public Node<T, V> getChild(final int i) {
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
         */
        public void setKeysCount(final int keysCount) {
            this.keysCount = keysCount;
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
         */
        public void setLeaf(final boolean leaf) {
            this.leaf = leaf;
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
        public void setChild(final int index, final Node<T, V> r) {
            children[index] = r;
        }

        /**
         * @param j
         * @param key
         */
        public void setKey(final int j, final T key) {
            keys[j] = key;
        }

        /*
        * (non-Javadoc)
        *
        * @see java.lang.Object#toString()
        */
        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
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

        public V getValue(int i) {
            return values[i];

        }

        public void setValue(int i, V value) {
            values[i] = value;
        }
    }

}
