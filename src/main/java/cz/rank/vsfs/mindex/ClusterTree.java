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
import org.apache.commons.math3.util.FastMath;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final List<D> objects;
    private final Cluster<D> root = new RootCluster<>(0);
    private final BPlusTreeMap<Double, D> btreemap = new BPlusTreeMap<>(500);
    private PivotDistanceTable<D> pivotDistanceTable = null;
    private double maximumDistance;

    public ClusterTree(int maxLevel, int leafClusterCapacity, Collection<Pivot<D>> pivots) {
        this.maxLevel = maxLevel;
        this.leafClusterCapacity = leafClusterCapacity;
        this.pivots = pivots;

        checkParams();

        // Save reallocation
        objects = new ArrayList<>(minimalExpectedObjects());
    }

    private int minimalExpectedObjects() {
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

    public void add(D point) {
        objects.add(point);
    }

    public void build() {
        calculateMaximumDistance();
        calculateDistances();

        for (D object : objects) {
            Cluster<D> currentCluster = root;
            for (int currentLevel = 0; currentLevel < maxLevel; currentLevel++) {
                final Pivot<D> pivot = pivotDistanceTable.getPivotAt(object, currentLevel);

                currentCluster = currentCluster.getOrCreateSubCluster(pivot);
            }
        }
    }

    private void calculateMaximumDistance() {
        maximumDistance = new MaximumDistance<D>(objects).calculate();
    }

    private void calculateDistances() {
        pivotDistanceTable = new PivotDistanceTable<D>(maximumDistance, pivots, objects);
        pivotDistanceTable.calculate();
    }

    public Collection<D> rangeQuery(D queryObject, double range) {
        final Set<D> foundObjects = new HashSet<>();

        final int pivotsSize = pivots.size();

        final Collection<ClusterPivotDistance<D>> pivotsPermutation = new PriorityQueue<>(pivotsSize);
        final Map<Pivot<D>, ClusterPivotDistance<D>> pivotsDistances = new HashMap<>(pivotsSize);
        for (Cluster<D> cluster : root.getSubClusters()) {
            final ClusterPivotDistance<D> currentDistance = new ClusterPivotDistance<>(cluster, queryObject);

            pivotsPermutation.add(currentDistance);
            pivotsDistances.put(cluster.getBasePivot(), currentDistance);
        }

        final Queue<Cluster<D>> nodeQueue = new ArrayDeque<>();

        nodeQueue.add(root);

        int currentLevel = 0;
        while (nodeQueue.isEmpty()) {
            final Cluster<D> node = nodeQueue.poll();

            if (currentLevel > 0) {
                continue;

            }

            if (node instanceof InternalCluster) {
                nodeQueue.addAll(node.getSubClusters());
            } else {
                final LeafCluster<D> leafCluster = (LeafCluster) node;
                final double keyMin = leafCluster.getKeyMin();
                final double keyMax = leafCluster.getKeyMax();
                final double rMin = frac(keyMin);
                final double rMax = frac(keyMax);
                final double distance = leafCluster.getBasePivot().distance(queryObject);

                if (distance + range < rMin || distance - range > rMax) {
                    continue;
                }

                final double keyMinFloor = FastMath.floor(keyMin);
                final Collection<D> objects = btreemap
                        .rangeSearch(keyMinFloor + distance - range, keyMinFloor + distance + range);

                for (D object : objects) {
                }
            }
        }
        return foundObjects;
    }

    private double frac(double x) {
        return x - FastMath.floor(x);
    }
}
