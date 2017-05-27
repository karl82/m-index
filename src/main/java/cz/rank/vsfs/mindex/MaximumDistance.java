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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private final int parallelism;

    /**
     * Use all available cores.
     */
    private final int objectsSize;

    private double maximum = 0d;

    public MaximumDistance(List<D> objects) {
        this.objects = objects;
        objectsSize = objects.size();
        parallelism = Runtime.getRuntime()
                             .availableProcessors();
    }

    public MaximumDistance(List<D> objects,
                           int parallelism) {
        this.objects = objects;
        objectsSize = objects.size();
        this.parallelism = parallelism;
    }

    public double calculate() {
        final ExecutorService ecsPool = Executors.newFixedThreadPool(parallelism);
        final ExecutorCompletionService<Double> ecs = new ExecutorCompletionService<>(ecsPool);
        int submittedSolvers = submitSolvers(ecs);
        int takenSolvers = 0;

        logger.info("Submitted {} solvers", submittedSolvers)
        ;
        while (takenSolvers++ < submittedSolvers) {
            try {
                Future<Double> result = ecs.take();
                maximum = FastMath.max(result.get(), maximum);
            } catch (InterruptedException e) {
                Thread.currentThread()
                      .interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException("Error during calculation maximum distance!", e);
            }
        }

        ecsPool.shutdown();

        return maximum;
    }

    private int submitSolvers(ExecutorCompletionService<Double> ecs) {
        int submittedSolvers = 0;
        for (int i = 0; i < objectsSize; i++) {
            final D object = objects.get(i);
            for (int j = i + 1; j < objectsSize; j += SOLVER_GRANULARITY) {
                logger.debug("Submitting {} for object {} with granularity {}", j, i, SOLVER_GRANULARITY);
                ecs.submit(new DistanceSolver(j, object));
                submittedSolvers++;
            }
        }
        return submittedSolvers;
    }

    private class DistanceSolver
            implements Callable<Double> {
        private final int j;

        private final int maxJ;

        private final D object;

        public DistanceSolver(int j,
                              D object) {
            this.j = j;
            maxJ = FastMath.min(j + SOLVER_GRANULARITY, objectsSize);
            this.object = object;
        }

        @Override
        public Double call() throws Exception {
            double tempMaximum = 0;
            for (int jj = j; jj < maxJ; ++jj) {
                tempMaximum = FastMath.max(object.distance(objects.get(jj)), tempMaximum);
            }
            logger.debug("Finished object {} with {} ", j - 1, maxJ);
            return tempMaximum;
        }
    }
}
