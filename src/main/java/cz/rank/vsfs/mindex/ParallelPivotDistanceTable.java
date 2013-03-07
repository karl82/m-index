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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Karel Rank
 */
public class ParallelPivotDistanceTable<D extends Distanceable<D>> extends AbstractPivotDistanceTable<D> {
    private static final Logger logger = LoggerFactory.getLogger(ParallelPivotDistanceTable.class);
    private final AtomicInteger submittedSolvers = new AtomicInteger();
    private final int parallelism = Runtime.getRuntime().availableProcessors();
    private volatile boolean submittedAllSolvers = false;

    public ParallelPivotDistanceTable(double maximumDistance, List<Pivot<D>> pivots, List<D> objects) {
        super(maximumDistance, pivots, objects);
    }

    public ParallelPivotDistanceTable(List<Pivot<D>> pivots, List<D> objects) {
        super(pivots, objects);
    }

    @Override
    public void calculate() {
        final ExecutorService es = Executors.newSingleThreadExecutor();
        final ExecutorService ecsPool = Executors.newFixedThreadPool(parallelism);
        final ExecutorCompletionService<PivotDistanceResult> ecs = new ExecutorCompletionService<>(
                ecsPool);

        final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

        es.execute(new Runnable() {
            @Override
            public void run() {
                int takenSolvers = 0;
                while (notAllSolversTaken(takenSolvers)) {
                    try {
                        final PivotDistanceResult result = ecs.take().get();
                        storeResult(result);
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

}
