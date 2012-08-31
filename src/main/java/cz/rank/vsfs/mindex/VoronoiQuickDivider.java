package cz.rank.vsfs.mindex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class VoronoiQuickDivider<D extends Distanceable<D>> {
    private final Collection<Pivot<D>> pivots;
    private final Collection<D> points;
    private final Map<D, Pivot<D>> nearestPivots = new HashMap<>();

    public VoronoiQuickDivider(Collection<Pivot<D>> pivots, Collection<D> points) {
        this.pivots = pivots;
        this.points = points;
    }

    public void calculate() {
        for (D point : points) {
            Pivot<D> nearestPivot = null;
            double shortestDistance = Double.MAX_VALUE;
            for (Pivot<D> pivot : pivots) {
                double currentDistance = pivot.distance(point);

                if (currentDistance < shortestDistance) {
                    shortestDistance = currentDistance;
                    nearestPivot = pivot;
                }
            }

            nearestPivots.put(point, nearestPivot);
        }
    }

    public HashMap<D, Pivot<D>> getNearestPivots() {
        return new HashMap<>(nearestPivots);
    }
}