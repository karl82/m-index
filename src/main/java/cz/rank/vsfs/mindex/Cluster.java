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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NotThreadSafe
public class Cluster<D extends Distanceable<D>> {
    private final int[] indexes;
    private final int calculatedIndex;
    private final Pivot<D> basePivot;
    private final int pivotsCount;
    private final Map<D, Double> objects = new HashMap<>();
    private boolean normalized = false;
    private double maxDistance = 0.0d;
    private final Set<Cluster<D>> subClusters;

    public Cluster(Pivot<D> basePivot, int pivotsCount, int[] indexes) {
        this.basePivot = basePivot;
        this.pivotsCount = pivotsCount;
        this.indexes = indexes;
        this.calculatedIndex = calculateIndex();
        subClusters = new HashSet<>(this.pivotsCount);
    }

    public int getIndex() {
        return calculatedIndex;
    }

    private int calculateIndex() {
        int tempIndex = 0;
        for (int i = 0; i < indexes.length; i++) {
            tempIndex += indexes[i] * FastMath.pow(pivotsCount, indexes.length - 1 - i);
        }
        return tempIndex;
    }

    /**
     * Returns base pivot used for distance calculations
     *
     * @return base pivot
     */
    public Pivot<D> getBasePivot() {
        return basePivot;
    }

    /**
     * Adds object into cluster
     *
     * @param object
     */
    public void add(D object) {
        double distance = basePivot.distance(object);
        objects.put(object, distance);

        maxDistance = FastMath.max(maxDistance, distance);
    }

    /**
     * Normalize distances in cluster to have range [0, 1).
     * <p/>
     * Can be called only once
     */
    public void normalizeDistances() {
        if (isNormalized()) {
            throw new IllegalStateException("Cluster is already normalized: " + this);
        }

        if (maxDistance > 0d) {
            for (Map.Entry<D, Double> entry : objects.entrySet()) {
                objects.put(entry.getKey(), entry.getValue() / maxDistance);
            }
        }

        normalized = true;
    }

    private boolean isNormalized() {
        return normalized;
    }

    public double getKey(D object) {
        if (isNotNormalized()) {
            throw new IllegalStateException("Cluster is not yet normalized: " + this);
        }

        final Double objectDistance = objects.get(object);
        if (objectDistance == null) {
            throw new IllegalArgumentException("Object: " + object + " was not found in this cluster:" + this);
        }

        return objectDistance + getIndex();
    }

    private boolean isNotNormalized() {
        return !isNormalized();
    }

    public int[] getIndexes() {
        return indexes;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cluster [indexes=").append(Arrays.toString(indexes)).append(", objects=").append(objects)
               .append("]");
        return builder.toString();
    }

    public Set<D> getObjects() {
        return Collections.unmodifiableSet(objects.keySet());
    }

    public void addSubClusters(Collection<Cluster<D>> subClusters) {
        this.subClusters.addAll(subClusters);
    }

    public Set<Cluster<D>> getSubClusters() {
        return subClusters;
    }
}
