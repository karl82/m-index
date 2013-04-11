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

import cz.rank.vsfs.mindex.util.PerfLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Karel Rank
 */
public abstract class MIndexPerfTest {
    public static final String MINDEX_REFERENCE_FILE = "mindex.reference.file";
    public static final int DEFAULT_TEST_INVOCATIONS = 20;
    protected static final int[] PIVOTS_COUNT = {
            10,
            20,
            30,
            50,
//            100,
//            200,
//            400
    };
    protected static final int[] CLUSTER_MAX_LEVEL = {
            2,
            3,
            4,
//            5,
    };
    protected static final double[] RANGES = {
//            0.01,
//            0.05,
            0.15,
//            0.3,
//            0.5,
//            1.0
    };
    protected static final int[] QUERY_OBJECTS = {
            //           10,
            //          20,
//            50,
            100,
//            1000
    };

    protected static final int[] BTREE_LEVEL = {
//            5,
//            10,
            50,
            100,
            500
    };

    private static final Logger logger = LoggerFactory.getLogger(MIndexPerfTest.class);
    protected final List<Vector> objects = new ArrayList<>();
    protected double maximumDistance;

    public void logJvmInfo() {
        PerfLogger.logJvmInfo();
    }

    protected void performGc() throws InterruptedException {
        logger.info("Performing GC...");
        System.gc();
        TimeUnit.SECONDS.sleep(2);
        logger.info("GC done...");
    }

    @BeforeClass
    public void loadReferenceDataAndWarmUp() throws IOException {
        logJvmInfo();
        Path referenceDataPath = new File(System.getProperty(MINDEX_REFERENCE_FILE)).toPath();

        logger.info("Reading reference data from: " + referenceDataPath);

        final List<String> lines = Files.readAllLines(referenceDataPath, Charset.defaultCharset());
        logger.info("Read " + lines.size() + " lines");
        for (String line : lines) {
            parseLineAndCreateVector(line);
        }

        calculateMaximumDistance();
        warmUp();
    }

    private void calculateMaximumDistance() {
        logger.info("Calculating maximum distance...");
        maximumDistance = new MaximumDistance<>(objects).calculate() * 1.15d;
        logger.info("Maximum distance is " + maximumDistance);

    }

    protected abstract void warmUp();

    private void parseLineAndCreateVector(String line) {
        // Skip empty lines
        if (line.trim().isEmpty()) {
            return;
        }

        final String[] values = line.split(" ");

        objects.add(new Vector(convertStringsIntoDoubles(values)));
    }

    private List<Double> convertStringsIntoDoubles(String[] values) {
        List<Double> doubleValues = new ArrayList<>(values.length);
        for (String value : values) {
            doubleValues.add(Double.valueOf(value));
        }

        return doubleValues;
    }
}
