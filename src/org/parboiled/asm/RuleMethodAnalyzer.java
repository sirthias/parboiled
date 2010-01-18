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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.common.DisjointIndexSet;
import org.parboiled.common.Preconditions;
import org.parboiled.support.Checks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RuleMethodAnalyzer {

    private final ParserClassNode classNode;

    public RuleMethodAnalyzer(@NotNull ParserClassNode classNode) {
        this.classNode = classNode;
    }

    @SuppressWarnings({"unchecked"})
    public List<RuleMethodInfo> analyzeRuleMethods() throws AnalyzerException {
        List<RuleMethodInfo> methodInfos = new ArrayList<RuleMethodInfo>(classNode.methods.size());
        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            RuleMethodInfo methodInfo = analyzeRuleMethod(method);
            methodInfos.add(methodInfo);

            if (hasActions(methodInfo)) {
                List<InstructionSubSet> subSets = partitionInstructionGraph(methodInfo);
                methodInfo.setInstructionSubSets(subSets);
            }
        }
        return methodInfos;
    }

    private RuleMethodInfo analyzeRuleMethod(@NotNull MethodNode method) throws AnalyzerException {
        Checks.ensure(method.maxLocals == 1, "Parser rule method '%s()' contains local variables, " +
                "which are not allowed in rule defining methods", method.name);

        RuleMethodInfo methodInfo = new RuleMethodInfo(method);
        final RuleMethodInterpreter interpreter = new RuleMethodInterpreter(classNode, methodInfo);

        new Analyzer(interpreter) {
            @Override
            protected void newControlFlowEdge(int insn, int successor) {
                interpreter.newControlFlowEdge(insn, successor);
            }
        }.analyze(classNode.name, method);

        interpreter.finish();

        return methodInfo;
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
                    boolean nodeInActionPartition = node.isAction || node.isMagicWrapper() ||
                            actionMarkers[indexSet.getRepresentative(node.instructionIndex)];
                    boolean predInActionPartition = pred.isAction || pred.isMagicWrapper() ||
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
            Preconditions.checkState(!subSet.isActionSet ||
                    subSet.lastIndex - subSet.firstIndex == instructions.length - 1);

            instructionSubSets.add(subSet);
        }
        return instructionSubSets;
    }

}
