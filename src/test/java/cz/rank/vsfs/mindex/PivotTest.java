package cz.rank.vsfs.mindex;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.Test;

public class PivotTest {
    @Test
    public void testPivot() {
        Pivot<Point> pivot = new Pivot<>(0, new Point(0, 0));

        assertThat(pivot.getIndex(), is(0));
    }
}
