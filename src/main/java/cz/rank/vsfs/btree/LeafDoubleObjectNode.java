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
import java.util.Collections;
import java.util.List;

/**
 * @author Karel Rank
 */
class LeafDoubleObjectNode<V> extends AbstractDoubleObjectNode<V> {
    private final TDoubleList keys;
    private final List<List<V>> values;
    private LeafDoubleObjectNode<V> sibling = null;

    public LeafDoubleObjectNode(int degree) {
        super(degree);

        keys = new TDoubleArrayList(maxKeys(), Double.NaN);
        values = new ArrayList<>(maxChildren());
    }

    @Override
    public void insertNonFull(double key, V value) {
        final int pos = keys.binarySearch(key);

        if (pos < 0) {
            keys.insert(fixBinPos(pos), key);
            final ArrayList<V> values = new ArrayList<>();
            values.add(value);
            this.values.add(fixBinPos(pos), values);
        } else {
            values.get(pos).add(value);
        }
    }

    @Override
    public void splitChild(DoubleObjectNode<V> parent, int index) {
        final int nodeDegree = getDegree();
        final LeafDoubleObjectNode<V> z = new LeafDoubleObjectNode<>(nodeDegree);

        final TDoubleList keysForMove = keys.subList(nodeDegree, getKeysCount());
        z.keys.addAll(keysForMove);
        final List<List<V>> valuesForMove = values.subList(nodeDegree, getKeysCount());
        z.values.addAll(valuesForMove);

        keys.remove(nodeDegree, getKeysCount() - nodeDegree);
        valuesForMove.clear();

        parent.setChild(index + 1, keys.get(nodeDegree - 1), z);

        siblingCorrection(z);
    }

    private void siblingCorrection(LeafDoubleObjectNode<V> z) {
        if (sibling == null) {
            sibling = z;
        } else {
            z.sibling = sibling;
            sibling = z;
        }
    }

    @Override
    public void setChild(int index, double key, DoubleObjectNode<V> r) {
        throwUnsupportedChildren();
    }

    private void throwUnsupportedChildren() {
        throw new UnsupportedOperationException(this.getClass() + " doesn't support children");
    }

    @Override
    public void setChild(int index, DoubleObjectNode<V> node) {
        throwUnsupportedChildren();
    }

    @Override
    public List<V> rangeSearch(double from, double to) {
        final int fromPos = fixBinPos(keys.binarySearch(from));
        final int toPos = fixBinPos(keys.binarySearch(to));

        final List<List<V>> matchedValues = values.subList(fromPos, toPos);
        final List<V> range = new ArrayList<>(matchedValues.size() * 2);

        for (List<V> list : matchedValues) {
            range.addAll(list);
        }
        if (!matchedValues.isEmpty()) {
            range.addAll(siblingRange(from, to));
        }

        return range;
    }

    private List<? extends V> siblingRange(double from, double to) {
        if (sibling == null) {
            return Collections.emptyList();
        } else {
            return sibling.rangeSearch(from, to);
        }
    }

    @Override
    public int getKeysCount() {
        return keys.size();
    }

    @Override
    public V search(double key) {
        final int pos = keys.binarySearch(key);

        return pos < 0 ? null : values.get(pos).get(0);
    }

    @Override
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

    @Override
    public void accept(DoubleObjectNodeVisitor<V> visitor) {
        visitor.enterLeafNode(keys, values, maxKeys());
    }
}
