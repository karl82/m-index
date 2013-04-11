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
def multiLevelTagRegexp = /pivotsCount=(\d+), queryObjects=(\d+), clusterMaxLevel=(\d+), range=(\d+.\d+), btreeLevel=(\d+)}\.${args[0]}/
def dynamicTagRegexp = /pivotsCount=(\d+), queryObjects=(\d+), clusterMaxLevel=(\d+), leafObjectsCount=(\d+), range=(\d+.\d+), btreeLevel=(\d+)}\.${args[0]}/

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

new File(args[1]).readLines().each { filterLine(it) }

println "Pivots,Query Objects, Cluster Level, BTree degree, Range, Max Leaf Objects, Average, Standard Deviation"

timeStats.each { k, v ->
    avg = v.sum() / v.size()

    def sumQuadr = 0
    v.each { val ->
        sumQuadr += (val - avg) * (val - avg)
    }
    stdDev = Math.sqrt(sumQuadr / (v.size()))
    println "${k.toCsv()},${avg},${stdDev}"
}

println()
println "Pivots,Query Objects, Cluster Level, BTree degree, Range, Max Leaf Objects, Created Clusters, Range Pivot Distance Filter, Object Filter, Pivot Filter, Double-Pivot Distance Filter"

filterStats.each { k, v ->
    println "${k.toCsv()},${clusterStats[k]},${v.rangePivotDistanceFilter},${v.objectFilter},${v.pivotFilter},${v.doublePivotDistanceFilter}"
}
