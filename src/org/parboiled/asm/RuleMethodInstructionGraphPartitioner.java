/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.common.DisjointIndexSet;
import org.parboiled.support.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleMethodInstructionGraphPartitioner implements ClassTransformer {

    private final ClassTransformer nextTransformer;

    public RuleMethodInstructionGraphPartitioner(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public Class<?> transform(ParserClassNode classNode) throws Exception {
        for (RuleMethodInfo methodInfo : classNode.methodInfos) {
            if (hasActions(methodInfo)) {
                List<InstructionSubSet> subSets = partitionInstructionGraph(methodInfo);
                methodInfo.setInstructionSubSets(subSets);
            }
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : null;
    }

    private boolean hasActions(RuleMethodInfo methodInfo) {
        for (InstructionGraphNode node : methodInfo.instructionGraphNodes) {
            if (node.isAction) return true;
        }
        return false;
    }

    private List<InstructionSubSet> partitionInstructionGraph(@NotNull RuleMethodInfo methodInfo) {
        InstructionGraphNode[] graphNodes = methodInfo.instructionGraphNodes;
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
                    "Illegal action construct in parser rule method '%s'", methodInfo.method.name);

            instructionSubSets.add(subSet);
        }
        return instructionSubSets;
    }

}