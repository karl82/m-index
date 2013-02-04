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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Karel Rank
 */
public class PivotDistanceTableTest {
    @Test
    public void testDistanceTable() {
        PivotDistanceTable<Point> distanceTable = new PivotDistanceTable<>(pivots(), points());
        distanceTable.calculate();
    }

    private Collection<Point> points() {
        Collection<Point> points = new ArrayList<>();
        points.add(new Point(-2,1));
        points.add(new Point(-1,1));
        points.add(new Point(0,1));
        points.add(new Point(1,1));
        return points;
    }

    private Collection<Pivot<Point>> pivots() {
        Collection<Pivot<Point>> pivots = new ArrayList<>();

        pivots.add(new Pivot<>(0, new Point(-2,0)));
        pivots.add(new Pivot<>(1, new Point(-1,0)));
        pivots.add(new Pivot<>(2, new Point(0,0)));
        pivots.add(new Pivot<>(3, new Point(1,0)));
        return pivots;
    }

}
