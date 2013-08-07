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

import gnu.trove.list.TDoubleList;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karel Rank
 */
class InternalDoubleObjectNode<V> extends AbstractDoubleObjectNode<V> {

    private final List<DoubleObjectNode<V>> children;
    private final TDoubleList keys;

    public InternalDoubleObjectNode(int degree) {
        super(degree);

        children = new ArrayList<>(maxChildren());
        keys = new TDoubleArrayList(maxKeys(), Double.NaN);
    }

    @Override
    public void insertNonFull(double key, V value) {
        int childPos = fixBinPos(keys.binarySearch(key));

        childPos = splitChildIfFull(childPos, key);

        getChild(childPos).insertNonFull(key, value);
    }

    private int splitChildIfFull(int childPos, double key) {
        final DoubleObjectNode<V> child = getChild(childPos);
        if (child.isFull()) {
            child.splitChild(this, childPos);
            if (key > keys.get(childPos)) {
                childPos++;
            }
        }
        return childPos;
    }

    @Override
    public void splitChild(DoubleObjectNode<V> parent, int index) {
        final int nodeDegree = getDegree();
        final InternalDoubleObjectNode<V> z = new InternalDoubleObjectNode<>(nodeDegree);

        final TDoubleList keysForMove = keys.subList(nodeDegree, getKeysCount());
        z.keys.addAll(keysForMove);
        final List<DoubleObjectNode<V>> childrenForMove = children.subList(nodeDegree, children.size());
        z.children.addAll(childrenForMove);

        keys.remove(nodeDegree, getKeysCount() - nodeDegree);
        childrenForMove.clear();

        parent.setChild(index + 1, keys.get(nodeDegree - 1), z);
    }

    /**
     * @param i
     * @return
     */
    public DoubleObjectNode<V> getChild(int i) {
        return children.get(i);
    }

    /**
     * @param node
     */
    @Override
    public void setChild(int index, DoubleObjectNode<V> node) {
        children.add(index, node);
    }

    @Override
    public List<V> rangeSearch(double from, double to) {
        final int pos = fixBinPos(keys.binarySearch(from));

        return getChild(pos).rangeSearch(from, to);
    }

    /**
     * @param key
     * @param node
     */
    @Override
    public void setChild(int index, double key, DoubleObjectNode<V> node) {
        keys.insert(index - 1, key);
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
    public V search(double key) {
        final int pos = fixBinPos(keys.binarySearch(key));

        return getChild(pos).search(key);
    }

    @Override
    public void accept(DoubleObjectNodeVisitor<V> visitor) {
        visitor.enterInternalNode(children, keys, maxKeys());
    }
}
