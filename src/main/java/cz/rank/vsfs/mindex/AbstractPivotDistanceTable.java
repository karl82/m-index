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

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * @author Karel Rank
 */
public abstract class AbstractPivotDistanceTable<D extends Distanceable<D>> implements PivotDistanceTable<D> {
    protected static final int SOLVER_GRANULARITY = 10000;
    protected final double maximumDistance;
    protected final List<Pivot<D>> pivots;
    protected final List<D> objects;
    protected final Map<D, List<PivotDistance<D>>> distancesSortedByDistance;
    protected final Map<D, List<PivotDistance<D>>> distancesSortedByPivot;
    protected final int objectsSize;
    private final PivotDistanceComparator distanceComparator = new PivotDistanceComparator();

    public AbstractPivotDistanceTable(double maximumDistance, List<Pivot<D>> pivots, List<D> objects) {
        this.maximumDistance = maximumDistance;
        this.pivots = pivots;
        this.objects = objects;
        objectsSize = objects.size();
        distancesSortedByDistance = new HashMap<>(objectsSize);
        distancesSortedByPivot = new HashMap<>(objectsSize);
    }

    public AbstractPivotDistanceTable(List<Pivot<D>> pivots, List<D> objects) {
        this.pivots = pivots;
        this.objects = objects;
        objectsSize = objects.size();
        distancesSortedByDistance = new HashMap<>(objectsSize);
        distancesSortedByPivot = new HashMap<>(objectsSize);
        maximumDistance = 1d;
    }

    protected PivotDistance<D> pivotDistanceAt(D object, int index) {
        return distancesSortedByDistance.get(object).get(index);
    }

    @Override
    public double firstPivotDistance(D object) {
        return distanceAt(object, 0);
    }

    @Override
    public double distanceAt(D object, int index) {
        return pivotDistanceAt(object, index).getDistance();
    }

    @Override
    public double pivotDistance(D object, int pivotIndex) {
        return distancesSortedByPivot.get(object).get(pivotIndex).getDistance();
    }

    @Override
    public Pivot<D> pivotAt(D object, int index) {
        return pivotDistanceAt(object, index).getPivot();
    }

    protected void storeResult(PivotDistanceResult result) {
        distancesSortedByDistance.putAll(result.distancesSortedByDistance);
        distancesSortedByPivot.putAll(result.distancesSortedByPivot);
    }

    @Immutable
    private class PivotDistanceComparator implements Comparator<PivotDistance<D>> {
        @Override
        public int compare(PivotDistance<D> o1, PivotDistance<D> o2) {
            return o1.getPivot().getIndex() - o2.getPivot().getIndex();
        }
    }

    protected class PivotDistanceSolver implements Callable<ParallelPivotDistanceTable.PivotDistanceResult> {
        private final int objectIndex;
        private final PivotDistanceResult result = new PivotDistanceResult();

        public PivotDistanceSolver(int objectIndex) {
            this.objectIndex = objectIndex;
        }

        @Override
        public PivotDistanceResult call() throws Exception {
            for (int i = objectIndex; i < objectIndex + SOLVER_GRANULARITY && i < objectsSize; ++i) {
                calculateObjectDistance(objects.get(i));
            }
            return result;
        }

        private void calculateObjectDistance(D object) {
            final SortedSet<PivotDistance<D>> objectDistancesByPivot = new TreeSet<>(
                    distanceComparator);
            final SortedSet<PivotDistance<D>> objectDistancesByDistance = new TreeSet<>();
            final int size = pivots.size();
            for (int i = 0; i < size; i++) {
                final PivotDistance<D> pivotDistance = createPivotDistance(object, i);
                objectDistancesByPivot.add(pivotDistance);
                objectDistancesByDistance.add(pivotDistance);
            }

            result.distancesSortedByPivot.put(object, new ArrayList<>(objectDistancesByPivot));
            result.distancesSortedByDistance.put(object, new ArrayList<>(objectDistancesByDistance));
        }

        private PivotDistance<D> createPivotDistance(D object, int i) {
            return new PivotDistance<>(maximumDistance, pivots.get(i), object);
        }
    }

    protected class PivotDistanceResult<D extends Distanceable<D>> {
        protected final Map<D, List<PivotDistance<D>>> distancesSortedByDistance = new HashMap<>(SOLVER_GRANULARITY);
        protected final Map<D, List<PivotDistance<D>>> distancesSortedByPivot = new HashMap<>(SOLVER_GRANULARITY);
    }
}
