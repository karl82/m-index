package cz.rank.vsfs.mindex;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 */
public class ClusterTest {
    @DataProvider(name = "clusterIndexTestData")
    public Object[][] createClusterIndexTestData() {
        return new Object[][] {
                { 2, new int[] { 0 }, 0 },
                { 2, new int[] { 1 }, 1 },
                { 4, new int[] { 0,1 }, 1 },
                { 4, new int[] { 0,2 }, 2 },
                { 4, new int[] { 0,3 }, 3 },
                { 4, new int[] { 1,0 }, 4 },
                { 4, new int[] { 1,2 }, 6 },
                { 4, new int[] { 1,3 }, 7 },
                { 4, new int[] { 2,0 }, 8 },
                { 4, new int[] { 2,1 }, 9 },
                { 4, new int[] { 2,3 }, 11 },
                { 4, new int[] { 3,0 }, 12 },
                { 4, new int[] { 3,1 }, 13 },
                { 4, new int[] { 3,2 }, 14 },
        };
    }

    @Test(dataProvider = "clusterIndexTestData")
    public void testClusterIndex(int pivotsCount, int[] indexes, int expectedIndex) {
        Cluster cluster = new Cluster(new Point(0, 0), pivotsCount, indexes);
        assertThat(cluster.getIndex(), is(expectedIndex));
    }
}
