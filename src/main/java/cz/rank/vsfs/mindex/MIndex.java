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
 * Created with IntelliJ IDEA.
 * User: karl
 * Date: 09.03.13
 * Time: 20:09
 * To change this template use File | Settings | File Templates.
 */
public abstract class MIndex<D extends Distanceable<D>> {
    private static final Logger logger = LoggerFactory.getLogger(MIndex.class);
    protected final int maxLevel;
    protected final List<Pivot<D>> pivots;
    protected final List<D> objects;
    protected final Cluster<D> clusterRoot;
    protected final BPlusTreeMultiMap<Double, D> btreemap;
    protected final int pivotsSize;
    protected double maximumDistance = Double.MIN_VALUE;
    protected PivotDistanceTable<D> pivotDistanceTable = null;

    protected MIndex(int maxLevel, int btreeLevel, List<Pivot<D>> pivots) {
        super();
        this.maxLevel = maxLevel;
        this.pivots = pivots;

        checkParams();

        pivotsSize = pivots.size();

        // Save reallocation
        objects = new ArrayList<>(50000);
        clusterRoot = new RootCluster<>(maxLevel, pivotsSize);
        btreemap = new BPlusTreeMultiMap<>(btreeLevel);
    }

    protected MIndex(int maxLevel, int btreeLevel, List<Pivot<D>> pivots, double maximumDistance) {
        super();
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

    protected void checkParams() {
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

    public abstract void build();

    protected void calculateMaximumDistance() {
        if (maximumDistance == Double.MIN_VALUE) {
            logger.info("Calculating maximum distance...");
            maximumDistance = new MaximumDistance<>(objects).calculate();
        }

        logger.info("Maximum distance is " + maximumDistance);
    }

    protected void calculateDistances() {
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
