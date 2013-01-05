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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Dynamic M-Index Cluster
 *
 * @author Karel Rank
 */
public class ClusterTree<D extends Distanceable<D>> {
    private final int maxLevel;
    private final int leafClusterCapacity;
    private final Collection<Pivot<D>> pivots;
    private final Collection<D> points;
    private final Cluster<D> root = new RootCluster<>(0);

    public ClusterTree(int maxLevel, int leafClusterCapacity, Collection<Pivot<D>> pivots) {
        this.maxLevel = maxLevel;
        this.leafClusterCapacity = leafClusterCapacity;
        this.pivots = pivots;

        checkParams();

        // Save reallocation
        points = new ArrayList<>(minimalExpectedPoints());

        createLevelOneClusters();
    }

    private void createLevelOneClusters() {
        for (Pivot<D> pivot : pivots) {
            root.add(new Cluster<>(pivot, pivots.size(), new Index(pivot.getIndex(), maxLevel)));
        }
    }

    private int minimalExpectedPoints() {
        return this.leafClusterCapacity * this.pivots.size();
    }

    private void checkParams() {
        doCheckPivots();
        doCheckMaxLevel();
    }

    private void doCheckPivots() {
        if (pivots == null) {
            throw new NullPointerException("Pivots cannot be null");
        }
        if (pivots.isEmpty()) {
            throw new IllegalArgumentException("Pivots cannot be empty");
        }
    }

    private void doCheckMaxLevel() {
        if (maxLevel < 1) {
            throw new IllegalArgumentException("Maximum cluster level must be greater than 0. Current: " + maxLevel);
        }

        if (maxLevel > pivots.size()) {
            throw new IllegalArgumentException(
                    "Maximum cluster level must be lower than pivots. Current max level: " + maxLevel + ". Pivots: " + pivots);
        }
    }

    public Cluster<D> getCluster(int index) {
        return root.get(index);
    }

    public void add(D point) {
        points.add(point);
    }

    public void build() {
        final Queue<ClusterNode<D>> nodeQueue = new ArrayDeque<>();
        nodeQueue.add(root);

        while (!nodeQueue.isEmpty()) {
            final ClusterNode<D> node = nodeQueue.poll();
            final Map<Pivot<D>, Cluster<D>> clusterMappedToPivots = new HashMap<>(node.getClusters().size());

            for (Cluster<D> cluster : node.getClusters()) {
                clusterMappedToPivots.put(cluster.getBasePivot(), cluster);
            }
            new PointsIntoClusterDivider<D>(clusterMappedToPivots, pivots, points).divide();

        }
    }

    public Collection<D> rangeQuery(D queryObject, double range) {
        final Set<D> founded = new HashSet<>();

        final Collection<ClusterPivotDistance<D>> pivotsPermutation = new PriorityQueue<>(pivots.size());

        for (Cluster<D> pivot : root.getClusters()) {
            final ClusterPivotDistance<D> currentDistance = new ClusterPivotDistance<>(pivot, queryObject);


            pivotsPermutation.add(currentDistance);
        }

        final Queue<ClusterNode<D>> nodeQueue = new ArrayDeque<>();

        nodeQueue.add(root);

        int currentLevel = 0;
        while (nodeQueue.isEmpty()) {
            final ClusterNode<D> node = nodeQueue.poll();

            if (currentLevel > 0) {
                continue;

            }
        }
        return founded;
    }
}
