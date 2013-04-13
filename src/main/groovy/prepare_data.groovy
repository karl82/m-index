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


tagPostFix = args[0]
logFileName = args[1]
filesPrefix = args.length > 2 ? args[2] : ""

// pivotsCount=10, queryObjects=100, clusterMaxLevel=2, leafObjectsCount=10, range=0.15, btreeLevel=50
import cz.rank.vsfs.mindex.QueryStats
import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Tag {
    def pivotsCount
    def queryObjects
    def clusterLevel
    def range
    def btreeLevel
    def leafObjectsCount

    def toCsv() {
        "${pivotsCount},${queryObjects},${clusterLevel},${btreeLevel},${range},${leafObjectsCount ?: leafObjectsCount}"
    }
}
timeStats = [:]
def clusterStats = [:]
def filterStats = [:]

lineRegexp = /time.(\d+)..*tag.(.*). message/
clusterStatRegexp = /ClusterStats\{clusters=(\d+)}/
filterStatRegexp = /rangePivotDistanceFilter=(\d+), objectFilter=(\d+), pivotFilter=(\d+), doublePivotDistanceFilter=(\d+)}/
def multiLevelTagRegexp = /pivotsCount=(\d+), queryObjects=(\d+), clusterMaxLevel=(\d+), range=(\d+.\d+), btreeLevel=(\d+)}\.${tagPostFix}/
def dynamicTagRegexp = /pivotsCount=(\d+), queryObjects=(\d+), clusterMaxLevel=(\d+), leafObjectsCount=(\d+), range=(\d+.\d+), btreeLevel=(\d+)}\.${tagPostFix}/

def lastTag

def storeTimeStats(def tag, def time) {
    def times = timeStats[tag]
    if (!times) {
        times = []
        timeStats[tag] = times
    }
    times << time
}

filterLine = { line ->
    def timeMatcher = (line =~ lineRegexp)

    if (timeMatcher.find()) {
        time = Integer.valueOf(timeMatcher[0][1])
        tagMatcher = (timeMatcher[0][2] =~ multiLevelTagRegexp)

        if (tagMatcher.find()) {
            lastTag = new Tag(pivotsCount: tagMatcher[0][1],
                    queryObjects: tagMatcher[0][2],
                    clusterLevel: tagMatcher[0][3],
                    range: tagMatcher[0][4],
                    btreeLevel: tagMatcher[0][5])
            storeTimeStats(lastTag, time)
        } else {
            tagMatcher = (timeMatcher[0][2] =~ dynamicTagRegexp)
            if (tagMatcher.find()) {
                lastTag = new Tag(pivotsCount: tagMatcher[0][1],
                        queryObjects: tagMatcher[0][2],
                        clusterLevel: tagMatcher[0][3],
                        leafObjectsCount: tagMatcher[0][4],
                        range: tagMatcher[0][5],
                        btreeLevel: tagMatcher[0][6])
                storeTimeStats(lastTag, time)
            }
        }
    } else {
        if (lastTag) {
            def clusterStatMatcher = (line =~ clusterStatRegexp)
            if (clusterStatMatcher.find()) {
                clusterStats[lastTag] = Integer.valueOf(clusterStatMatcher[0][1])
            } else {
                def filterStatsMatcher = (line =~ filterStatRegexp)
                if (filterStatsMatcher.find()) {
                    filterStats[lastTag] = new QueryStats(rangePivotDistanceFilter: Integer.valueOf(filterStatsMatcher[0][1]),
                            objectFilter: Integer.valueOf(filterStatsMatcher[0][2]),
                            pivotFilter: Integer.valueOf(filterStatsMatcher[0][3]),
                            doublePivotDistanceFilter: Integer.valueOf(filterStatsMatcher[0][4]))
                }
            }
        }
    }
}

new File(logFileName).readLines().each { filterLine(it) }

// Print measured times in csv format
new File("${filesPrefix}.csv").withWriter { out ->
    out.writeLine "Pivots,Query Objects, Cluster Level, BTree degree, Range, Max Leaf Objects, Average, Standard Deviation"

    timeStats.each { k, v ->
        avg = v.sum() / v.size()

        def sumQuadr = 0
        v.each { val ->
            sumQuadr += (val - avg) * (val - avg)
        }
        stdDev = Math.sqrt(sumQuadr / (v.size()))
        out.writeLine "${k.toCsv()},${avg},${stdDev}"
    }
}

// Print statistics about queries and clusters
new File("${filesPrefix}_stats.csv").withWriter { out ->
    out.writeLine "Pivots,Query Objects, Cluster Level, BTree degree, Range, Max Leaf Objects, Created Clusters, Range Pivot Distance Filter, Object Filter, Pivot Filter, Double-Pivot Distance Filter"

    filterStats.each { k, v ->
        out.writeLine "${k.toCsv()},${clusterStats[k]},${v.rangePivotDistanceFilter},${v.objectFilter},${v.pivotFilter},${v.doublePivotDistanceFilter}"
    }
}

def seriesNameByBtree(def tag) {
    "p${tag.pivotsCount}cl${tag.clusterLevel}bt${tag.btreeLevel}"
}

def seriesNameByLeafObjectsCount(def tag) {
    "p${tag.pivotsCount}cl${tag.clusterLevel}${tag.leafObjectsCount ? 'l' + tag.leafObjectsCount : ''}"
}

println()

allPivotCounts = [] as Set

timeStats.each { k, v -> allPivotCounts.add(k.pivotsCount) }

allPivotCounts.each { p ->
    def btreeDataHeader = ["#BTree Level"]
    def btreeValues = [:]
    def fileName = "${filesPrefix}_p${p}"
    def datFileName = "${fileName}.dat"
    new File(datFileName).withWriter { out ->
        timeStats.each { k, v ->
            if (k.pivotsCount != p) {
                return
            }

            seriesName = seriesNameByLeafObjectsCount(k)
            if (!btreeDataHeader.contains(seriesName)) {
                btreeDataHeader << seriesName
            }

            btreeLevel = k.btreeLevel
            btreeTimes = btreeValues[btreeLevel]
            if (!btreeTimes) {
                btreeTimes = []
                btreeValues[btreeLevel] = btreeTimes
            }

            btreeTimes << (v.sum() / v.size())
        }

        btreeValues.sort()*.key

        out.writeLine btreeDataHeader.join(" ")
        btreeValues.each { k, v ->
            out.writeLine "${k} ${v.join(' ')}"
        }
    }
    new File("${fileName}.p").withWriter { out ->
        out.writeLine "set title \"Range Query pro p = ${p}\""
        out.writeLine 'set term postscript eps'
        out.writeLine "set output '${fileName}.eps'"
        out.writeLine 'set pointsize 1.5'
        out.writeLine "set xlabel 'stupen B-Tree'"
        out.writeLine "set ylabel 'Cas(ms)'"
        out.writeLine 'set logscale x'
        out.writeLine 'set key below'
        out.writeLine 'plot \\'

// Prepare data for GNU plot
        for (int i = 1; i < btreeDataHeader.size(); i++) {
            out.writeLine "\"${datFileName}\" using 1:${i + 1} title '${btreeDataHeader[i]}'with linespoints${i + 1 == btreeDataHeader.size() ? '' : ', \\'}"
        }
    }
}
