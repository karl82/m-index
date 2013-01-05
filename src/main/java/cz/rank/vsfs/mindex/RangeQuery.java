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

import cz.rank.vsfs.btree.BPlusTreeMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Karel Rank
 */
public class RangeQuery<D extends Distanceable<D>> {
    private final Collection<Cluster<D>> clusters;
    private final BPlusTreeMap<Double, D> btree;

    public RangeQuery(Collection<Cluster<D>> clusters, BPlusTreeMap<Double, D> btree) {
        this.clusters = clusters;
        this.btree = btree;
    }

    public D query(D point, double range) {
        final List<ClusterPivotDistance<D>> clusterPivotDistances = new ArrayList<>(clusters.size());
        ClusterPivotDistance nearestPivot = ClusterPivotDistance.maxClusterPivotDistance();

        for (Cluster<D> cluster : clusters) {
            final ClusterPivotDistance<D> currentDistance = new ClusterPivotDistance<>(cluster,
                                                                                       point);

            clusterPivotDistances.add(currentDistance);
            if (currentDistance.compareTo(nearestPivot) < 0) {
                nearestPivot = currentDistance;
            }
        }

        final List<Cluster<D>> clustersForProcessing = new ArrayList<>(clusters.size());
        // Double-Pivot distance constraint
        for (ClusterPivotDistance<D> clusterPivotDistance : clusterPivotDistances) {
            if (clusterPivotDistance.getDistance() - nearestPivot.getDistance() <= 2 * range) {
                clustersForProcessing.add(clusterPivotDistance.getCluster());
            }
        }

        return null;
    }

}
