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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@NotThreadSafe
public class InternalCluster<D extends Distanceable<D>> implements Cluster<D> {
    private final Index index;
    private final Cluster<D> parent;
    private final Map<Pivot<D>, Cluster<D>> subClustersMappedToPivots = new HashMap<>();
    private double rMin = Double.MAX_VALUE;
    private double rMax = Double.MIN_VALUE;

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
    public void propagateDistance(double distance) {
        rMin = FastMath.min(distance, rMin);
        rMax = FastMath.max(distance, rMax);

        if (parent != null) {
            parent.propagateDistance(distance);
        }
    }

    @Override
    public int getLevel() {
        return getIndex().getLevel();
    }

    @Override
    public Cluster<D> getOrCreateSubCluster(Pivot<D> pivot) {
        Cluster<D> cluster = subClustersMappedToPivots.get(pivot);

        if (cluster == null) {
            if (index.getLevel() + 1 != index.getMaxLevel()) {
                cluster = new InternalCluster<D>(this, index.addLevel(pivot.getIndex()));
            } else {
                cluster = new LeafCluster<D>(this, index.addLevel(pivot.getIndex()));
            }

            subClustersMappedToPivots.put(pivot, cluster);
        }
        return cluster;
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
        return rMin;
    }

    @Override
    public double getKeyMax() {
        return rMax;
    }
}
