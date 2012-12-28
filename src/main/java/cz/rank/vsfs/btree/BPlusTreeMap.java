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

        root = new LeafNode<>(degree);
    }

/*
    public static <T extends Comparable<T>, V> void splitChild(final InternalNode<T, V> node, final int index, final InternalNode<T, V> fullNode) {
        final int nodeDegree = fullNode.getDegree();
        final InternalNode<T, V> z = new InternalNode<>(nodeDegree);

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
*/

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
            final InternalNode<K, V> s = new InternalNode<>(getDegree());

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
     * @return
     */
    public V search(K key) {
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

}
