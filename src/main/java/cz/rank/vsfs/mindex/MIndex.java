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

import cz.rank.vsfs.btree.BPlusTreeMultiMap;
import org.apache.commons.math3.util.FastMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Dynamic M-Index Cluster
 *
 * @author Karel Rank
 */
public class MIndex<D extends Distanceable<D>> {
    private static final Logger logger = LoggerFactory.getLogger(MIndex.class);
    private final int maxLevel;
    private final List<Pivot<D>> pivots;
    private final List<D> objects;
    private final Cluster<D> clusterRoot;
    private final BPlusTreeMultiMap<Double, D> btreemap;
    private final int pivotsSize;
    private PivotDistanceTable<D> pivotDistanceTable = null;
    private double maximumDistance = Double.MIN_VALUE;

    public MIndex(int maxLevel, int btreeLevel, List<Pivot<D>> pivots) {
        this.maxLevel = maxLevel;
        this.pivots = pivots;

        checkParams();

        pivotsSize = pivots.size();

        // Save reallocation
        objects = new ArrayList<>(50000);
        clusterRoot = new RootCluster<>(maxLevel, pivotsSize);
        btreemap = new BPlusTreeMultiMap<>(btreeLevel);
    }

    public MIndex(int maxLevel, int btreeLevel, List<Pivot<D>> pivots, double maximumDistance) {
        this.maxLevel = maxLevel;
        this.pivots = pivots;

        checkParams();

        this.maximumDistance = maximumDistance;
        pivotsSize = pivots.size();


        // Save reallocation
        objects = new ArrayList<>(50000);
        clusterRoot = new RootCluster<>(maxLevel, pivotsSize);
        btreemap = new BPlusTreeMultiMap<>(btreeLevel);
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

        final ClusterTreeBuilder<D> builder = new MultiLevelClusterTreeBuilder<>(objects, clusterRoot, pivotDistanceTable, btreemap);

        builder.build();
    }

    private void calculateMaximumDistance() {
        if (maximumDistance == Double.MIN_VALUE) {
            logger.info("Calculating maximum distance...");
            maximumDistance = new MaximumDistance<>(objects).calculate();
        }

        logger.info("Maximum distance is " + maximumDistance);
    }

    private void calculateDistances() {
        logger.info("Calculating pivots and objects distances...");
        pivotDistanceTable = new ParallelPivotDistanceTable<>(maximumDistance, pivots, objects);
        pivotDistanceTable.calculate();
        logger.info("Finished calculation of pivots and objects distances...");
    }

    public Collection<D> rangeQuery(D queryObject, double range) {
        final double normalizedRange = range / maximumDistance;

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Querying objects which are in range: " + normalizedRange + " from object: " + queryObject);
        }

