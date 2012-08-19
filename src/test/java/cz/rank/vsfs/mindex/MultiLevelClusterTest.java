package cz.rank.vsfs.mindex;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.annotations.Test;

public class MultiLevelClusterTest {
    @Test
    public void f() {

        Pivot<Point> pivot0 = new Pivot<>(0, new Point(0, 0));
        Pivot<Point> pivot1 = new Pivot<>(1, new Point(1, 0));
        Pivot<Point> pivot2 = new Pivot<>(2, new Point(0, 1));
        Pivot<Point> pivot3 = new Pivot<>(3, new Point(1, 1));

        Set<Pivot<Point>> pivots = new HashSet<>();

        pivots.add(pivot0);
        pivots.add(pivot1);
        pivots.add(pivot2);
        pivots.add(pivot3);

        Point point0 = new Point(0, 0.2d);
        Point point1 = new Point(0.2d, 0.2d);
        Point point2 = new Point(0.2d, 0);
        Point point3 = new Point(0.4d, 0.4d);
        Point point4 = new Point(0.5d, 0.5d);
        Point point5 = new Point(0.5d, 0);

        Set<Point> points = new HashSet<>();
        points.add(point0);
        points.add(point1);
        points.add(point2);
        points.add(point3);
        points.add(point4);
        points.add(point5);

        VoronoiQuickDivider<Point> divider = new VoronoiQuickDivider<>(pivots, points);
        divider.calculate();

        Cluster<Point> cluster = new Cluster<Point>(pivot0, pivots.size(), new int[] { pivot0.getIndex() });

        for (Entry<Point, Pivot<Point>> entry : divider.getNearestPivots().entrySet()) {
            assertThat(entry.getValue().getIndex(), is(pivot0.getIndex()));

            cluster.add(entry.getKey());
        }

        // Next clustering
        Set<Pivot<Point>> pivotsForSecondLevel = new HashSet<>(pivots);
        pivotsForSecondLevel.remove(pivot0);

        divider = new VoronoiQuickDivider<>(pivotsForSecondLevel, points);
        divider.calculate();

        Map<Integer, Cluster<Point>> clusters2ndLevel = new HashMap<>();
        int[] indexes2ndLevel = Arrays.copyOf(cluster.getIndexes(), cluster.getIndexes().length + 1);
        for (Pivot<Point> pivot : pivotsForSecondLevel) {
            indexes2ndLevel[indexes2ndLevel.length - 1] = pivot.getIndex();
            clusters2ndLevel.put(pivot.getIndex(),
                    new Cluster<>(cluster.getBasePivot(), pivots.size(), indexes2ndLevel));
        }

        for (Entry<Point, Pivot<Point>> entry : divider.getNearestPivots().entrySet()) {
            clusters2ndLevel.get(entry.getValue().getIndex()).add(entry.getKey());
        }

        System.out.print("");
    }
}
