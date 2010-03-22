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

import com.google.common.collect.Lists;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

import java.util.List;

/**
 * A node in the instruction dependency graph.
 */
class InstructionGraphNode implements Value {

    private final AbstractInsnNode instruction;
    private final int instructionIndex;
    private final BasicValue resultValue;
    private final List<Value> predecessors = Lists.newArrayList();
    private final List<InstructionGroup> groups = Lists.newArrayList();
    private final boolean isActionRoot;
    private final boolean isCaptureRoot;
    private final boolean isContextSwitch;
    private final boolean isCallOnContextAware;

    public InstructionGraphNode(AbstractInsnNode instruction, int instructionIndex,
                                BasicValue resultValue, List<Value> predecessors) {
        this.instruction = instruction;
        this.instructionIndex = instructionIndex;
        this.resultValue = resultValue;
        this.isActionRoot = AsmUtils.isActionRoot(instruction);
        this.isCaptureRoot = AsmUtils.isCaptureRoot(instruction);
        this.isContextSwitch = AsmUtils.isContextSwitch(instruction);
        this.isCallOnContextAware = AsmUtils.isCallOnContextAware(instruction);
        this.predecessors.addAll(predecessors);
    }

    public int getSize() {
        return resultValue.getSize();
    }

    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    public int getInstructionIndex() {
        return instructionIndex;
    }

    public BasicValue getResultValue() {
        return resultValue;
    }

    public List<Value> getPredecessors() {
        return predecessors;
    }

    public List<InstructionGroup> getGroups() {
        return groups;
    }

    public boolean isActionRoot() {
        return isActionRoot;
    }

    public boolean isCaptureRoot() {
        return isCaptureRoot;
    }

    public boolean isContextSwitch() {
        return isContextSwitch;
    }

    public boolean isCallOnContextAware() {
        return isCallOnContextAware;
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
