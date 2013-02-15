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

import org.apache.commons.math3.util.FastMath;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Karel Rank
 */
public class VectorTest {
    @DataProvider
    public Object[][] vectorData() {
        return new Object[][]{
                {new TestParams(new Vector(0d, 0d, 0d), new Vector(0d, 0d, 0d), 0.0d)},
                {new TestParams(new Vector(0d), new Vector(5d), 5.0d)},
                {new TestParams(new Vector(0d), new Vector(-5d), 5.0d)},
                {new TestParams(new Vector(0d, 0d), new Vector(5d, 5d, 0d), FastMath.sqrt(50))},
                {new TestParams(new Vector(0d, 0d), new Vector(-5d, 5d, 0d), FastMath.sqrt(50))},
                {new TestParams(new Vector(0d, 0d), new Vector(-5d, -5d, 0d), FastMath.sqrt(50))},
                {new TestParams(new Vector(0d, 0d), new Vector(5d, -5d, 0d), FastMath.sqrt(50))},
        };
    }

    @Test(groups = "unit", dataProvider = "vectorData")
    public void testDistanceBetweenVectors(TestParams params) {
        assertThat(params.vector1.distance(params.vector2), is(params.expectedDistance));
    }

    private class TestParams {
        private final Vector vector1;
        private final Vector vector2;
        private final double expectedDistance;

        public TestParams(Vector vector1, Vector vector2, double expectedDistance) {
            this.vector1 = vector1;
            this.vector2 = vector2;
            this.expectedDistance = expectedDistance;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("{").append(vector1);
            sb.append(",").append(vector2);
            sb.append(", expectedDistance=").append(expectedDistance);
            sb.append('}');
            return sb.toString();
        }
    }
}
