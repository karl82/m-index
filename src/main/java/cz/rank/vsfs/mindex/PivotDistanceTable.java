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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Karel Rank
 */
public class PivotDistanceTable<D extends Distanceable<D>> {
    private static final Logger logger = LoggerFactory.getLogger(PivotDistanceTable.class);
    private static final int SOLVER_GRANULARITY = 10000;
    private final double maximumDistance;
    private final List<Pivot<D>> pivots;
    private final List<D> objects;
    private final Map<D, List<PivotDistance<D>>> distancesSortedByDistance;
    private final Map<D, List<PivotDistance<D>>> distancesSortedByPivot;
    private final PivotDistanceComparator<D> distanceComparator = new PivotDistanceComparator<>();
    private final AtomicInteger submittedSolvers = new AtomicInteger();
    private final int parallelism = Runtime.getRuntime().availableProcessors();
    private volatile boolean submittedAllSolvers = false;
    private final int objectsSize;

    public PivotDistanceTable(double maximumDistance, List<Pivot<D>> pivots, List<D> objects) {
        this.maximumDistance = maximumDistance;
        this.pivots = pivots;
        this.objects = objects;
        objectsSize = objects.size();
        distancesSortedByDistance = new HashMap<>(objectsSize);
        distancesSortedByPivot = new HashMap<>(objectsSize);
    }

    public PivotDistanceTable(List<Pivot<D>> pivots, List<D> objects) {
        this.pivots = pivots;
        this.objects = objects;
        objectsSize = objects.size();
        distancesSortedByDistance = new HashMap<>(objectsSize);
        distancesSortedByPivot = new HashMap<>(objectsSize);
        maximumDistance = 1d;
    }

    public void calculate() {
        final ExecutorService ecsPool = Executors.newFixedThreadPool(parallelism);
        final ExecutorCompletionService<PivotDistanceResult> ecs = new ExecutorCompletionService<>(
                ecsPool);

        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        final ExecutorService es;
        es = Executors.newSingleThreadExecutor();
        es.execute(new Runnable() {
            @Override
            public void run() {
                int takenSolvers = 0;
                while (notAllSolversTaken(takenSolvers)) {
                    try {
                        PivotDistanceResult result = ecs.take().get();
                        distancesSortedByDistance.putAll(result.distancesSortedByDistance);
                        distancesSortedByPivot.putAll(result.distancesSortedByPivot);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        logger.error("Error during calculation pivot distance table!", e);
                    }
                    takenSolvers++;
                }

                waitForCalculation(cyclicBarrier);
            }

            private boolean notAllSolversTaken(int solvers) {
                return !submittedAllSolvers || solvers < submittedSolvers.get();
            }
        });

        submitSolvers(ecs);

        waitForCalculation(cyclicBarrier);

        es.shutdown();
        ecsPool.shutdown();
    }

    private void submitSolvers(ExecutorCompletionService<PivotDistanceResult> ecs) {
        for (int i = 0; i < objectsSize; i += SOLVER_GRANULARITY) {
            ecs.submit(new PivotDistanceSolver(i));
            submittedSolvers.incrementAndGet();
        }

        submittedAllSolvers = true;
    }

    private void waitForCalculation(CyclicBarrier cyclicBarrier) {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            logger.error("Error during calculation maximum distance!", e);
        }
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

    @Immutable
    private static class PivotDistanceComparator<D extends Distanceable<D>> implements Comparator<PivotDistance<D>> {
        @Override
        public int compare(PivotDistance<D> o1, PivotDistance<D> o2) {
            return o1.getPivot().getIndex() - o2.getPivot().getIndex();
        }
    }

    private class PivotDistanceSolver implements Callable<PivotDistanceResult> {
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
            List<PivotDistance<D>> objectDistances = doCalculatePivotDistances(object);

            copyAndSortDistancesByDistance(object, objectDistances);
            sortDistancesByPivot(object, objectDistances);
        }

        private List<PivotDistance<D>> doCalculatePivotDistances(D object) {
            final int size = pivots.size();
            final List<PivotDistance<D>> objectDistances = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                objectDistances.add(createPivotDistance(object, i));
            }
            return objectDistances;
        }

        private void sortDistancesByPivot(D object, List<PivotDistance<D>> objectDistances) {
            Collections.sort(objectDistances, distanceComparator);
            result.distancesSortedByPivot.put(object, objectDistances);
        }

        private void copyAndSortDistancesByDistance(D object, List<PivotDistance<D>> objectDistances) {
            List<PivotDistance<D>> objectDistancesSortedByDistance = new ArrayList<>(objectDistances);
            Collections.sort(objectDistancesSortedByDistance);
            result.distancesSortedByDistance.put(object, objectDistancesSortedByDistance);
        }

        private PivotDistance<D> createPivotDistance(D object, int i) {
            return new PivotDistance<>(maximumDistance, pivots.get(i), object);
        }
    }

    private class PivotDistanceResult<D extends Distanceable<D>> {
        private final Map<D, List<PivotDistance<D>>> distancesSortedByDistance = new HashMap<>(SOLVER_GRANULARITY);
        private final Map<D, List<PivotDistance<D>>> distancesSortedByPivot = new HashMap<>(SOLVER_GRANULARITY);
    }
}
