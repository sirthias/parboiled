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

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

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
