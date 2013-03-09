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

package cz.rank.vsfs.mindex;

import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@NotThreadSafe
public class InternalCluster<D extends Distanceable<D>> implements Cluster<D> {
    private static final Logger logger = LoggerFactory.getLogger(InternalCluster.class);
    private final Index index;
    private final Cluster<D> parent;
    private final Map<Pivot<D>, Cluster<D>> subClustersMappedToPivots = new HashMap<>();
    private final Collection<D> objects = new HashSet<>();
    private double keyMin = Double.MAX_VALUE;
    private double keyMax = Double.MIN_VALUE;

    public InternalCluster(Cluster<D> parent, Index index) {
        this.parent = parent;
        this.index = index;
    }

    public InternalCluster(Index index) {
        this.index = index;
        parent = null;
    }

    @Override
    public int getCalculatedIndex() {
        return index.getCalculatedIndex();
    }

    @Override
    public void setKey(double key) {
        setMinKey(key);
        setMaxKey(key);
    }

    private void setMaxKey(double distance) {
        keyMax = FastMath.max(distance, keyMax);
    }

    private void setMinKey(double distance) {
        keyMin = FastMath.min(distance, keyMin);
    }

    @Override
    public int getLevel() {
        return getIndex().getLevel();
    }

    @Override
    public void storeSubCluster(Pivot<D> pivot, Cluster<D> cluster) {
        subClustersMappedToPivots.put(pivot, cluster);
    }

    @Override
    public int getObjectsCount() {
        return objects.size();
    }

    @Override
    public Cluster<D> getParent() {
        return parent;
    }

    @Override
    public Collection<D> getObjects() {
        return objects;
    }

    @Override
    public Index getIndex() {
        return index;
    }

    @Override
    public Collection<Cluster<D>> getSubClusters() {
        return subClustersMappedToPivots.values();
    }

    @Override
    public int parentIndex() {
        return getIndex().prevLevelIndex();
    }

    @Override
    public double getKeyMin() {
        return keyMin;
    }

    @Override
    public double getKeyMax() {
        return keyMax;
    }

    @Override
    public void accept(ClusterVisitor<D> visitor) {
        visitor.enterInternalCluster(this);
    }

    @Override
    public int getMaxLevel() {
        return getIndex().getMaxLevel();
    }

    @Override
    public Cluster<D> getSubCluster(Pivot<D> pivot) {
        return subClustersMappedToPivots.get(pivot);
    }

    @Override
    public void addObject(D object) {
        objects.add(object);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("InternalCluster");
        sb.append("{index=").append(index);
        sb.append(", keyMin=").append(keyMin);
        sb.append(", keyMax=").append(keyMax);
        sb.append('}');
        return sb.toString();
    }

}
