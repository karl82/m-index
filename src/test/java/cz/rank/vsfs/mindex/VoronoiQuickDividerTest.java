package cz.rank.vsfs.mindex;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class VoronoiQuickDividerTest {
    private final Collection<Pivot<Point>> pivots = new HashSet<>();
    private final Collection<Point> points = new HashSet<>();

    @BeforeMethod
    public void setUp() {
        for (int i = -1, pivotIndex = 0; i <= 1; i++, pivotIndex++) {
            pivots.add(new Pivot<>(pivotIndex, new Point(i, 0)));
        }

        for (int i = -2; i < 2; i++) {
            points.add(new Point(i, 1));
            points.add(new Point(i, 0));
            points.add(new Point(i, -1));
        }
    }

    @AfterMethod
    public void tearDown() {
        points.clear();
        pivots.clear();
    }

    @Test
    public void testOrder() {
        VoronoiQuickDivider<Point> divider = new VoronoiQuickDivider<>(pivots, points);

        divider.calculate();

        for (Entry<Point, Pivot<Point>> entry : divider.getNearestPivots().entrySet()) {
            System.out.println(entry);
        }
    }

}
