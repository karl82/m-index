package cz.rank.vsfs.mindex;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 */
public class SubClusterBuildTask<D extends Distanceable<D>> implements Runnable {
    private final ExecutorService executorService;
    private final CountDownLatch signal;
    private final int currentLevel;
    private final int level;
    private final Cluster<D> cluster;
    private final Set<Pivot<D>> pendingPivots;
    private final int originalPivotsCount;

    public static class Builder<D extends Distanceable<D>> {
        private final ExecutorService executorService;
        private final CountDownLatch signal;
        private int currentLevel;
        private int level;
        private Cluster<D> cluster;
        private Set pendingPivots;
        private int originalPivotsCount;

        public Builder(ExecutorService executorService, CountDownLatch signal) {
            this.executorService = executorService;
            this.signal = signal;
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
            this.pendingPivots = new HashSet<>(pendingPivots);

            return this;
        }

        public SubClusterBuildTask<D> build() {
            return new SubClusterBuildTask<D>(this);
        }

    }

    private SubClusterBuildTask(Builder builder) {
        this.executorService = builder.executorService;
        this.signal = builder.signal;
        this.currentLevel = builder.currentLevel;
        this.level = builder.level;
        this.cluster = builder.cluster;
        this.originalPivotsCount = builder.originalPivotsCount;
        this.pendingPivots = builder.pendingPivots;
    }

    @Override
    public void run() {
        Map<Pivot<D>, Cluster<D>> subClusters = new HashMap<>(pendingPivots.size());

        int[] clusterIndexes = cluster.getIndexes();
        for (Pivot<D> pivot : pendingPivots) {
            int[] index = Arrays.copyOf(clusterIndexes, clusterIndexes.length + 1);
            index[index.length - 1] = pivot.getIndex();
            subClusters.put(pivot, new Cluster<D>(cluster.getBasePivot(), originalPivotsCount, index));
        }

        Set<D> points = cluster.getObjects();

        new PointsIntoClusterDivider<D>(subClusters, pendingPivots, points).divide();

        cluster.addSubClusters(subClusters.values());
        // Check if we reach desired level of clustering
        if (currentLevel < level) {
            new SubClusterBuildTaskSubmitter<D>(executorService, signal, currentLevel + 1, level).submit(subClusters, originalPivotsCount, pendingPivots);
        }

        signal.countDown();
    }
}
