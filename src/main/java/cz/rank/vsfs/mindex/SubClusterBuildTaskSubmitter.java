package cz.rank.vsfs.mindex;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 */
public class SubClusterBuildTaskSubmitter<D extends Distanceable<D>> {
    private final ExecutorService executorService;
    private final CountDownLatch signal;
    private final int currentLevel;
    private final int level;

    public SubClusterBuildTaskSubmitter(ExecutorService executorService, CountDownLatch signal, int currentLevel, int level) {
        this.executorService = executorService;
        this.signal = signal;
        this.currentLevel = currentLevel;
        this.level = level;
    }

    public void submit(Map<Pivot<D>, Cluster<D>> clusters, int originalPivotsCounts, Set<Pivot<D>> pivots) {
        for (Map.Entry<Pivot<D>, Cluster<D>> entry : clusters.entrySet()) {
            Set<Pivot<D>> subClusterPivots = new HashSet<>(pivots);
            subClusterPivots.remove(entry.getKey());

            SubClusterBuildTask<D> buildTask = new SubClusterBuildTask
                    .Builder<D>(executorService, signal).levels(currentLevel, level)
                                                        .pivots(originalPivotsCounts, subClusterPivots)
                                                        .cluster(entry.getValue())
                                                        .build();

            executorService.submit(buildTask);
        }

    }
}
