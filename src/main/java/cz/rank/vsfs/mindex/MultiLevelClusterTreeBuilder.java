/*
 * Copyright © 2012 Karel Rank All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *  Neither the name of Karel Rank nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package cz.rank.vsfs.mindex;

import cz.rank.vsfs.btree.BPlusTreeMultiDoubleObjectMap;
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
    private final BPlusTreeMultiDoubleObjectMap<D> btreemap;

    public MultiLevelClusterTreeBuilder(List objects, Cluster clusterRoot, PivotDistanceTable pivotDistanceTable, BPlusTreeMultiDoubleObjectMap<D> btreemap) {

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
                    incrementCluster();
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

    private boolean atLeafLevel(Cluster<D> cluster) {
        return cluster.getLevel() + 1 == cluster.getMaxLevel();
    }


}
