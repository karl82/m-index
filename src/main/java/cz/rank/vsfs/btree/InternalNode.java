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

package cz.rank.vsfs.btree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karel Rank
 */
class InternalNode<K extends Comparable<? super K>, V> extends AbstractNode<K, V> {

    private final List<Node<K, V>> children;
    private final List<K> keys;

    @SuppressWarnings("unchecked")
    public InternalNode(int degree) {
        super(degree);

        children = new ArrayList<>(maxChildren());
        keys = new ArrayList<>(maxKeys());
    }

    @Override
    public void insertNonFull(K key, V value) {
        int childPos = fixBinPos(Collections.binarySearch(keys, key));

        childPos = splitChildIfFull(childPos, key);

        getChild(childPos).insertNonFull(key, value);
    }

    private int splitChildIfFull(int childPos, K key) {
        final Node<K, V> child = getChild(childPos);
        if (child.isFull()) {
            child.splitChild(this, childPos);
            if (key.compareTo(keys.get(childPos)) > 0) {
                childPos++;
            }
        }
        return childPos;
    }

    @Override
    public void splitChild(Node<K, V> parent, int index) {
        final int nodeDegree = getDegree();
        final InternalNode<K, V> z = new InternalNode<>(nodeDegree);

        final List<K> keysForMove = keys.subList(nodeDegree, getKeysCount());
        z.keys.addAll(keysForMove);
        final List<Node<K, V>> childrenForMove = children.subList(nodeDegree, children.size());
        z.children.addAll(childrenForMove);

        keysForMove.clear();
        childrenForMove.clear();

        parent.setChild(index + 1, keys.get(nodeDegree - 1), z);
    }

    /**
     * @param i
     * @return
     */
    public Node<K, V> getChild(int i) {
        return children.get(i);
    }

    /**
     * @param node
     */
    @Override
    public void setChild(int index, Node<K, V> node) {
        children.add(index, node);
    }

    @Override
    public List<V> rangeSearch(K from, K to) {
        final int pos = fixBinPos(Collections.binarySearch(keys, from));

        return getChild(pos).rangeSearch(from, to);
    }

    /**
     * @param key
     * @param node
     */
    @Override
    public void setChild(int index, K key, Node<K, V> node) {
        keys.add(index - 1, key);
        children.add(index, node);
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
            builder.append(children.get(i)).append('|');
            builder.append(keys.get(i)).append('|');
        }

        builder.append(children.get(getKeysCount()));
        builder.append("]");

        return builder.toString();
    }

    @Override
    public int getKeysCount() {
        return keys.size();
    }

    @Override
    public V search(K key) {
        final int pos = fixBinPos(Collections.binarySearch(keys, key));

        return getChild(pos).search(key);
    }

    @Override
    public void accept(NodeVisitor<K, V> visitor) {
        visitor.enterInternalNode(children, keys, maxKeys());
    }
}
