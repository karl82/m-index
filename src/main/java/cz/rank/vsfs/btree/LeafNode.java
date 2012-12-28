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
class LeafNode<K extends Comparable<? super K>, V> extends AbstractNode<K, V> {
    private final List<K> keys;
    private final List<V> values;

    public LeafNode(int degree) {
        super(degree);

        keys = new ArrayList<>(maxKeys());
        values = new ArrayList<>(maxChildren());
    }

    @Override
    public void insertNonFull(K key, V value) {
        final int pos = Collections.binarySearch(keys, key);

        if (pos < 0) {
            keys.add(-pos - 1, key);
            values.add(-pos - 1, value);
        } else {
            keys.set(pos, key);
            values.set(pos, value);
        }
    }

    @Override
    public void splitChild(Node<K, V> parent, int index) {
        final int nodeDegree = getDegree();
        final LeafNode<K, V> z = new LeafNode<>(nodeDegree);

        final List<K> keysForMove = keys.subList(nodeDegree, getKeysCount());
        z.keys.addAll(keysForMove);
        final List<V> valuesForMove = values.subList(nodeDegree, getKeysCount());
        z.values.addAll(valuesForMove);

        keysForMove.clear();
        valuesForMove.clear();

        parent.setChild(index + 1, keys.get(nodeDegree - 1), z);

//        parent.setKey(index, fullNode.getKey(nodeDegree - 1));
//        node.setValue(index, fullNode.getValue(nodeDegree - 1));
    }

    @Override
    public void setChild(int index, K key, Node<K, V> r) {
        throw new UnsupportedOperationException(this.getClass() + " doesn't support children");
    }

    @Override
    public void setChild(int index, Node<K, V> node) {
        throw new UnsupportedOperationException(this.getClass() + " doesn't support children");
    }

    @Override
    public int getKeysCount() {
        return keys.size();
    }

    @Override
    public V search(K key) {
        final int pos = Collections.binarySearch(keys, key);

        return pos < 0 ? null : values.get(pos);
    }

    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < getKeysCount(); i++) {
            builder.append(keys.get(i)).append("->");
            builder.append(values.get(i));

            if (i < getKeysCount() - 1) {
                builder.append('|');
            }
        }

        builder.append("]");

        return builder.toString();
    }
}
