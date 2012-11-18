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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Karel Rank
 */
public class PivotDistanceTest {
    @DataProvider(name = "comparableData")
    public Object[][] comparableData() {
        return new Object[][]{
                {5.0d,
                 5.0d,
                 0},
                {6.0d,
                 5.0d,
                 1},
                {5.0d,
                 6.0d,
                 -1}
        };

    }

    @Test(dataProvider = "comparableData")
    public void testCompareTo(double distance1, double distance2, int expectedResult) throws Exception {
        final PivotDistance<Point> pivotDistance1 = new PivotDistance<>(null, distance1);
        final PivotDistance<Point> pivotDistance2 = new PivotDistance<>(null, distance2);

        assertThat(pivotDistance1.compareTo(pivotDistance2), is(expectedResult));
    }

    @Test(dataProvider = "comparableData")
    public void testCompareToReverse(double distance1, double distance2, int expectedResult) throws Exception {
        final PivotDistance<Point> pivotDistance1 = new PivotDistance<>(null, distance1);
        final PivotDistance<Point> pivotDistance2 = new PivotDistance<>(null, distance2);

        assertThat(pivotDistance2.compareTo(pivotDistance1), is(-expectedResult));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testCompareToNull() throws Exception {
        final PivotDistance<Point> distance1 = new PivotDistance<>(null, 5.0d);

        distance1.compareTo(null);
    }

}
