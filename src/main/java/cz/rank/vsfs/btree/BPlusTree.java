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

/**
 * @author rank
 */
public class BPlusTree<T extends Comparable<T>> {
    public static <T extends Comparable<T>> void splitChild(final Node<T> node, final int index, final Node<T> fullNode) {
        int nodeDegree = fullNode.getDegree();
        Node<T> z = new Node<>(nodeDegree);

        z.leaf = fullNode.leaf;
        z.setKeysCount(nodeDegree - 1);

        for (int j = 0; j < nodeDegree - 1; j++) {
            z.setKey(j, fullNode.getKey(j + nodeDegree));
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
        }

        node.setKey(index, fullNode.getKey(nodeDegree - 1));

        node.setKeysCount(node.getKeysCount() + 1);
    }

    private Node<T> root;

    private final int degree;

    /**
     * Construct B+Tree with degree
     *
     * @param degree
     */
    public BPlusTree(final int degree) {
        this.degree = degree;

        root = NodeFactory.allocateRoot(getDegree());
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
    public void insert(final T key) {
        Node<T> r = root;

        if (r.isFull()) {
            Node<T> s = new Node<>(getDegree());

            root = s;
            s.setLeaf(false);
            s.setKeysCount(0);

            s.setChild(0, r);

            BPlusTree.splitChild(s, 0, r);
            insertNonFull(s, key);
        } else {
            insertNonFull(r, key);
        }
    }

    /**
     * @param s
     * @param key
     */
    private void insertNonFull(final Node<T> s, final T key) {
        int i = s.getKeysCount() - 1;

        if (s.isLeaf()) {
            while (i >= 0 && key.compareTo(s.getKey(i)) < 0) {
                s.setKey(i + 1, s.getKey(i));
                i--;
            }

            s.setKey(i + 1, key);
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

            insertNonFull(s.getChild(i), key);
        }
    }

    /**
     * @param d
     * @return
     */
    public T search(final T d) {
        return searchNode(root, d);
    }

    /**
     * @param node
     * @param d
     * @return
     */
    private T searchNode(final Node<T> node, final T d) {
        int i = 0;
        while (i < node.getKeysCount() && d.compareTo(node.getKey(i)) > 0) {
            i++;
        }

        if (i < node.getKeysCount() && d.equals(node.getKey(i))) {
            return node.getKey(i);
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
}
