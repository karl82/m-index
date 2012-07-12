package cz.rank.vsfs.mindex;

import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class PointTest {

    @Test
    public void testZeroDistance() {
        Point a = new Point(10, 10);
        Point b = new Point(10, 10);

        assertThat(a.distance(b), is(0d));
    }

    @Test
    public void testAbsDistance() {
        Point a = new Point(-100, -100);
        Point b = new Point(-10, -10);
        
        assertThat(a.distance(b), greaterThan(0d));
    }
}
