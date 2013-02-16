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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Calculates maximum distance between distanceable objects
 * <p/>
 * Uses divide and conquer to split work between several cores
 *
 * @author Karel Rank
 */
public class MaximumDistance<D extends Distanceable<D>> {
    /**
     * Amount of computation done by each solver
     */
    public static final int SOLVER_GRANULARITY = 10000;
    private static final Logger logger = LoggerFactory.getLogger(MaximumDistance.class);
    private final List<D> objects;
    /**
     * Use all available cores.
     */
    private final ExecutorCompletionService<Double> ecs;
    private final int objectsSize;
    private AtomicInteger submittedSolvers = new AtomicInteger();
    private double maximum = 0d;
    private volatile boolean submittedAllSolvers = false;

    public MaximumDistance(List<D> objects) {
        this.objects = objects;
        objectsSize = objects.size();
        ecs = new ExecutorCompletionService<>(
                Executors.newFixedThreadPool(nSolverThreads()));
    }

    public MaximumDistance(List<D> objects, int cores) {
        this.objects = objects;
        objectsSize = objects.size();
        ecs = new ExecutorCompletionService<>(
                Executors.newFixedThreadPool(cores));
    }

    /**
     * Optimal count of solver threads
     *
     * @return {@link Runtime#availableProcessors()}
     */
    private int nSolverThreads() {
        return Runtime.getRuntime().availableProcessors();
    }

    public double calculate() {
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        final ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(new Runnable() {
            @Override
            public void run() {
                int takenSolvers = 0;
                while (takenSolvers++ != submittedSolvers.get() || !submittedAllSolvers) {
                    try {
                        Future<Double> result = ecs.take();
                        maximum = FastMath.max(result.get(), maximum);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        logger.error("Error during calculation maximum distance!", e);
                    }
                }

                waitForCalculation(cyclicBarrier);
            }
        });

        submitSolvers();

        waitForCalculation(cyclicBarrier);

        return maximum;
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

    private void submitSolvers() {
        for (int i = 0; i < objectsSize; i++) {
            final D object = objects.get(i);
            for (int j = i + 1; j < objectsSize; j += SOLVER_GRANULARITY) {
                ecs.submit(new DistanceSolver(j, object));
                submittedSolvers.incrementAndGet();
            }
        }
        submittedAllSolvers = true;
    }

    private class DistanceSolver implements Callable<Double> {
        private final int j;
        private D object;

        public DistanceSolver(int j, D object) {
            this.j = j;
            this.object = object;
        }

        @Override
        public Double call() throws Exception {
            double tempMaximum = 0;
            for (int jj = j; jj < j + SOLVER_GRANULARITY && jj < objectsSize; ++jj) {
                tempMaximum = FastMath.max(object.distance(objects.get(jj)), tempMaximum);
            }
            return tempMaximum;
        }
    }
}
