/**
 *
 */
package cz.rank.vsfs.btree;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author rank
 * 
 */
public class BPlusTreeTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSortOf2() {
        BPlusTree tree = new BPlusTree(2);

        tree.insert(0.2d);
        tree.insert(1.0d);
        tree.insert(0.1d);
        tree.insert(2.2d);

        assertEquals(0.2d, tree.search(0.2d), 0.01d);
    }

    @Test
    public void testSortOf2SeveralSplits() {
        BPlusTree tree = new BPlusTree(2);

        tree.insert(0.2d);
        tree.insert(1.0d);
        tree.insert(0.1d);
        tree.insert(2.2d);
        tree.insert(0.05d);
        tree.insert(2.4d);
        tree.insert(2.5d);
        tree.insert(2.6d);
        tree.insert(2.7d);

        assertEquals(2.7d, tree.search(2.7d), 0.01d);
    }

    @Test
    public void testSplitFullNode() {
        Node fullNode = new Node(2);

        fullNode.setKey(0, 0.5d);
        fullNode.setKey(1, 1.1d);
        fullNode.setKey(2, 1.5d);
        fullNode.setKeysCount(3);

        assertTrue(fullNode.isFull());

        Node newNode = new Node(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        BPlusTree.splitChild(newNode, 0, fullNode);

        assertFalse(fullNode.isFull());
        assertEquals(0.5d, newNode.getChild(0).getKey(0), 0.01d);
        assertEquals(1.1d, newNode.getKey(0), 0.01d);
        assertEquals(1.5d, newNode.getChild(1).getKey(0), 0.01d);
    }
}
