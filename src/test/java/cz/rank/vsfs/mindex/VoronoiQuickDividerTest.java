package cz.rank.vsfs.mindex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class VoronoiQuickDividerTest {
    private final Collection<Point> pivots = new ArrayList<>();
    private final Collection<Point> points = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = -1; i <= 1; i++) {
            pivots.add(new Point(i, 0));
        }
        for (int i = -2; i < 2; i++) {
            points.add(new Point(i, 1));
            points.add(new Point(i, 0));
            points.add(new Point(i, -1));
        }
    }

    @After
    public void tearDown() {
        points.clear();
        pivots.clear();
    }

    @Test
    public void testOrder() {
        VoronoiQuickDivider<Point> divider = new VoronoiQuickDivider<>(pivots, points);

        divider.calculate();

        for (Entry<Point, Point> entry : divider.getNearestPivots().entrySet()) {
            System.out.println(entry);
        }
    }

}
