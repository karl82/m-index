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

import cz.rank.vsfs.btree.BPlusTreeMap;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Karel Rank
 */
public class IndexOrchestratorTest {
    private static final int INITIAL_PIVOTS_COUNT = 5;
    private static final int INITIAL_POINTS_COUNT = 100;
    public static final int CLUSTER_MAX_LEVEL = 2;

    @Test(groups = {"longRunning"})
    public void testIndexOrchestrator() throws NoSuchAlgorithmException {

        final Random random = ThreadLocalRandom.current();


        final Set<Pivot<Point>> pivots = new HashSet<>(INITIAL_PIVOTS_COUNT);
        for (int i = 0; i < INITIAL_PIVOTS_COUNT; i++) {
            pivots.add(new Pivot<>(i, new Point(random.nextDouble() * 50, random.nextDouble() * 50)));
        }

        final Set<Point> points = new HashSet<>(INITIAL_POINTS_COUNT);
        for (int i = 0; i < INITIAL_POINTS_COUNT; i++) {
            points.add(new Point(random.nextDouble() * 50, random.nextDouble() * 50));
        }

        final MultiLevelClusterBuilder<Point> builder = new MultiLevelClusterBuilder<Point>(CLUSTER_MAX_LEVEL)
                .fromPoints(points)
                .withPivots(pivots);

        final Collection<Cluster<Point>> clusters = builder.build();

        final IndexOrchestrator<Point> orchestrator = new IndexOrchestrator<>(CLUSTER_MAX_LEVEL, clusters);

        final BPlusTreeMap<Double, Point> btree = orchestrator.orchestrateBtree();

        System.out.println(btree);

    }

}
