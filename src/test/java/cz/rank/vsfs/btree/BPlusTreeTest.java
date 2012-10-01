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

    @Test(groups = {"unit"})
    public void testDegree2() {
        BPlusTree<Double> tree = new BPlusTree<>(2);

        tree.insert(0.2d);
        tree.insert(1.0d);
        tree.insert(0.1d);
        tree.insert(2.2d);
        tree.insert(5.2d);

        assertThat(tree.search(0.2d), is(0.2d));
    }

    @Test(groups = {"unit"})
    public void testDegree20() {
        BPlusTree<Integer> tree = new BPlusTree<>(20);

        for (int d = 6000; d > -6000; d--) {
            tree.insert(d);
        }

        for (int d = 6000; d > 6000; d--) {
            assertThat(tree.search(d), is(d));
        }

    }

    @Test(groups = {"unit"})
    public void testDescendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<>(2);

        tree.insert(6);
        tree.insert(5);
        tree.insert(4);
        tree.insert(3);
        tree.insert(2);
        tree.insert(1);
        tree.insert(0);

        assertThat(tree.search(2), is(2));
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"unit"})
    public void testMinimalDegree0() {
        new BPlusTree<Integer>(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, groups = {"unit"})
    public void testMinimalDegree1() {
        new BPlusTree<Integer>(1);
    }

    @Test(groups = {"unit"})
    public void testNullReturnOnSearch() {
        BPlusTree<Integer> tree = new BPlusTree<>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        assertThat(tree.search(31), is(nullValue()));
    }

    @Test(groups = {"unit"})
    public void testSeveralSplitsAscendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<>(2);

        for (int d = -30; d < 30; d++) {
            tree.insert(d);
        }

        for (int d = -30; d < 30; d++) {
            assertThat(tree.search(d), is(d));
        }
    }

    @Test(groups = {"unit"})
    public void testSeveralSplitsDescendingDegree2() {
        BPlusTree<Integer> tree = new BPlusTree<>(2);

        for (int d = 30; d > -30; d--) {
            tree.insert(d);
        }

        for (int d = 30; d > -30; d--) {
            assertThat(tree.search(d), is(d));
        }
    }

    @Test(groups = {"unit"})
    public void testSplitFullChildNode() {
        Node<Integer> fullNode = new Node<>(2);

        fullNode.setKey(0, 1);
        fullNode.setKey(1, 2);
        fullNode.setKey(2, 3);
        fullNode.setKeysCount(3);
        fullNode.setLeaf(true);

        assertThat(fullNode.isFull(), is(true));

        Node<Integer> newNode = new Node<>(fullNode.getDegree());

        newNode.setKey(0, 4);
        newNode.setKeysCount(1);
        newNode.setChild(0, fullNode);
        newNode.setLeaf(false);

        Node<Integer> oldNode = new Node<>(fullNode.getDegree());

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

    @Test(groups = {"unit"})
    public void testSplitFullNode() {
        Node<Double> fullNode = new Node<>(2);

        fullNode.setKey(0, 0.5d);
        fullNode.setKey(1, 1.1d);
        fullNode.setKey(2, 1.5d);
        fullNode.setKeysCount(3);

        assertThat(fullNode.isFull(), is(true));

        Node<Double> newNode = new Node<>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        BPlusTree.splitChild(newNode, 0, fullNode);

        assertThat(fullNode.isFull(), is(false));
        assertThat(newNode.getChild(0).getKey(0), is(0.5d));
        assertThat(newNode.getKey(0), is(1.1d));
        assertThat(newNode.getChild(1).getKey(0), is(1.5d));
    }
}
