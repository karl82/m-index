package cz.rank.vsfs.mindex;

import cz.rank.vsfs.btree.BPlusTreeMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 */
public class MultiLevelClusterTreeBuilder<D extends Distanceable<D>> extends AbstractClusterTreeBuilder<D> {
    private static final Logger logger = LoggerFactory.getLogger(MultiLevelClusterTreeBuilder.class);
    private final List<D> objects;
    private final Cluster<D> clusterRoot;
    private final PivotDistanceTable<D> pivotDistanceTable;
    private final BPlusTreeMultiMap<Double, D> btreemap;

    public MultiLevelClusterTreeBuilder(List<D> objects, Cluster<D> clusterRoot, PivotDistanceTable<D> pivotDistanceTable, BPlusTreeMultiMap<Double, D> btreemap) {

        this.objects = objects;
        this.clusterRoot = clusterRoot;
        this.pivotDistanceTable = pivotDistanceTable;
        this.btreemap = btreemap;
    }

    @Override
    public void build() {
        int maxLevel = clusterRoot.getIndex().getMaxLevel();
        for (D object : objects) {
            Cluster<D> currentCluster = clusterRoot;
            for (int currentLevel = 0; currentLevel < maxLevel; ++currentLevel) {
                final Pivot<D> pivot = pivotDistanceTable.pivotAt(object, currentLevel);

                Cluster<D> subCluster = currentCluster.getSubCluster(pivot);
                if (subCluster == null) {
                    subCluster = createAndStoreSubCluster(currentCluster, pivot);

                }

                currentCluster = subCluster;
            }

            final double distance = pivotDistanceTable.firstPivotDistance(object);
            final double objectKey = currentCluster.getCalculatedIndex() + distance;


            if (logger.isDebugEnabled()) {
                logger.debug("Inserting into B+Tree key: {}; obj: {}; cluster: {}", objectKey, object,
                        currentCluster.getIndex());
            }

            btreemap.insert(objectKey, object);
            currentCluster.setKey(objectKey);
        }
    }

    private Cluster<D> createAndStoreSubCluster(Cluster<D> cluster, Pivot<D> pivot) {
        Cluster<D> newCluster;
        if (atLeafLevel(cluster)) {
            newCluster = createLeafSubCluster(cluster, pivot);
        } else {
            newCluster = createInternalSubCluster(cluster, pivot);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Created new cluster: {}", newCluster);
        }

        cluster.storeSubCluster(pivot, newCluster);

        return newCluster;
    }

}
