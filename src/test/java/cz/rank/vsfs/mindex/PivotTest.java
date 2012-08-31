package cz.rank.vsfs.mindex;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class PivotTest {
    @Test
    public void testPivotIndex() {
        Pivot<Point> pivot = new Pivot<>(0, new Point(0, 0));

        assertThat(pivot.getIndex(), is(0));
    }

    @Test
    public void testPivotsAreEqual() {
        Pivot<Point> pivot1 = new Pivot<>(0, new Point(0, 0));
        Pivot<Point> pivot2 = new Pivot<>(0, new Point(0, 0));

        assertThat(pivot1, is(pivot2));
    }

    @Test
    public void testPivotAreNotEqual() {
        Pivot<Point> pivot1 = new Pivot<>(0, new Point(0, 0));
        Pivot<Point> pivot2 = new Pivot<>(1, new Point(0, 0));

        assertThat(pivot1, is(not(pivot2)));
    }

}
