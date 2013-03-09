package cz.rank.vsfs.mindex;

/**
 */
public abstract class AbstractClusterTreeBuilder<D extends Distanceable<D>> implements ClusterTreeBuilder<D> {
    protected boolean atLeafLevel(Cluster<D> cluster) {
        return cluster.getLevel() + 1 == cluster.getMaxLevel();
    }

    protected LeafCluster<D> createLeafSubCluster(Cluster<D> cluster, Pivot<D> pivot) {
        return new LeafCluster<>(cluster, nextLevelIndex(cluster, pivot));
    }

    protected InternalCluster<D> createInternalSubCluster(Cluster<D> cluster, Pivot<D> pivot) {
        return new InternalCluster<>(cluster, nextLevelIndex(cluster, pivot));
    }

    protected Index nextLevelIndex(Cluster<D> cluster, Pivot<D> pivot) {
        return cluster.getIndex().addLevel(pivot.getIndex());
    }
}
