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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Karel Rank
 */
public class PivotDistanceTable<D extends Distanceable<D>> {
    private final double maximumDistance;
    private final List<Pivot<D>> pivots;
    private final Collection<D> objects;

    private final Map<D, List<PivotDistance<D>>> distancesSortedByDistance;
    private final Map<D, List<PivotDistance<D>>> distancesSortedByPivot;
    private final PivotDistanceComparator<D> distanceComparator = new PivotDistanceComparator<>();

    public PivotDistanceTable(double maximumDistance, List<Pivot<D>> pivots, Collection<D> objects) {
        this.maximumDistance = maximumDistance;
        this.pivots = pivots;
        this.objects = objects;
        distancesSortedByDistance = new HashMap<>(objects.size());
        distancesSortedByPivot = new HashMap<>(objects.size());
    }

    public PivotDistanceTable(List<Pivot<D>> pivots, Collection<D> objects) {
        this.pivots = pivots;
        this.objects = objects;
        distancesSortedByDistance = new HashMap<>(objects.size());
        distancesSortedByPivot = new HashMap<>(objects.size());
        maximumDistance = 1d;
    }

    public void calculate() {
        for (D object : objects) {
            calculateObjectDistance(object);
        }
    }

    private void calculateObjectDistance(D object) {
        List<PivotDistance<D>> objectDistances = doCalculatePivotDistances(object);

        copyAndSortDistancesByDistance(object, objectDistances);
        sortDistancesByPivot(object, objectDistances);
    }

    private void sortDistancesByPivot(D object, List<PivotDistance<D>> objectDistances) {
        Collections.sort(objectDistances, distanceComparator);
        distancesSortedByPivot.put(object, objectDistances);
    }

    private void copyAndSortDistancesByDistance(D object, List<PivotDistance<D>> objectDistances) {
        List<PivotDistance<D>> objectDistancesSortedByDistance = new ArrayList<>(objectDistances);
        Collections.sort(objectDistancesSortedByDistance);
        distancesSortedByDistance.put(object, objectDistancesSortedByDistance);
    }

    private List<PivotDistance<D>> doCalculatePivotDistances(D object) {
        final int size = pivots.size();
        final List<PivotDistance<D>> objectDistances = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            objectDistances.add(createPivotDistance(object, i));
        }
        return objectDistances;
    }

    private PivotDistance<D> createPivotDistance(D object, int i) {
        return new PivotDistance<>(maximumDistance, pivots.get(i), object);
    }

    public Pivot<D> pivotAt(D object, int index) {
        return pivotDistanceAt(object, index).getPivot();
    }

    private PivotDistance<D> pivotDistanceAt(D object, int index) {
        return distancesSortedByDistance.get(object).get(index);
    }

    public double firstPivotDistance(D object) {
        return distanceAt(object, 0);
    }

    public double distanceAt(D object, int index) {
        return pivotDistanceAt(object, index).getDistance();
    }

    public double pivotDistance(D object, int pivotIndex) {
        return distancesSortedByPivot.get(object).get(pivotIndex).getDistance();
    }

    private static class PivotDistanceComparator<D extends Distanceable<D>> implements Comparator<PivotDistance<D>> {
        @Override
        public int compare(PivotDistance<D> o1, PivotDistance<D> o2) {
            return o1.getPivot().getIndex() - o2.getPivot().getIndex();
        }
    }
}
