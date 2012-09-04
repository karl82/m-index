package cz.rank.vsfs.mindex;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Builds multilevel cluster
 */
public class MultiLevelClusterBuilder<D extends Distanceable<D>> {
    private final int level;
    private Set<Pivot<D>> pivots;
    private Set<D> points;

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

        Map<Pivot<D>, Cluster<D>> clusters = new HashMap<>(pivots.size());

        for (Pivot<D> pivot : pivots) {
            clusters.put(pivot, new Cluster<D>(pivot, pivots.size(), new int[]{pivot.getIndex()}));
        }

        new PointsIntoClusterDivider<D>(clusters, pivots, points).divide();


        int currentLevel = 1;

        if (currentLevel < level) {
            final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                                                                                        .availableProcessors());

            final CountDownLatch signal = new CountDownLatch((int) Math.pow(pivots.size(), level));

            final SubClusterBuildTaskSubmitter<D> submitter = new SubClusterBuildTaskSubmitter<>(executorService, signal,
                                                                                            currentLevel, level);

            submitter.submit(clusters, pivots.size(), pivots);

            try {
                signal.await();
            } catch (InterruptedException e) {
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
