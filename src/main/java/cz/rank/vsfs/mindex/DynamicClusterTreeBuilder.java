package cz.rank.vsfs.mindex;

import cz.rank.vsfs.btree.BPlusTreeMultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 */
public class DynamicClusterTreeBuilder<D extends Distanceable<D>> extends AbstractClusterTreeBuilder<D> {
    private static final Logger logger = LoggerFactory.getLogger(DynamicClusterTreeBuilder.class);
    private final int leafObjectsLimit;
    private final List<D> objects;
    private final Cluster<D> clusterRoot;
    private final PivotDistanceTable<D> pivotDistanceTable;
    private final BPlusTreeMultiMap<Double, D> btreemap;

    public DynamicClusterTreeBuilder(int leafObjectsLimit, List<D> objects, Cluster<D> clusterRoot, PivotDistanceTable<D> pivotDistanceTable, BPlusTreeMultiMap<Double, D> btreemap) {
        this.leafObjectsLimit = leafObjectsLimit;
        this.objects = objects;
        this.clusterRoot = clusterRoot;
        this.pivotDistanceTable = pivotDistanceTable;
        this.btreemap = btreemap;
    }

    @Override
    public void build() {
        int maxLevel = clusterRoot.getMaxLevel();
        final Map<D, Cluster<D>> objectsMapping = new HashMap<>(objects.size());
        final Deque<D> objectsDeque = new LinkedList<>(objects);
        while (!objectsDeque.isEmpty()) {
            final D object = objectsDeque.poll();

            Cluster<D> currentCluster = clusterRoot;
            for (int currentLevel = 0; currentLevel < maxLevel; ++currentLevel) {
                final Pivot<D> pivot = pivotDistanceTable.pivotAt(object, currentLevel);

                Cluster<D> subCluster = currentCluster.getSubCluster(pivot);
                if (subCluster == null) {
                    subCluster = createAndStoreLeafSubCluster(currentCluster, pivot);
                    storeObject(objectsMapping, object, subCluster);
                    break;
                } else {
                    // Is current cluster leaf cluster?
                    if (subCluster == LeafCluster.NO_SUBCLUSTERS) {
                        if (currentCluster.getObjectsCount() == leafObjectsLimit && notAtLeafLevel(currentCluster)) {
                            replaceLeafClusterWithInternal(object, objectsDeque, currentCluster, currentLevel);
                        } else {
                            storeObject(objectsMapping, object, currentCluster);
                        }

                        break;
                    } else {
                        currentCluster = subCluster;
                    }
                }
            }
        }

        for (Map.Entry<D, Cluster<D>> entry : objectsMapping.entrySet()) {
            final D object = entry.getKey();
            final Cluster<D> currentCluster = entry.getValue();
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

    private Cluster<D> createAndStoreLeafSubCluster(Cluster<D> currentCluster, Pivot<D> pivot) {
        final Cluster<D> newCluster = createLeafSubCluster(currentCluster, pivot);

        if (logger.isDebugEnabled()) {
            logger.debug("Created new cluster: {}", newCluster);
        }

        currentCluster.storeSubCluster(pivot, newCluster);
        return newCluster;
    }

    private void storeObject(Map<D, Cluster<D>> objectsMapping, D object, Cluster<D> currentCluster) {
        currentCluster.addObject(object);
        objectsMapping.put(object, currentCluster);
    }

    private void replaceLeafClusterWithInternal(D object, Deque<D> objectsDeque, Cluster<D> currentCluster, int currentLevel) {
        final Cluster<D> parent = currentCluster.getParent();
        final Pivot<D> currentPivot = pivotDistanceTable.pivotAt(object, currentLevel - 1);
        Cluster<D> internalCluster = createInternalSubCluster(parent, currentPivot);

        if (logger.isDebugEnabled()) {
            logger.debug("Replaced cluster: {} with internal cluster: {}", currentCluster, internalCluster);
        }

        objectsDeque.addAll(currentCluster.getObjects());
        objectsDeque.add(object);
        parent.storeSubCluster(currentPivot, internalCluster);
    }

    private boolean notAtLeafLevel(Cluster<D> currentCluster) {
        return !atLeafLevel(currentCluster);
    }
}
