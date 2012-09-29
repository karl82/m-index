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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 */
public class ClusterTest {
    @DataProvider(name = "clusterIndexTestData")
    public Object[][] createClusterIndexTestData() {
        return new Object[][] {
                { 2, new int[] { 0 }, 0 },
                { 2, new int[] { 1 }, 1 },
                { 4, new int[] { 0, 1 }, 1 },
                { 4, new int[] { 0, 2 }, 2 },
                { 4, new int[] { 0, 3 }, 3 },
                { 4, new int[] { 1, 0 }, 4 },
                { 4, new int[] { 1, 2 }, 6 },
                { 4, new int[] { 1, 3 }, 7 },
                { 4, new int[] { 2, 0 }, 8 },
                { 4, new int[] { 2, 1 }, 9 },
                { 4, new int[] { 2, 3 }, 11 },
                { 4, new int[] { 3, 0 }, 12 },
                { 4, new int[] { 3, 1 }, 13 },
                { 4, new int[] { 3, 2 }, 14 } };
    }

    @Test(dataProvider = "clusterIndexTestData")
    public void testClusterIndex(int pivotsCount, int[] indexes, int expectedIndex) {
        Cluster<Point> cluster = new Cluster<>(new Pivot<>(0, new Point(0, 0)), pivotsCount, indexes);
        assertThat(cluster.getIndex(), is(expectedIndex));
    }

    @Test
    public void testAddPointIntoCluster() {
        Cluster<Point> cluster = new Cluster<>(new Pivot<>(0, new Point(0, 0)), 2, new int[] { 1 });
        Point point = new Point(4, 3);
        cluster.add(point);

        cluster.normalizeDistances();

        assertThat(cluster.getKey(point), is(2d));
    }

    @Test(expectedExceptions = { IllegalStateException.class })
    public void testCallGetKeyBeforeNormalization() {
        Cluster<Point> cluster = new Cluster<>(new Pivot<>(0, new Point(0, 0)), 2, new int[] { 1 });
        cluster.getKey(new Point(0, 0));
    }

    @Test(expectedExceptions = { IllegalStateException.class })
    public void testDoubleCallNormalizeDistances() {
        Cluster<Point> cluster = new Cluster<>(new Pivot<>(0, new Point(0, 0)), 2, new int[] { 1 });

        cluster.normalizeDistances();
        cluster.normalizeDistances();
    }
}
