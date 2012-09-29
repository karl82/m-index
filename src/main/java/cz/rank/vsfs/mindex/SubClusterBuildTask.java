package cz.rank.vsfs.mindex;

import java.util.*;
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
        private Set pendingPivots;
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
            return new SubClusterBuildTask<D>(this);
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
            SubClusterBuildTask.Builder<D> builder = new SubClusterBuildTask.Builder<D>().levels(currentLevel + 1,
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
