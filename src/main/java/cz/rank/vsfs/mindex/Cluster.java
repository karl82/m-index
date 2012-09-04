package cz.rank.vsfs.mindex;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.jcip.annotations.NotThreadSafe;

@NotThreadSafe
public class Cluster<D extends Distanceable<D>> {
    private final int[] indexes;
    private final int calculatedIndex;
    private final Pivot<D> basePivot;
    private final int pivotsCount;
    private final Map<D, Double> objects = new ConcurrentHashMap<>();
    private boolean normalized = false;
    private double maxDistance = 0.0d;
    private Set<Cluster<D>> subClusters;

    public Cluster(Pivot<D> basePivot, int pivotsCount, int[] indexes) {
        this.basePivot = basePivot;
        this.pivotsCount = pivotsCount;
        this.indexes = indexes.clone();
        this.calculatedIndex = calculateIndex();
        subClusters = new HashSet<>(this.pivotsCount);
    }

    public int getIndex() {
        return calculatedIndex;
    }

    private int calculateIndex() {
        int tempIndex = 0;
        for (int i = 0; i < indexes.length; i++) {
            tempIndex += indexes[i] * Math.pow(pivotsCount, indexes.length - 1 - i);
        }
        return tempIndex;
    }

    /**
     * Returns base pivot used for distance calculations
     * 
     * @return base pivot
     */
    public Pivot<D> getBasePivot() {
        return basePivot;
    }

    /**
     * Adds object into cluster
     * 
     * @param object
     */
    public void add(D object) {
        double distance = basePivot.distance(object);
        objects.put(object, distance);

        maxDistance = Math.max(maxDistance, distance);
    }

    /**
     * Normalize distances in cluster to have range [0, 1).
     * 
     * Can be called only once
     */
    public void normalizeDistances() {
        if (isNormalized()) {
            throw new IllegalStateException("Cluster is already normalized: " + this);
        }

        if (maxDistance > 0d) {
            for (Map.Entry<D, Double> entry : objects.entrySet()) {
                objects.put(entry.getKey(), entry.getValue() / maxDistance);
            }
        }

        normalized = true;
    }

    private boolean isNormalized() {
        return normalized;
    }

    public double getKey(D object) {
        if (isNotNormalized()) {
            throw new IllegalStateException("Cluster is not yet normalized: " + this);
        }

        Double objectDistance = objects.get(object);
        if (objectDistance == null) {
            throw new IllegalArgumentException("Object: " + object + " was not found in this cluster:" + this);
        }

        return objectDistance + getIndex();
    }

    private boolean isNotNormalized() {
        return !isNormalized();
    }

    public int[] getIndexes() {
        return indexes.clone();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cluster [indexes=").append(Arrays.toString(indexes)).append(", objects=").append(objects)
                .append("]");
        return builder.toString();
    }

    public Set<D> getObjects() {
        return new HashSet<>(objects.keySet());
    }

    public void addSubClusters(Collection<Cluster<D>> subClusters) {
        this.subClusters.addAll(subClusters);
    }
}
