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

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Builds multilevel cluster
 */
public class MultiLevelClusterBuilder<D extends Distanceable<D>> {
    private final int level;
    private Set<Pivot<D>> pivots;
    private Set<D> points;

    /**
     * Builder for multi-level cluster with level of {@code level}
     *
     * @param level of cluster. Minimum level is 1
     */
    public MultiLevelClusterBuilder(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("Cluster level must be > 0. Current level: " + level);
        }

        this.level = level;
    }

    public MultiLevelClusterBuilder<D> fromPoints(Set<D> points) {
        checkPoints(points);

        this.points = new HashSet<>(points);

        return this;
    }

    /**
     * Checks validity of points from which should be the cluster build
     *
     * @param points
     * @throws IllegalArgumentException if points are null or empty
     */
    private void checkPoints(Set<D> points) {
        if (points == null) {
            throw new IllegalArgumentException("Points are null");
        }

        if (points.isEmpty()) {
            throw new IllegalArgumentException("At least one point must be set");
        }
    }

    public MultiLevelClusterBuilder<D> withPivots(Set<Pivot<D>> pivots) {
        checkPivots(pivots);

        this.pivots = new HashSet<>(pivots);

        return this;
    }

    /**
     * Checks validity of pivots from which should be the cluster build
     *
     * @param pivots
     * @throws IllegalArgumentException if pivots are null or empty
     */
    private void checkPivots(Set<Pivot<D>> pivots) {
        if (pivots == null) {
            throw new IllegalArgumentException("Pivots are null");
        }

        if (pivots.isEmpty()) {
            throw new IllegalArgumentException("At least one pivot must be set");
        }
    }

    public Collection<Cluster<D>> build() {
        doChecks();

        final Map<Pivot<D>, Cluster<D>> clusters = new HashMap<>(pivots.size());

        for (Pivot<D> pivot : pivots) {
            clusters.put(pivot, new Cluster<>(pivot, pivots.size(), new int[]{pivot.getIndex()}));
        }

        new PointsIntoClusterDivider<>(clusters, pivots, points).divide();


        int currentLevel = 1;

        if (currentLevel < level) {
            final ForkJoinPool forkJoinPool = new ForkJoinPool();
            final SubClusterBuildTask.Builder<D> builder = new SubClusterBuildTask.Builder<D>().levels(currentLevel + 1,
                                                                                                       level);
            final Collection<SubClusterBuildTask<D>> tasks = new ArrayList<>(clusters.size());

            for (Map.Entry<Pivot<D>, Cluster<D>> entry : clusters.entrySet()) {
                if (!entry.getValue().getObjects().isEmpty()) {
                    final Set<Pivot<D>> subClusterPivots = new HashSet<>(pivots);
                    subClusterPivots.remove(entry.getKey());

                    SubClusterBuildTask<D> buildTask = builder.pivots(pivots.size(), subClusterPivots)
                                                              .cluster(entry.getValue()).build();
                    tasks.add(buildTask);
                    forkJoinPool.invoke(buildTask);
                }
            }

            for (RecursiveAction action : tasks) {
                action.join();
            }
        }
        return clusters.values();
    }

    /**
     * Performs validity checks for entered data
     */
    private void doChecks() {
        checkPoints(points);
        checkPivots(pivots);
    }

    /**
     * Level of clustering for builder
     *
     * @return the level
     */
    public int getLevel() {
        return level;
    }
}
