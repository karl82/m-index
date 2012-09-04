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

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*")
    public void testNoNullPivots() {
        new MultiLevelClusterBuilder<Point>(1).withPivots(null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*one point.*")
    public void testEmptyPivots() {
        new MultiLevelClusterBuilder<Point>(1).fromPoints(new HashSet<Point>());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*")
    public void testNullPivotsDuringBuild() {
        Set<Point> points = new HashSet<>();
        points.add(new Point(0, 0));

        new MultiLevelClusterBuilder<Point>(1).fromPoints(points).build();
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*one pivot.*")
    public void testNoEmptyPoints() {
        new MultiLevelClusterBuilder<Point>(1).withPivots(new HashSet<Pivot<Point>>());
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*")
    public void testNoNullPoints() {
        new MultiLevelClusterBuilder<Point>(1).fromPoints(null);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class}, expectedExceptionsMessageRegExp = ".*null.*")
    public void testNullPointsDuringBuild() {
        Set<Pivot<Point>> pivots = new HashSet<>();
        pivots.add(new Pivot<Point>(0, new Point(0, 0)));

        new MultiLevelClusterBuilder<Point>(1).withPivots(pivots).build();
    }

    @Test
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

    @Test
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

    @Test
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

    @Test(expectedExceptions = {IllegalArgumentException.class}, dataProvider = "notValidClusterLevels")
    public void testNotValidClusterLevel(int level) {
        new MultiLevelClusterBuilder<Point>(level);
    }

    @Test(dataProvider = "validClusterLevels")
    public void testValidClusterLevel(int level) {
        assertThat(new MultiLevelClusterBuilder<Point>(level).getLevel(), is(level));
    }

    @Test(groups = {"longRunning"})
    public void testHugeAmountOfPivotsAndClusters() throws NoSuchAlgorithmException {
        Random random = SecureRandom.getInstance("SHA1PRNG");


        Set<Pivot<Point>> pivots = new HashSet<>(INITIAL_PIVOTS_COUNT);
        for (int i = 0; i < INITIAL_PIVOTS_COUNT; i++) {
            pivots.add(new Pivot<Point>(i, new Point(random.nextDouble() * 50, random.nextDouble() * 50)));
        }

        Set<Point> points = new HashSet<>(INITIAL_POINTS_COUNT);
        for (int i= 0; i < INITIAL_POINTS_COUNT; i++) {
        points.add(new Point(random.nextDouble() * 50, random.nextDouble() * 50));
        }

        MultiLevelClusterBuilder<Point> builder = new MultiLevelClusterBuilder<Point>(5).fromPoints(points)
                                                                                        .withPivots(pivots);

        Collection<Cluster<Point>> clusters = builder.build();

    }

}
