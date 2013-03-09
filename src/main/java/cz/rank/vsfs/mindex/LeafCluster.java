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
public class LeafCluster<D extends Distanceable<D>> extends InternalCluster<D> {
    public static final Cluster NO_SUBCLUSTERS = new LeafCluster();

    public LeafCluster(Cluster<D> parentCluster, Index index) {
        super(parentCluster, index);
    }

    private LeafCluster() {
        super(null, new Index(1, 1));
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LeafCluster");
        sb.append("{index=").append(getIndex());
        sb.append(", rMin=").append(getKeyMin());
        sb.append(", rMax=").append(getKeyMax());
        sb.append('}');
        return sb.toString();

    }

    /**
     * Always returns {@link #NO_SUBCLUSTERS}
     *
     * @param pivot
     * @return {@link #NO_SUBCLUSTERS}
     */
    @Override
    public Cluster<D> getSubCluster(Pivot<D> pivot) {
        return NO_SUBCLUSTERS;
    }

    @Override
    public void accept(ClusterVisitor<D> visitor) {
        visitor.enterLeafCluster(this);
    }
}
