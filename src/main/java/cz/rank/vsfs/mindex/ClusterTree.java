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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Dynamic M-Index Cluster
 *
 * @author Karel Rank
 */
public class ClusterTree<D extends Distanceable<D>> {
    private static final Logger logger = LoggerFactory.getLogger(ClusterTree.class);

    private final int maxLevel;
    private final int leafClusterCapacity;
    private final Collection<Pivot<D>> pivots;
    private final List<D> objects;
    private final Cluster<D> root;
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
        root = new RootCluster<>(maxLevel, this.pivots.size());
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
            for (int currentLevel = 0; currentLevel < maxLevel; ++currentLevel) {
                final Pivot<D> pivot = pivotDistanceTable.pivotAt(object, currentLevel);

                currentCluster = currentCluster.getOrCreateSubCluster(pivot);
            }

            final double distance = pivotDistanceTable.firstPivotDistance(object);
            final double objectKey = currentCluster.getCalculatedIndex() + distance;

            logger.debug("Inserting into B+Tree key: {}; obj: {}", objectKey, object);

            btreemap.insert(objectKey, object);
            currentCluster.propagateDistance(objectKey);
        }
    }

    private void calculateMaximumDistance() {
        logger.info("Calculating maximum distance...");
        maximumDistance = new MaximumDistance<D>(objects).calculate();
        logger.info("Maximum distance is " + maximumDistance);
    }

    private void calculateDistances() {
        logger.info("Calculating pivots and objects distances...");
        pivotDistanceTable = new PivotDistanceTable<D>(maximumDistance, pivots, objects);
        pivotDistanceTable.calculate();
        logger.info("Finished calculation of pivots and objects distances...");
    }

    public Collection<D> rangeQuery(D queryObject, double range) {
        final Set<D> foundObjects = new HashSet<>();
        final Queue<Cluster<D>> nodeQueue = new ArrayDeque<>();
        final PivotDistanceTable<D> queryObjectPivotDistance = calculateDistanceFor(queryObject);

        nodeQueue.addAll(root.getSubClusters());
        final double normalizedRange = range / maximumDistance;

        while (!nodeQueue.isEmpty()) {
            final Cluster<D> node = nodeQueue.poll();

            if (doublePivotDistanceConstraint(node, queryObjectPivotDistance, queryObject, normalizedRange)) {
                continue;
            }

            if (node instanceof LeafCluster) {
                final LeafCluster<D> leafCluster = (LeafCluster) node;
                final double keyMin = leafCluster.getKeyMin();
                final double keyMax = leafCluster.getKeyMax();
                final double rMin = frac(keyMin);
                final double rMax = frac(keyMax);
                final double distance = queryObjectPivotDistance.firstPivotDistance(queryObject);

                if (rangePivotDistanceConstraint(normalizedRange, rMin, rMax, distance)) {
                    continue;
                }

                final double keyMinFloor = FastMath.floor(keyMin);
                final Collection<D> objects = btreemap
                        .rangeSearch(keyMinFloor + distance - normalizedRange,
                                     keyMinFloor + distance + normalizedRange);

                for (D object : objects) {
                    if (pivotShouldBeFiltered(object, queryObject, queryObjectPivotDistance, normalizedRange)) {
                        continue;
                    }

                    if (queryObject.distance(object) <= normalizedRange) {
                        foundObjects.add(object);
                    }
                }
            } else {
                nodeQueue.addAll(node.getSubClusters());
            }
        }
        return foundObjects;
    }

    private boolean pivotShouldBeFiltered(D object, D queryObject, PivotDistanceTable<D> queryObjectPivotDistance, double range) {
        double maxDistance = 0;
        for (int i = 0; i < pivots.size(); ++i) {
            maxDistance = FastMath.max(maxDistance, FastMath.abs(
                    queryObjectPivotDistance.pivotDistance(queryObject, i) - pivotDistanceTable
                            .pivotDistance(object, i)));
        }

        return maxDistance > range;
    }

    private boolean rangePivotDistanceConstraint(double normalizedRange, double rMin, double rMax, double distance) {
        return distance + normalizedRange < rMin || distance - normalizedRange > rMax;
    }

    private boolean doublePivotDistanceConstraint(Cluster<D> node, PivotDistanceTable<D> queryObjectPivotDistance, D queryObject, double normalizedRange) {
        final int currentLevel = node.getLevel();
        if (currentLevel > 0) {
            final int parentIndex = node.parentIndex();
            final double currentLevelDistance = queryObjectPivotDistance
                    .pivotDistance(queryObject, parentIndex);
            final double smallestDistance = nearestNonConflictingPivot(queryObject, node, queryObjectPivotDistance);
            return (currentLevelDistance - smallestDistance > 2 * normalizedRange);
        }

        return false;
    }

    private double nearestNonConflictingPivot(D queryObject, Cluster<D> node, PivotDistanceTable<D> queryObjectPivotDistance) {
        final int currentLevel = node.getLevel();

        if (currentLevel < 2) {
            return queryObjectPivotDistance.firstPivotDistance(queryObject);
        }

        final Set<Integer> indexes2Level = node.getIndex().indexes2LevelAsSet();
        for (int i = 0; i < pivots.size(); i++) {
            Pivot<D> pivot = queryObjectPivotDistance.pivotAt(queryObject, i);
            if (!indexes2Level.contains(pivot.getIndex())) {
                return queryObjectPivotDistance.distanceAt(queryObject, i);
            }
        }

        return 0;  //To change body of created methods use File | Settings | File Templates.
    }

    private PivotDistanceTable<D> calculateDistanceFor(D queryObject) {
        final PivotDistanceTable<D> queryObjectPivotDistance = new PivotDistanceTable<D>(maximumDistance, pivots,
                                                                                         Arrays.asList(queryObject));
        queryObjectPivotDistance.calculate();

        return queryObjectPivotDistance;
    }

    private double frac(double x) {
        return x - FastMath.floor(x);
    }

    public void addAll(List<D> objects) {
        this.objects.addAll(objects);
    }
}
