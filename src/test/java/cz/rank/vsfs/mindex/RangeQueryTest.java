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

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

/**
 * @author Karel Rank
 */
public class RangeQueryTest {
    private static final int CLUSTER_MAX_LEVEL = 2;

/*
    public void testRangeQuery() {
        final Pivot<Point> pivot1 = new Pivot<>(1, new Point(-1, -1));
        final Pivot<Point> pivot2 = new Pivot<>(2, new Point(1, 1));
        final Set<Pivot<Point>> pivots = new HashSet<>(Arrays.asList(pivot1, pivot2));

        final Point point1 = new Point(1.5, 1.5);
        final Point point2 = new Point(-1.5, -1.5);
        final Point point3 = new Point(0.0, 0.0);
        final Point point4 = new Point(0.5, 0.5);
        final Set<Point> points = new HashSet<>(Arrays.asList(point1, point2, point3, point4));

        final MultiLevelClusterBuilder<Point> builder = new MultiLevelClusterBuilder<Point>(CLUSTER_MAX_LEVEL)
                .fromPoints(points)
                .withPivots(pivots);

        final Collection<Cluster<Point>> clusters = builder.build();

        final IndexOrchestrator<Point> orchestrator = new IndexOrchestrator<>(CLUSTER_MAX_LEVEL, clusters);

        final BPlusTreeMap<Double, Point> btree = orchestrator.orchestrateBtree();


        final RangeQuery<Point> rangeQuery = new RangeQuery<>(clusters, btree);

        assertThat(rangeQuery.query(new Point(0.75, 0.75), 0.5), is(point4));
    }
*/


    @Test
    public void testRangeQuery() {
        final ClusterTree<Point> clusterTree = new ClusterTree<>(4, 10, fourPivots());

        clusterTree.add(new Point(0.1, 0.1));
        for (int i = -50; i < 50; i++) {
            clusterTree.add(new Point(i, i));
        }

        final Collection<Point> points = clusterTree.rangeQuery(new Point(0, 0), 0.5d);

        assertThat(points, contains(new Point(0, 0), new Point(0.1, 0.1)));
    }

    private List<Pivot<Point>> fourPivots() {
        return Arrays.asList(
                new Pivot<>(0, new Point(0, 0)),
                new Pivot<>(1, new Point(1, 1)),
                new Pivot<>(2, new Point(2, 2)),
                new Pivot<>(3, new Point(-10, -10)));
    }

}
