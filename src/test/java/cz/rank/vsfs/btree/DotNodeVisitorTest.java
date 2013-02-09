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

package cz.rank.vsfs.btree;

import org.testng.annotations.Test;

/**
 * @author Karel Rank
 */
public class DotNodeVisitorTest {
    @Test(groups = "unit")
    public void test() {
        DotNodeVisitor visitor = new DotNodeVisitor();
        Node<Double, Double> node = internalNode();

        node.accept(visitor);

        String graphDefinition = visitor.getGraphDefinition();
    }

    private Node<Double, Double> internalNode() {
        InternalNode<Double, Double> fullNode = new InternalNode<>(2);

        final LeafNode<Double, Double> leafNode1 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode2 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode3 = new LeafNode<>(fullNode.getDegree());
        final LeafNode<Double, Double> leafNode4 = new LeafNode<>(fullNode.getDegree());

        fullNode.setChild(0, leafNode1);
        fullNode.setChild(1, 1d, leafNode2);
        fullNode.setChild(2, 2d, leafNode3);
        fullNode.setChild(3, 3d, leafNode4);

        InternalNode<Double, Double> newNode = new InternalNode<>(fullNode.getDegree());

        newNode.setChild(0, fullNode);

        fullNode.splitChild(newNode, 0);

        return newNode;
    }
}
