package cz.rank.vsfs.mindex;

import java.util.Map;
import java.util.Set;

public class PointsIntoClusterDivider<D extends Distanceable<D>> {
    private final Map<Pivot<D>, Cluster<D>> clusters;
    private final Set<Pivot<D>> pivots;
    private final Set<D> points;

    public PointsIntoClusterDivider(Map<Pivot<D>, Cluster<D>> clusters, Set<Pivot<D>> pivots, Set<D> points) {
        this.clusters = clusters;
        this.pivots = pivots;
        this.points = points;
    }

    public void divide() {
        VoronoiQuickDivider<D> divider = new VoronoiQuickDivider<D>(pivots, points);
        Map<D, Pivot<D>> nearestPivots = divider.calculate();

        assignObjectsToClusters(nearestPivots);

        normalizeClusters();
    }

    private void assignObjectsToClusters(Map<D, Pivot<D>> nearestPivots) {
        for (Map.Entry<D, Pivot<D>> entry : nearestPivots.entrySet()) {
            final Cluster<D> cluster = clusters.get(entry.getValue());

            cluster.add(entry.getKey());
        }
    }

    private void normalizeClusters() {
        for (Cluster<D> cluster : clusters.values()) {
            cluster.normalizeDistances();
        }
    }
}
