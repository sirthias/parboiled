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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.AbstractVisitor;

import java.util.ArrayList;
import java.util.List;

class InstructionGraphNode implements Value {

    public final AbstractInsnNode instruction;
    public final int instructionIndex;
    public final BasicValue basicValue;
    public final List<Value> predecessors = new ArrayList<Value>();
    public final boolean isAction;
    public final boolean isContextSwitch;
    public final boolean isCallOnContextAware;
    public final boolean isRuleCreation;

    public InstructionGraphNode(AbstractInsnNode instruction, int instructionIndex, BasicValue resultValue,
                                List<Value> predecessors, boolean isAction, boolean contextSwitch,
                                boolean callOnContextAware, boolean ruleCreation) {
        this.instruction = instruction;
        this.instructionIndex = instructionIndex;
        this.basicValue = resultValue;
        this.isAction = isAction;
        isContextSwitch = contextSwitch;
        isCallOnContextAware = callOnContextAware;
        isRuleCreation = ruleCreation;
        this.predecessors.addAll(predecessors);
    }

    public int getSize() {
        return basicValue.getSize();
    }

    @Override
    public String toString() {
        String label = instruction == null ? "null" :
                instruction.getOpcode() >= 0 ? AbstractVisitor.OPCODES[instruction.getOpcode()] :
                        instruction.getClass().getSimpleName();
        return new StringBuilder()
                .append("Insn ").append(instructionIndex).append(": ")
                .append(label)
                .append(" -> ")
                .append(basicValue != null ? basicValue.getType() : "null")
                .toString();
    }

    public InstructionGraphNode getEarlierstPredecessor() {
        InstructionGraphNode earliestPred = null;
        for (Value predecessor : predecessors) {
            if (predecessor instanceof InstructionGraphNode) {
                InstructionGraphNode predNode = (InstructionGraphNode) predecessor;
                if (earliestPred == null || earliestPred.instructionIndex > predNode.instructionIndex) {
                    earliestPred = predNode;
                }
            }
        }
        return earliestPred;
    }

}
