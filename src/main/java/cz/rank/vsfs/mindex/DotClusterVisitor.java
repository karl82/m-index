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
public class DotClusterVisitor<D extends Distanceable<D>> implements ClusterVisitor<D> {
    private static final String graphHeader = "digraph G {\n";
    private static final String graphFooter = "}";

    private final StringBuilder clusterDefinitions = new StringBuilder();

    @Override
    public String getGraphDefinition() {
        return graphHeader + clusterDefinitions + graphFooter;
    }

    @Override
    public void enterCluster(Cluster<D> cluster) {
        String currentIndex = cluster.getIndex().toString().replaceAll("[\\[\\]]", "\"");
        for (Cluster<D> subCluster : cluster.getSubClusters()) {
            createRelation(currentIndex, subCluster.getIndex().toString().replaceAll("[\\[\\]]", "\""));
            subCluster.accept(this);
        }
    }

    private void createRelation(String from, String to) {
        clusterDefinitions.append(from).append(" -> ").append(to).append(";\n");
    }
}
