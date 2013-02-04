/*
 * Copyright © 2012 Karel Rank All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice, this
 *   list of conditions and the following disclaimer in the documentation and/or
 *   other materials provided with the distribution.
 *  Neither the name of Karel Rank nor the names of its contributors may be used to
 *   endorse or promote products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 *
 */
package cz.rank.vsfs.btree;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author rank
 */
public class BPlusTreeMapTest {

    /**
     * @throws Exception
     */
    @BeforeMethod
    public void setUp() throws Exception {
    }

    /**
     * @throws Exception
     */
    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test(groups = {"unit"})
    public void testDegree2() {
        BPlusTreeMap<Double, Object> tree = new BPlusTreeMap<>(2);

        final Object value = new Object();
        tree.insert(0.2d, value);
        tree.insert(1.0d, new Object());
        tree.insert(0.1d, new Object());
        tree.insert(2.2d, new Object());
        tree.insert(5.2d, new Object());

        assertThat(tree.search(0.2d), is(value));
    }

    @Test(groups = {"unit"})
    public void testValuesReplacement() {
        BPlusTreeMap<Integer, Object> tree = new BPlusTreeMap<>(2);

        final Object value = new Object();
        tree.insert(1, new Object());
        tree.insert(1, value);

        assertThat(tree.search(1), is(value));
    }


    @Test(groups = {"unit"})
    public void testDegree20() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(20);

        for (int d = 6000; d > -6000; d--) {
            tree.insert(d, d);
        }

        for (int d = 6000; d > -6000; d--) {
            assertThat(tree.search(d), is(d));
        }

    }

    @Test(groups = {"unit"})
    public void testDescendingDegree2() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        tree.insert(6, 6);
        tree.insert(5, 5);
        tree.insert(4, 4);
        tree.insert(3, 3);
        tree.insert(2, 2);
        tree.insert(1, 1);
        tree.insert(0, 0);

        assertThat(tree.search(2), is(2));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"unit"})
    public void testMinimalDegree0() {
        new BPlusTreeMap<Integer, Void>(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"unit"})
    public void testMinimalDegree1() {
        new BPlusTreeMap<Integer, Void>(1);
    }

    @Test(groups = {"unit"})
    public void testNullReturnOnSearch() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d, d);
        }

        assertThat(tree.search(31), is(nullValue()));
    }

    @Test(groups = {"unit"})
    public void testSeveralSplitsAscendingDegree2() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d, d);
        }

        for (int d = -30; d < 30; d++) {
            assertThat(tree.search(d), is(d));
        }
    }

    @Test(groups = {"unit"})
    public void testSeveralSplitsDescendingDegree2() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = 30; d > -30; d--) {
            tree.insert(d, d);
        }

        for (int d = 30; d > -30; d--) {
            assertThat(tree.search(d), is(d));
        }
    }

    @Test(groups = {"unit"})
    public void testRangeQuery() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = 10; d > -10; d--) {
            tree.insert(d, d);
        }

        List<Integer> range = tree.rangeSearch(-2, 2);

        assertThat(range, contains(-2, -1, 0, 1));
    }

    @Test(groups = {"unit"}, enabled = false)
    public void testRangeQueryWithDuplicates() {
        BPlusTreeMap<Integer, Object> tree = new BPlusTreeMap<>(2);

        Object object1 = new Object();
        Object object2 = new Object();

        for (int d = 10; d > -10; d--) {
            tree.insert(d, new Object());
        }

        tree.insert(12, object1);
        tree.insert(12, object2);

        List<Object> range = tree.rangeSearch(12, 13);

        assertThat(range, contains(object2, object1));
    }

    @Test(groups = {"unit"})
    public void testEmptyRangeQuery() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = 10; d > -10; d--) {
            tree.insert(d, d);
        }

        List<Integer> range = tree.rangeSearch(20, 22);

        assertThat(range, is(empty()));
    }

    @Test(groups = {"unit"}, expectedExceptions = IllegalArgumentException.class)
    public void testWrongRangesForRangeQuery() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = 10; d > -10; d--) {
            tree.insert(d, d);
        }

        tree.rangeSearch(22, 20);
    }

    @Test(groups = {"unit"})
    public void testOneMatchOnLastSiblingForRangeQuery() {
        BPlusTreeMap<Integer, Integer> tree = new BPlusTreeMap<>(2);

        for (int d = 10; d > -10; d--) {
            tree.insert(d, d);
        }

        List<Integer> range = tree.rangeSearch(10, 11);

        assertThat(range, contains(10));
    }

    @Test(groups = {"unit"})
    public void testSplitFullLeafNode() {
        LeafNode<Double, Double> fullNode = new LeafNode<>(2);

        fullNode.insertNonFull(0.5d, 0.5d);
        fullNode.insertNonFull(1.5d, 1.5d);
        fullNode.insertNonFull(2.5d, 2.5d);

        assertThat(fullNode.isFull(), is(true));

        InternalNode<Double, Double> newNode = new InternalNode<>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        fullNode.splitChild(newNode, 0);

        assertThat(fullNode.isFull(), is(false));
    }

    @Test(groups = {"unit"}, expectedExceptions = UnsupportedOperationException.class)
    public void testLeafNodeDoesntSupportChildren1() {
        new LeafNode<Integer, Object>(2).setChild(0, new LeafNode<Integer, Object>(2));
    }

    @Test(groups = {"unit"}, expectedExceptions = UnsupportedOperationException.class)
    public void testLeafNodeDoesntSupportChildren2() {
        new LeafNode<Integer, Object>(2).setChild(0, 0, new LeafNode<Integer, Object>(2));
    }

    @Test(groups = {"unit"})
    public void testSplitFullInternalNode() {
        InternalNode<Double, Double> fullNode = new InternalNode<>(2);

        final LeafNode<Double, Double> leafNode1 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode2 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode3 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode4 = new LeafNode<>(fullNode.getDegree());

        fullNode.setChild(0, leafNode1);
        fullNode.setChild(1, 1d, leafNode2);
        fullNode.setChild(2, 2d, leafNode3);
        fullNode.setChild(3, 3d, leafNode4);

        assertThat(fullNode.isFull(), is(true));

        InternalNode<Double, Double> newNode = new InternalNode<>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        fullNode.splitChild(newNode, 0);

        assertThat(fullNode.isFull(), is(false));
    }

}