        final ClusterRangeQuery clusterRangeQuery = new ClusterRangeQuery(queryObject, range, normalizedRange);
        Collection<D> foundObjects = clusterRangeQuery.performQuery();
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Found objects which are in range: " + normalizedRange + " from object: " + queryObject + " objects:" + foundObjects.size());
        }

        return foundObjects;
    }

    public void addAll(List<D> objects) {
        this.objects.addAll(objects);
    }

    public String getTreeGraph() {
        return btreemap.toGraph();
    }

    public String getClusterGraph() {
        final DotClusterVisitor<D> visitor = new DotClusterVisitor<>();
        clusterRoot.accept(visitor);
        return visitor.getGraphDefinition();
    }

    private class ClusterRangeQuery implements ClusterVisitor<D> {
        private final Collection<D> foundObjects = new HashSet<>();
        private final D queryObject;
        private final double range;
        private final double normalizedRange;
        private final Queue<Cluster<D>> clusterQueue = new LinkedList<>();
        private final PivotDistanceTable<D> queryObjectPivotDistance;
        private final double firstPivotDistance;

        private ClusterRangeQuery(D queryObject, double range, double normalizedRange) {
            this.queryObject = queryObject;
            this.range = range;
            this.normalizedRange = normalizedRange;
            queryObjectPivotDistance = calculateDistanceFor(queryObject);
            firstPivotDistance = queryObjectPivotDistance.firstPivotDistance(queryObject);

        }

        @Override
        public void enterInternalCluster(InternalCluster<D> internalCluster) {
            clusterQueue.addAll(internalCluster.getSubClusters());
        }

        @Override
        public void enterLeafCluster(LeafCluster<D> leafCluster) {
            final double keyMin = leafCluster.getKeyMin();
            final double keyMax = leafCluster.getKeyMax();
            final double rMin = frac(keyMin);
            final double rMax = frac(keyMax);

            if (rangePivotDistanceConstraint(normalizedRange, rMin, rMax, firstPivotDistance)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping cluster due rangePivotDistanceConstraint: {}", leafCluster);
                }

                return;
            }

            final List<D> objects = rangeSearchForObjects(keyMin);
            for (D object : objects) {
                if (!pivotShouldBeFiltered(object, queryObject, queryObjectPivotDistance, normalizedRange) &&
                        isObjectInRange(object, queryObject, range)) {
                    foundObjects.add(object);
                }
            }
        }

        private List<D> rangeSearchForObjects(double keyMin) {
            final double keyMinFloor = FastMath.floor(keyMin);
            final List<D> objects = btreemap
                    .rangeSearch(keyMinFloor + firstPivotDistance - normalizedRange,
                            keyMinFloor + firstPivotDistance + normalizedRange);

            if (logger.isDebugEnabled()) {
                logger.debug("Range search from {} to {} returned {} objects",
                        keyMinFloor + firstPivotDistance - normalizedRange,
                        keyMinFloor + firstPivotDistance + normalizedRange, objects.size());
            }

            return objects;
        }

        public Collection<D> performQuery() {
            clusterQueue.addAll(clusterRoot.getSubClusters());
            while (!clusterQueue.isEmpty()) {
                final Cluster<D> cluster = clusterQueue.poll();

                if (doublePivotDistanceConstraint(cluster, queryObjectPivotDistance, queryObject, normalizedRange)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping cluster due doublePivotDistanceConstraint: {}", cluster);
                    }

                    continue;
                }

                cluster.accept(this);
            }

            return foundObjects;
        }

        private PivotDistanceTable<D> calculateDistanceFor(D queryObject) {
            final PivotDistanceTable<D> queryObjectPivotDistance = new SimplePivotDistanceTable<>(maximumDistance, pivots,
                    Arrays.asList(
                            queryObject));
            queryObjectPivotDistance.calculate();

            return queryObjectPivotDistance;

        }

        private boolean isObjectInRange(D object, D queryObject, double range) {
            return queryObject.distance(object) / maximumDistance <= range;
        }

        private boolean pivotShouldBeFiltered(D object, D queryObject, PivotDistanceTable<D> queryObjectPivotDistance, double normalizedRange) {
            double maxDistance = 0;
            boolean shouldBeFiltered = false;
            for (int i = 0; i < pivotsSize && !shouldBeFiltered; ++i) {
                maxDistance = FastMath.max(maxDistance, FastMath.abs(
                        queryObjectPivotDistance.pivotDistance(queryObject, i) - pivotDistanceTable
                                .pivotDistance(object, i)));
                shouldBeFiltered = maxDistance > normalizedRange;
            }

            return shouldBeFiltered;
        }

        private boolean rangePivotDistanceConstraint(double range, double rMin, double rMax, double distance) {
            return distance + range < rMin || distance - range > rMax;
        }

        private boolean doublePivotDistanceConstraint(Cluster<D> node, PivotDistanceTable<D> queryObjectPivotDistance, D queryObject, double range) {
            final int currentLevel = node.getLevel();

            return currentLevel > 0 && doDoublePivotDistanceConstraint(node, queryObjectPivotDistance, queryObject, range);
        }

        private boolean doDoublePivotDistanceConstraint(Cluster<D> node, PivotDistanceTable<D> queryObjectPivotDistance, D queryObject, double range) {
            final int parentIndex = node.parentIndex();
            final double currentLevelDistance = queryObjectPivotDistance
                    .pivotDistance(queryObject, parentIndex);
            final double smallestDistance = nearestNonConflictingPivot(queryObject, node, queryObjectPivotDistance);
            return (currentLevelDistance - smallestDistance > 2 * range);
        }

        private double nearestNonConflictingPivot(D queryObject, Cluster<D> node, PivotDistanceTable<D> queryObjectPivotDistance) {
            final int currentLevel = node.getLevel();

            if (currentLevel < 2) {
                return queryObjectPivotDistance.firstPivotDistance(queryObject);
            }

            final Set<Integer> indexes2Level = node.getIndex().indexes2LevelAsSet();
            for (int i = 0; i < pivotsSize; i++) {
                Pivot<D> pivot = queryObjectPivotDistance.pivotAt(queryObject, i);
                if (!indexes2Level.contains(pivot.getIndex())) {
                    return queryObjectPivotDistance.distanceAt(queryObject, i);
                }
            }

            return 0;
        }

        private double frac(double x) {
            return x - FastMath.floor(x);
        }

    }
}
