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

/**
 * @author Karel Rank
 */
public class PivotDistance<D extends Distanceable<D>> implements Comparable<PivotDistance> {
    private final Pivot<D> pivot;
    private final double distance;

    PivotDistance(Pivot<D> pivot, D object) {
        this.pivot = pivot;

        distance = pivot.distance(object);
    }

    private PivotDistance(double distance) {
        pivot = null;
        this.distance = distance;
    }

    public PivotDistance(double maximumDistance, Pivot<D> pivot, D object) {
        this.pivot = pivot;

        distance = pivot.distance(object) / maximumDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PivotDistance that = (PivotDistance) o;

        if (Double.compare(that.distance, distance) != 0) {
            return false;
        }
        if (!pivot.equals(that.pivot)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = pivot.hashCode();
        temp = distance != +0.0d ? Double.doubleToLongBits(distance) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public PivotDistance(Pivot<D> pivot, double distance) {
        this.pivot = pivot;
        this.distance = distance;
    }

    public Pivot<D> getPivot() {
        return pivot;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(PivotDistance clusterPivotDistance) {
        if (clusterPivotDistance == null) {
            throw new NullPointerException("PivotsDistance for comparison is null");
        }

        return Double.valueOf(distance).compareTo(clusterPivotDistance.getDistance());
    }


    public static <D extends Distanceable<D>> PivotDistance<D> maxClusterPivotDistance() {
        return new PivotDistance<>(Double.MAX_VALUE);
    }

    @Override
    public String toString() {
        return "PivotDistance{" +
                "pivot=" + pivot +
                ", distance=" + distance +
                '}';
    }
}
