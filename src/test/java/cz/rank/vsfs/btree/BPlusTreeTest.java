/**
 *
 */
package cz.rank.vsfs.btree;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author rank
 * 
 */
public class BPlusTreeTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterMethod
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

        assertThat(tree.search(0.2d), is(0.2d));
    }

    @Test
    public void testDegree20() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(20);

        for (int d = 6000; d > -6000; d--) {
            tree.insert(d);
        }

        for (int d = 6000; d > 6000; d--) {
            assertThat(tree.search(d), is(d));
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

        assertThat(tree.search(2), is(2));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMinimalDegree0() {
        new BPlusTree<Integer>(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testMinimalDegree1() {
        new BPlusTree<Integer>(1);
    }

    @Test
    public void testNullReturnOnSearch() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        assertThat(tree.search(31), is(nullValue()));
    }

    @Test
    public void testSeveralSplitsAscendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        for (int d = -30; d < 30; d++) {
            assertThat(tree.search(d), is(d));
        }
    }

    @Test
    public void testSeveralSplitsDescendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<Integer>(2);

        for (int d = 30; d > -30; d--) {
            tree.insert(d);
        }

        for (int d = 30; d > -30; d--) {
            assertThat(tree.search(d), is(d));
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

        assertThat(fullNode.isFull(), is(true));

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

        assertThat(fullNode.isFull(), is(false));
        assertThat(newNode.getChild(0).getKey(0), is(1));
        assertThat(newNode.getKey(0), is(2));
        assertThat(newNode.getKey(1), is(4));
        assertThat(newNode.getChild(1).getKey(0), is(3));
        assertThat(newNode.getChild(2).getKey(0), is(5));
    }

    @Test
    public void testSplitFullNode() {
        Node<Double> fullNode = new Node<Double>(2);

        fullNode.setKey(0, 0.5d);
        fullNode.setKey(1, 1.1d);
        fullNode.setKey(2, 1.5d);
        fullNode.setKeysCount(3);

        assertThat(fullNode.isFull(), is(true));

        Node<Double> newNode = new Node<Double>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        BPlusTree.splitChild(newNode, 0, fullNode);

        assertThat(fullNode.isFull(), is(false));
        assertThat(newNode.getChild(0).getKey(0), is(0.5d));
        assertThat(newNode.getKey(0), is(1.1d));
        assertThat(newNode.getChild(1).getKey(0), is(1.5d));
    }
}
