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

package cz.rank.vsfs.mindex.util;

import cz.rank.vsfs.mindex.Distanceable;
import cz.rank.vsfs.mindex.Pivot;
import cz.rank.vsfs.mindex.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Karel Rank
 */
public class Generators {
    public static <D extends Distanceable<D>> List<Pivot<D>> createPivots(List<D> pivotPoints) {
        List<Pivot<D>> pivots = new ArrayList<>(pivotPoints.size());

        for (int i = 0; i < pivotPoints.size(); ++i) {
            pivots.add(new Pivot<>(i, pivotPoints.get(i)));
        }

        return pivots;
    }

    public static List<Vector> createVectors(int vectorsCount, int vectorDimension, int limit) {
        final List<Vector> vectors = new ArrayList<>(vectorsCount);

        final ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < vectorsCount; ++i) {
            final List<Double> values = new ArrayList<>(vectorDimension);
            for (int j = 0; j < vectorDimension; ++j) {
                values.add(random.nextDouble(-limit, limit));
            }
            vectors.add(new Vector(values));
        }

        return vectors;
    }
}
