package cz.rank.vsfs.mindex;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 */
public class ClusterTest {
    @Test
    public void testClusterLevel1With3Pivots() {
        Cluster cluster = new Cluster(new Point(0,0), 3, new int[] {0});
        assertThat(cluster.getIndex(), is(0));
        
        cluster = new Cluster(new Point(0,0), 3, new int[] {1});
        assertThat(cluster.getIndex(), is(1));
        
        cluster = new Cluster(new Point(0,0), 3, new int[] {2});
        assertThat(cluster.getIndex(), is(2));
    }
    
    @Test
    public void testClusterLevel2With4Pivots() {
        Cluster cluster = new Cluster(new Point(0,0), 4, new int[] {0,1});
        assertThat(cluster.getIndex(), is(1));
        
        cluster = new Cluster(new Point(0,0), 4, new int[] {0,2});
        assertThat(cluster.getIndex(), is(2));
        
        cluster = new Cluster(new Point(0,0), 4, new int[] {0,3});
        assertThat(cluster.getIndex(), is(3));
    }
}
