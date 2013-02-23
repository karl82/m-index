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

import cz.rank.vsfs.mindex.util.Generators;
import cz.rank.vsfs.mindex.util.PerfLogger;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Karel Rank
 */
public class MaximumDistancePerfTest {
    public static final int DEFAULT_TEST_INVOCATIONS = 20;
    private static final Integer[] DIMENSIONS = {1,
                                                 2,
                                                 4,
                                                 8,
                                                 16,
                                                 32,
                                                 64};
    private static final Integer[] OBJECTS_COUNTS = {1000,
                                                     10000,
                                                     50000,
                                                     100000};
    private static final Logger logger = LoggerFactory.getLogger(MaximumDistancePerfTest.class);
    private static final StopWatch stopWatch = new Slf4JStopWatch(PerfLogger.LOGGER);

    @BeforeSuite
    public void logJvmInfo() {
        PerfLogger.logJvmInfo();
    }

    @BeforeClass
    public void warmUp() {
        logger.info("Performing JVM warm up...");
        List<Vector> objects = Generators.createVectors(30000, 32, 10);
        MaximumDistance<Vector> maximumDistance = new MaximumDistance<>(objects);
        stopWatch.start("WARM UP");
        maximumDistance.calculate();
        stopWatch.stop("WARM UP");
        logger.info("JVM warm up done...");
    }

    @AfterMethod
    public void performGc() throws InterruptedException {
        logger.info("Performing GC...");
        System.gc();

        TimeUnit.SECONDS.sleep(2);
        logger.info("GC done...");
    }

    @DataProvider(name = "maximumDistancePerfData")
    public Object[][] maximumDistancePerfData() {
        final int coresAvailable = Runtime.getRuntime().availableProcessors();
        List<TestParams[]> params = new ArrayList<>();

        for (Integer dimension : DIMENSIONS) {
            for (Integer objectsCount : OBJECTS_COUNTS) {
                for (int threads = 1; threads <= coresAvailable; threads++) {
                    params.add(new TestParams[]{new TestParams(dimension, objectsCount, threads,
                                                               DEFAULT_TEST_INVOCATIONS)});
                }
            }
        }
        return params.toArray(new Object[params.size()][1]);
    }

    @Test(dataProvider = "maximumDistancePerfData")
    public void test(TestParams params) {
        List<Vector> objects = Generators.createVectors(params.objectsCount, params.dimension, 10);

        for (int invocation = 1; invocation <= params.invocations; invocation++) {
            MaximumDistance<Vector> maximumDistance = new MaximumDistance<Vector>(objects, params.threads);

            stopWatch.start(params.toString(), Integer.toString(invocation));
            maximumDistance.calculate();
            stopWatch.stop(params.toString(), Integer.toString(invocation));
        }
    }

    private static class TestParams {
        private final int dimension;
        private final int objectsCount;
        private final int threads;
        private final int invocations;

        public TestParams(int dimension, int objectsCount, int threads, int invocations) {
            this.dimension = dimension;
            this.objectsCount = objectsCount;
            this.threads = threads;
            this.invocations = invocations;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("");
            sb.append("{dimension=").append(dimension);
            sb.append(", objectsCount=").append(objectsCount);
            sb.append(", threads=").append(threads);
            sb.append('}');
            return sb.toString();
        }
    }
}
