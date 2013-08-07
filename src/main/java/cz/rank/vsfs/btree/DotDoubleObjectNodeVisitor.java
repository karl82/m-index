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

import gnu.trove.list.TDoubleList;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author Karel Rank
 */
public class DotDoubleObjectNodeVisitor<V> implements DoubleObjectNodeVisitor<V> {
    private static final String graphHeader = "digraph g {\n" +
            "node[shape=record];\n";
    private static final String graphFooter = "}";

    private final StringBuilder nodesDefinitions = new StringBuilder();
    private final StringBuilder edgesDefinitions = new StringBuilder();

    private int internalNodeCounter = 0;
    private int leafNodeCounter = 0;
    private final Deque<String> fromDequeue = new ArrayDeque<>();
    private final Deque<String> prevLeafDequeue = new ArrayDeque<>();

    public String getGraphDefinition() {
        return graphHeader + nodesDefinitions + edgesDefinitions + graphFooter;
    }

    @Override
    public void enterInternalNode(List<DoubleObjectNode<V>> children, TDoubleList keys, int maxKeys) {
        String currentInternalNodeId = currentInternalNodeId();

        internalNodeCounter++;
        nodesDefinitions.append(generateInternalNodeDefinition(currentInternalNodeId, keys, maxKeys));

        if (!fromDequeue.isEmpty()) {
            createEdge(fromDequeue.getLast(), currentInternalNodeId);
        }
        for (int i = 0; i < children.size(); ++i) {
            DoubleObjectNode<V> node = children.get(i);
            fromDequeue.add(createFromInternalNode(currentInternalNodeId, i));

            node.accept(this);
        }

        fromDequeue.pollLast();
    }

    @Override
    public void enterLeafNode(TDoubleList keys, List<List<V>> values, int maxKeys) {
        String currentLeafNodeId = currentLeafNodeId();

        leafNodeCounter++;
        nodesDefinitions.append(generateLeafNodeDefinition(currentLeafNodeId, keys, values, maxKeys));

        if (!fromDequeue.isEmpty()) {
            createEdge(fromDequeue.getLast(), currentLeafNodeId);
        }

        if (!prevLeafDequeue.isEmpty()) {
            createEdge(prevLeafDequeue.pollLast(), currentLeafNodeId);
        }

        prevLeafDequeue.add(createFromLeafNode(currentLeafNodeId));
    }

    private void createEdge(String from, String to) {
        edgesDefinitions.append(from)
                .append(" -> ")
                .append(to).append(":parent")
                .append("\n");
    }

    private String generateLeafNodeDefinition(String nodeId, TDoubleList keys, List<List<V>> values, int maxKeys) {
        StringBuilder keySb = new StringBuilder("<parent>");
        StringBuilder ptrSb = new StringBuilder();

        final int keysSize = keys.size();
        for (int i = 0; i < maxKeys; ++i) {
            if (i < keysSize) {
                keySb.append(keys.get(i));
                ptrSb.append(values.get(i));
            }
            if (i + 1 < maxKeys) {
                keySb.append('|');
                ptrSb.append('|');
            }
        }

        ptrSb.append("|<nextLeaf>");
        return nodeId + "[label=\"{{" + keySb + "}|{" + ptrSb + "}}\"];\n";
    }

    private String currentLeafNodeId() {
        return "leafNode" + leafNodeCounter;
    }

    private String createFromInternalNode(String nodeId, int ptr) {
        return nodeId + ":ptr" + ptr;
    }

    private String createFromLeafNode(String nodeId) {
        return nodeId + ":nextLeaf";
    }

    private String generateInternalNodeDefinition(String nodeId, TDoubleList keys, int maxKeys) {
        StringBuilder keySb = new StringBuilder("<parent>");
        StringBuilder ptrSb = new StringBuilder();

        int ptrCounter = 0;
        final int keysSize = keys.size();
        for (int i = 0; i < maxKeys; ++i) {
            if (i < keysSize) {
                keySb.append(keys.get(i));
            }
            if (i + 1 < maxKeys) {
                keySb.append('|');
            }
            ptrSb.append("<ptr").append(ptrCounter++).append(">|");
        }

        return nodeId + "[label=\"{{" + keySb + "}|{" + ptrSb + "}}\"];\n";
    }

    private String currentInternalNodeId() {
        return "internalNode" + internalNodeCounter;
    }
}
