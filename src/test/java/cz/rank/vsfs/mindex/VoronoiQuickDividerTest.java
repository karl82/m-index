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

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class VoronoiQuickDividerTest {
    private final Collection<Pivot<Point>> pivots = new HashSet<>();
    private final Collection<Point> points = new HashSet<>();

    @BeforeMethod
    public void setUp() {
        for (int i = -1, pivotIndex = 0; i <= 1; i++, pivotIndex++) {
            pivots.add(new Pivot<>(pivotIndex, new Point(i, 0)));
        }

        for (int i = -2; i < 2; i++) {
            points.add(new Point(i, 1));
            points.add(new Point(i, 0));
            points.add(new Point(i, -1));
        }
    }

    @AfterMethod
    public void tearDown() {
        points.clear();
        pivots.clear();
    }

    @Test(groups = {"unit"})
    public void testOrder() {
        VoronoiQuickDivider<Point> divider = new VoronoiQuickDivider<>(pivots, points);


        Map<Point, Pivot<Point>> nearestPivots = divider.calculate();

        for (Entry<Point, Pivot<Point>> entry : nearestPivots.entrySet()) {
            System.out.println(entry);
        }
    }

}
