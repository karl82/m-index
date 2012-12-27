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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

/**
 */
public class SubClusterBuildTask<D extends Distanceable<D>> extends RecursiveAction {
    private final int currentLevel;
    private final int level;
    private final Cluster<D> cluster;
    private final Set<Pivot<D>> pendingPivots;
    private final int originalPivotsCount;

    public static class Builder<D extends Distanceable<D>> {
        private int currentLevel;
        private int level;
        private Cluster<D> cluster;
        private Set<Pivot<D>> pendingPivots;
        private int originalPivotsCount;

        public Builder() {
        }

        public Builder<D> levels(int currentLevel, int level) {
            this.currentLevel = currentLevel;
            this.level = level;

            return this;
        }

        public Builder<D> cluster(Cluster<D> cluster) {
            this.cluster = cluster;

            return this;
        }

        public Builder<D> pivots(int originalPivotsCount, Set<Pivot<D>> pendingPivots) {
            this.originalPivotsCount = originalPivotsCount;
            this.pendingPivots = pendingPivots;

            return this;
        }

        public SubClusterBuildTask<D> build() {
            return new SubClusterBuildTask<>(this);
        }

    }

    private SubClusterBuildTask(Builder builder) {
        this.currentLevel = builder.currentLevel;
        this.level = builder.level;
        this.cluster = builder.cluster;
        this.originalPivotsCount = builder.originalPivotsCount;
        this.pendingPivots = builder.pendingPivots;
    }

    @Override
    public void compute() {
        final Map<Pivot<D>, Cluster<D>> subClusters = new HashMap<>(pendingPivots.size());

        final int[] clusterIndexes = cluster.getIndexes();
        for (Pivot<D> pivot : pendingPivots) {
            final int[] index = Arrays.copyOf(clusterIndexes, clusterIndexes.length + 1);
            index[index.length - 1] = pivot.getIndex();
            subClusters.put(pivot, new Cluster<>(cluster.getBasePivot(), originalPivotsCount, index));
        }

        final Set<D> points = cluster.getObjects();

        new PointsIntoClusterDivider<>(subClusters, pendingPivots, points).divide();

        cluster.addSubClusters(subClusters.values());
        // Check if we reach desired level of clustering
        if (currentLevel < level) {
            final SubClusterBuildTask.Builder<D> builder = new SubClusterBuildTask.Builder<D>().levels(currentLevel + 1,
                                                                                                       level);
            List<SubClusterBuildTask> buildTasks = new ArrayList<>(pendingPivots.size());
            for (Map.Entry<Pivot<D>, Cluster<D>> entry : subClusters.entrySet()) {
                if (!entry.getValue().getObjects().isEmpty()) {
                    final Set<Pivot<D>> subClusterPivots = new HashSet<>(pendingPivots);
                    subClusterPivots.remove(entry.getKey());


                    buildTasks.add(builder.pivots(originalPivotsCount, subClusterPivots)
                                          .cluster(entry.getValue()).build());
                }
            }

            invokeAll(buildTasks);
        }
    }
}
