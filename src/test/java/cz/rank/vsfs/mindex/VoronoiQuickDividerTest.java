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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VoronoiQuickDividerTest {
    @DataProvider(name = "pivotsPointsData")
    public Object[][] pivotsPointsDataProvider() {
        Collection<Pivot<Point>> pivots = new HashSet<>();
        Collection<Point> points = new HashSet<>();
        Map<Point, PivotDistance<Point>> expected = new HashMap<>();

        Point tmpPoint = addPoint(points, -5, 0);
        expected.put(tmpPoint, new PivotDistance<>(addPivot(pivots, 0, -5, -5), tmpPoint));
        tmpPoint = addPoint(points, -4, 0);
        expected.put(tmpPoint, new PivotDistance<>(addPivot(pivots, 1, -4, -5), tmpPoint));
        tmpPoint = addPoint(points, -3, 0);
        expected.put(tmpPoint, new PivotDistance<>(addPivot(pivots, 2, -3, -5), tmpPoint));
        tmpPoint = addPoint(points, -2, 0);
        expected.put(tmpPoint, new PivotDistance<>(addPivot(pivots, 3, -2, -5), tmpPoint));
        tmpPoint = addPoint(points, -1, 0);
        expected.put(tmpPoint, new PivotDistance<>(addPivot(pivots, 4, -1, -5), tmpPoint));

        return new Object[][]{
                {pivots,
                 points,
                 expected}
        };
    }

    private Point addPoint(Collection<Point> points, int x, int y) {
        Point point = new Point(x, y);
        points.add(point);

        return point;
    }

    private Pivot<Point> addPivot(Collection<Pivot<Point>> pivots, int index, int x, int y) {
        final Pivot<Point> pivot = new Pivot<>(index, new Point(x, y));
        pivots.add(pivot);

        return pivot;
    }

    @Test(groups = {"unit"}, dataProvider = "pivotsPointsData")
    public void testOrder(Collection<Pivot<Point>> pivots, Collection<Point> points, Map<Point, PivotDistance<Point>> expected) {
        VoronoiQuickDivider<Point> divider = new VoronoiQuickDivider<>(pivots, points);

        assertThat(divider.calculate(), is(expected));
    }
}
