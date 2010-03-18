/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.transform;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.common.DisjointIndexSet;
import org.parboiled.support.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Partitions the instructions graphs of a RuleMethod into its InstructionSubSets
 */
class InstructionGraphPartitioner implements MethodTransformer {

    private final MethodTransformer next;

    public InstructionGraphPartitioner(MethodTransformer next) {
        this.next = next;
    }

    public void transform(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        InstructionGraphNode[] graphNodes = method.getInstructionGraphNodes();
        DisjointIndexSet indexSet = new DisjointIndexSet(graphNodes.length);
        boolean[] actionMarkers = new boolean[graphNodes.length];

        for (int i = graphNodes.length - 1; i >= 0; i--) {
            InstructionGraphNode node = graphNodes[i];
            for (Value predecessor : node.predecessors) {
                if (predecessor instanceof InstructionGraphNode) {
                    InstructionGraphNode pred = (InstructionGraphNode) predecessor;
                    boolean nodeInActionPartition = node.isAction || node.isContextSwitch ||
                            actionMarkers[indexSet.getRepresentative(node.instructionIndex)];
                    boolean predInActionPartition = pred.isAction || pred.isContextSwitch ||
                            actionMarkers[indexSet.getRepresentative(pred.instructionIndex)];

                    // do not join on edges pointing from non-action partitions to action predecessors
                    if (!nodeInActionPartition && predInActionPartition) {
                        continue;
                    }

                    int newRep = indexSet.merge(node.instructionIndex, pred.instructionIndex);
                    actionMarkers[newRep] = nodeInActionPartition || predInActionPartition;
                }
            }
        }

        List<InstructionSubSet> instructionSubSets = new ArrayList<InstructionSubSet>();
        for (Map.Entry<Integer, int[]> entry : indexSet.getSubSets().entrySet()) {
            int[] instructions = entry.getValue();
            InstructionSubSet subSet = new InstructionSubSet(
                    actionMarkers[entry.getKey()],
                    instructions[0],
                    instructions[instructions.length - 1]
            );

            // all instructions in an action subset have to form a continuous block,
            // i.e. there must not be any indices missing
            Checks.ensure(!subSet.isActionSet || subSet.lastIndex - subSet.firstIndex == instructions.length - 1,
                    "Illegal action construct in parser rule method '%s'", method.name);

            instructionSubSets.add(subSet);
        }

        method.setInstructionSubSets(instructionSubSets);

        if (next != null) next.transform(classNode, method);
    }

}