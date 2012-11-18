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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class MultiLevelClusterBuilderTest {

    public static final int INITIAL_PIVOTS_COUNT = 500;
    public static final int INITIAL_POINTS_COUNT = 5000;

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*",
          groups = {"unit"})
    public void testNoNullPivots() {
        new MultiLevelClusterBuilder<Point>(1).withPivots(null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*one point.*",
          groups = {"unit"})
    public void testEmptyPivots() {
        new MultiLevelClusterBuilder<Point>(1).fromPoints(new HashSet<Point>());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*",
          groups = {"unit"})
    public void testNullPivotsDuringBuild() {
        Set<Point> points = new HashSet<>();
        points.add(new Point(0, 0));

        new MultiLevelClusterBuilder<Point>(1).fromPoints(points).build();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*one pivot.*",
          groups = {"unit"})
    public void testNoEmptyPoints() {
        new MultiLevelClusterBuilder<Point>(1).withPivots(new HashSet<Pivot<Point>>());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*",
          groups = {"unit"})
    public void testNoNullPoints() {
        new MultiLevelClusterBuilder<Point>(1).fromPoints(null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*",
          groups = {"unit"})
    public void testNullPointsDuringBuild() {
        Set<Pivot<Point>> pivots = new HashSet<>();
        pivots.add(new Pivot<>(0, new Point(0, 0)));

        new MultiLevelClusterBuilder<Point>(1).withPivots(pivots).build();
    }

    @Test(groups = {"unit"})
    public void testClusterSizeMustBeTheSameAsPivots() {
        Pivot<Point> pivot0 = new Pivot<>(0, new Point(0, 0));
        Pivot<Point> pivot1 = new Pivot<>(1, new Point(1, 0));
        Pivot<Point> pivot2 = new Pivot<>(2, new Point(0, 1));
        Pivot<Point> pivot3 = new Pivot<>(3, new Point(1, 1));

        Set<Pivot<Point>> pivots = new HashSet<>();
        pivots.add(pivot0);
        pivots.add(pivot1);
        pivots.add(pivot2);
        pivots.add(pivot3);

        Set<Point> points = new HashSet<>();
        points.add(new Point(0, 0));

        assertThat(new MultiLevelClusterBuilder<Point>(2).withPivots(pivots)
                                                         .fromPoints(points)
                                                         .build()
                                                         .size(), is(pivots.size()));
    }

    @Test(groups = {"unit"})
    public void testClustersMustBeNormalized() {
        Point point = new Point(0, 0);
        Pivot<Point> pivot0 = new Pivot<>(0, point);

        Set<Pivot<Point>> pivots = new HashSet<>();
        pivots.add(pivot0);

        Set<Point> points = new HashSet<>();
        points.add(point);

        Collection<Cluster<Point>> clusters = new MultiLevelClusterBuilder<Point>(1).withPivots(pivots)
                                                                                    .fromPoints(points)
                                                                                    .build();
        Cluster<Point> cluster = clusters.iterator().next();

        assertThat(cluster.getKey(point), is(0d));
    }

    @Test(groups = {"unit"})
    public void test4Levels() {

        Pivot<Point> pivot0 = new Pivot<>(0, new Point(0, 0));
        Pivot<Point> pivot1 = new Pivot<>(1, new Point(1, 0));
        Pivot<Point> pivot2 = new Pivot<>(2, new Point(0, 1));
        Pivot<Point> pivot3 = new Pivot<>(3, new Point(1, 1));

        Set<Pivot<Point>> pivots = new HashSet<>();

        pivots.add(pivot0);
        pivots.add(pivot1);
        pivots.add(pivot2);
        pivots.add(pivot3);

        Point point0 = new Point(0, 0);
        Point point1 = new Point(1, 0);
        Point point2 = new Point(0, 1);
        Point point3 = new Point(1, 1);

        Set<Point> points = new HashSet<>();
        points.add(point0);
        points.add(point1);
        points.add(point2);
        points.add(point3);

        MultiLevelClusterBuilder<Point> builder = new MultiLevelClusterBuilder<Point>(2).fromPoints(points)
                                                                                        .withPivots(pivots);

        Collection<Cluster<Point>> clusters = builder.build();

        for (Cluster<Point> cluster : clusters) {
            assertThat(cluster.getObjects().size(), is(1));
        }
    }

    @DataProvider(name = "notValidClusterLevels")
    public Object[][] createNotValidClusterLevels() {
        return new Object[][]{
                {0},
                {-1},
        };
    }

    @DataProvider(name = "validClusterLevels")
    public Object[][] createValidClusterLevels() {
        return new Object[][]{
                {1},
                {2},
        };
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, dataProvider = "notValidClusterLevels",
          groups = {"unit"})
    public void testNotValidClusterLevel(int level) {
        new MultiLevelClusterBuilder<Point>(level);
    }

    @Test(dataProvider = "validClusterLevels", groups = {"unit"})
    public void testValidClusterLevel(int level) {
        assertThat(new MultiLevelClusterBuilder<Point>(level).getLevel(), is(level));
    }

    @Test(groups = {"longRunning"})
    public void testHugeAmountOfPivotsAndClusters() throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstance("SHA1PRNG");


        Set<Pivot<Point>> pivots = new HashSet<>(INITIAL_PIVOTS_COUNT);
        for (int i = 0; i < INITIAL_PIVOTS_COUNT; i++) {
            pivots.add(new Pivot<>(i, new Point(random.nextDouble() * 50, random.nextDouble() * 50)));
        }

        Set<Point> points = new HashSet<>(INITIAL_POINTS_COUNT);
        for (int i = 0; i < INITIAL_POINTS_COUNT; i++) {
            points.add(new Point(random.nextDouble() * 50, random.nextDouble() * 50));
        }

        MultiLevelClusterBuilder<Point> builder = new MultiLevelClusterBuilder<Point>(5).fromPoints(points)
                                                                                        .withPivots(pivots);

        Collection<Cluster<Point>> clusters = builder.build();

    }

}
