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

package cz.rank.vsfs.mindex;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Karel Rank
 */
public class ClusterTreeTest {
    @Test(groups = {"unit"})
    public void testMaximalLevel() {
        final ClusterTree<Point> tree = new ClusterTree<>(2, 5, twoPivots());
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*must be greater than 0.*", groups = {"unit"})
    public void testMaximalLevelMustBeGreaterThanOne() {
        final ClusterTree<Point> tree = new ClusterTree<>(0, 5, twoPivots());
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = ".*cannot be empty.*",
          groups = {"unit"})
    public void testPivotsCannotBeEmpty() {
        final ClusterTree<Point> tree = new ClusterTree<>(1, 5, Collections.<Pivot<Point>>emptyList());
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*cannot be null.*",
          groups = {"unit"})
    public void testPivotsCannotBeNull() {
        final ClusterTree<Point> tree = new ClusterTree<>(1, 5, null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = ".*must be lower than pivots.*", groups = {"unit"})
    public void testMaximalLevelMustBeLowerThanPivots() {
        final ClusterTree<Point> tree = new ClusterTree<>(3, 5, twoPivots());
    }

    private List<Pivot<Point>> twoPivots() {
        return Arrays.asList(new Pivot<>(0, new Point(0, 0)), new Pivot<>(1, new Point(1, 1)));
    }

    private List<Pivot<Point>> threePivots() {
        return Arrays.asList(new Pivot<>(0, new Point(0, 0)), new Pivot<>(1, new Point(1, 1)),
                             new Pivot<>(2, new Point(1, 5)));
    }

    @Test(groups = {"unit"})
    public void testRangeQuery2ndLevel() {
        final ClusterTree<Point> tree = new ClusterTree<>(2, 5, twoPivots());
        final Point point = new Point(0, 0);
        tree.add(point);
        tree.add(new Point(1, 1));

        tree.build();

        final Collection<Point> points = tree.rangeQuery(new Point(0, 0), 0.5d);

        assertThat(points, hasItem(point));
    }

    @Test(groups = {"unit"})
    public void testRangeQuery3rdLevel() {
        final ClusterTree<Point> tree = new ClusterTree<>(3, 5, threePivots());
        final Point point = new Point(2, 1);
        tree.add(point);
        tree.add(new Point(3, 5));
        tree.add(new Point(2, 4));
        tree.add(new Point(3, 2));
        tree.add(new Point(0, 1));

        tree.build();

        final Collection<Point> points = tree.rangeQuery(new Point(2.1d, 0.8d), 0.5d);

        assertThat(points, hasItem(point));
    }

}
