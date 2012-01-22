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
    public void testDegree2() {
        BPlusTree<Double> tree = new BPlusTree<Double>(2);

        tree.insert(0.2d);
        tree.insert(1.0d);
        tree.insert(0.1d);
        tree.insert(2.2d);
        tree.insert(5.2d);

        assertEquals(0.2d, tree.search(0.2d), 0.01d);
    }

    @Test
    public void testDegree20() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(20);

        for (int d = 6000; d > -6000; d--) {
            tree.insert(d);
        }

        for (int d = 6000; d > 6000; d--) {
            assertEquals((Integer) d, tree.search(d));
        }

    }

    @Test
    public void testDescendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        tree.insert(6);
        tree.insert(5);
        tree.insert(4);
        tree.insert(3);
        tree.insert(2);
        tree.insert(1);
        tree.insert(0);

        assertEquals(Integer.valueOf(2), tree.search(2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testMinimalDegree0() {
        new BPlusTree<Integer>(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMinimalDegree1() {
        new BPlusTree<Integer>(1);
    }

    @Test
    public void testNullReturnOnSearch() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        assertNull(tree.search(31));
    }

    @Test
    public void testSeveralSplitsAscendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        for (int d = -30; d < 30; d++) {
            assertEquals((Integer) d, tree.search(d));
        }
    }

    @Test
    public void testSeveralSplitsDescendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = 30; d > -30; d--) {
            tree.insert(d);
        }

        for (int d = 30; d > -30; d--) {
            assertEquals((Integer) d, tree.search(d));
        }
    }

    @Test
    public void testSplitFullChildNode() {
        Node<Integer> fullNode = new Node<Integer>(2);

        fullNode.setKey(0, 1);
        fullNode.setKey(1, 2);
        fullNode.setKey(2, 3);
        fullNode.setKeysCount(3);
        fullNode.setLeaf(true);

        assertTrue(fullNode.isFull());

        Node<Integer> newNode = new Node<Integer>(fullNode.getDegree());

        newNode.setKey(0, 4);
        newNode.setKeysCount(1);
        newNode.setChild(0, fullNode);
        newNode.setLeaf(false);

        Node<Integer> oldNode = new Node<Integer>(fullNode.getDegree());

        oldNode.setKey(0, 5);
        oldNode.setKeysCount(1);
        oldNode.setLeaf(true);

        newNode.setChild(1, oldNode);

        BPlusTree.splitChild(newNode, 0, fullNode);

        assertFalse(fullNode.isFull());
        assertEquals(1, newNode.getChild(0).getKey(0), 0.01d);
        assertEquals(2, newNode.getKey(0), 0.01d);
        assertEquals(4, newNode.getKey(1), 0.01d);
        assertEquals(3, newNode.getChild(1).getKey(0), 0.01d);
        assertEquals(5, newNode.getChild(2).getKey(0), 0.01d);
    }

    @Test
    public void testSplitFullNode() {
        Node<Double> fullNode = new Node<Double>(2);

        fullNode.setKey(0, 0.5d);
        fullNode.setKey(1, 1.1d);
        fullNode.setKey(2, 1.5d);
        fullNode.setKeysCount(3);

        assertTrue(fullNode.isFull());

        Node<Double> newNode = new Node<Double>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        BPlusTree.splitChild(newNode, 0, fullNode);

        assertFalse(fullNode.isFull());
        assertEquals(0.5d, newNode.getChild(0).getKey(0), 0.01d);
        assertEquals(1.1d, newNode.getKey(0), 0.01d);
        assertEquals(1.5d, newNode.getChild(1).getKey(0), 0.01d);
    }
}
