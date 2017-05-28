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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karel Rank
 */
public class DynamicMIndexPerfTest extends MIndexPerfTest {

    private static final Logger logger = LoggerFactory.getLogger(DynamicMIndexPerfTest.class);
    private static final int[] LEAF_OBJECTS_COUNT = {
            10,
            20,
            50,
            100,
            200,
            300,
            500
    };

    @Override
    protected void warmUp() {
        logger.info("Performing JVM warm up...");
        DynamicTestParams params = new DynamicTestParams(50, 5000, 3, 20, 500, 0.15d, 50);
        performTest(params, "WARMUP", createIndex(params));
        performTest(params, "WARMUP", createIndex(params));
        performTest(params, "WARMUP", createIndex(params));
        performTest(params, "WARMUP", createIndex(params));
        logger.info("JVM warm up done...");
    }

    private MIndex<Vector> createIndex(DynamicTestParams params) {
        return new DynamicMIndex<>(params.clusterMaxLevel, params.btreeLevel,
                                   createPivots(params),
                                   maximumDistance,
                                   params.leafObjectsCount);
    }

/*
    @AfterMethod
    public void performGc() throws InterruptedException {
        logger.info("Performing GC...");
        System.gc();

        TimeUnit.SECONDS.sleep(5);
        logger.info("GC done...");
    }
*/

    @DataProvider(name = "clusterTreeParams")
    public Object[][] clusterTreeParams() {
        List<DynamicTestParams[]> params = new ArrayList<>();

        for (Integer dimension : PIVOTS_COUNT) {
            for (Integer objectsCount : CLUSTER_MAX_LEVEL) {
                for (Double range : RANGES) {
                    for (Integer queryObjects : QUERY_OBJECTS) {
                        for (Integer leafObjectsCount : LEAF_OBJECTS_COUNT) {
                            for (Integer btreeLevel : BTREE_LEVEL) {
                                params.add(
                                        new DynamicTestParams[]{new DynamicTestParams(
                                                dimension,
                                                queryObjects,
                                                objectsCount,
                                                DEFAULT_TEST_INVOCATIONS,
                                                leafObjectsCount,
                                                range,
                                                btreeLevel)});
                            }
                        }
                    }
                }
            }
        }
        return params.toArray(new Object[params.size()][1]);
    }

    @Test(groups = "perf", dataProvider = "clusterTreeParams")
    public void testClusterTree(DynamicTestParams params) throws InterruptedException {
        for (int i = 1; i < params.invocations + 1; i++) {
            performTest(params, params.toString(), createIndex(params));
            performGc();
        }
    }

    private static class DynamicTestParams
            extends TestParams {
        private final int leafObjectsCount;

        public DynamicTestParams(int pivotsCount,
                                 int queryObjects,
                                 int clusterMaxLevel,
                                 int invocations,
                                 int leafObjectsCount,
                                 double range,
                                 int btreeLevel) {
            super(pivotsCount, queryObjects, clusterMaxLevel, invocations, range, btreeLevel);
            this.leafObjectsCount = leafObjectsCount;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{pivotsCount=").append(pivotsCount);
            sb.append(", queryObjects=").append(queryObjects);
            sb.append(", clusterMaxLevel=").append(clusterMaxLevel);
            sb.append(", leafObjectsCount=").append(leafObjectsCount);
            sb.append(", range=").append(range);
            sb.append(", btreeLevel=").append(btreeLevel);
            sb.append('}');
            return sb.toString();
        }
    }
}
