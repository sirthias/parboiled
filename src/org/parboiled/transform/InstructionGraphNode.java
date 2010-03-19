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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.ContextAware;

import java.util.ArrayList;
import java.util.List;

import static org.parboiled.transform.AsmUtils.*;

class InstructionGraphNode implements Value {

    public final AbstractInsnNode instruction;
    public final int instructionIndex;
    public final BasicValue basicValue;
    public final List<Value> predecessors = new ArrayList<Value>();
    public final boolean isAction;
    public final boolean isRuleCreation;
    public final boolean isContextSwitch;
    public final boolean isCallOnContextAware;

    public InstructionGraphNode(ParserClassNode classNode, AbstractInsnNode instruction, int instructionIndex,
                                BasicValue resultValue, List<Value> predecessors) {
        this.instruction = instruction;
        this.instructionIndex = instructionIndex;
        this.basicValue = resultValue;
        this.isAction = isCallToBooleanValueOfZ(instruction);
        this.isRuleCreation = isRuleCreation(instruction);
        this.isContextSwitch = isContextSwitch(classNode);
        this.isCallOnContextAware = isCallOnContextAware(classNode);
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

    private boolean isContextSwitch(ParserClassNode classNode) {
        if (instruction.getType() == AbstractInsnNode.METHOD_INSN) {
            MethodInsnNode mi = (MethodInsnNode) instruction;
            return "UP/UP2/UP3/UP4/UP5/UP6/DOWN/DOWN2/DOWN3/DOWN4/DOWN5/DOWN6".contains(mi.name) &&
                    "(Ljava/lang/Object;)Ljava/lang/Object;".equals(mi.desc) && classNode.isOwnerOf(mi);
        }
        return false;
    }

    private boolean isCallOnContextAware(ParserClassNode classNode) {
        if (instruction instanceof MethodInsnNode) {
            MethodInsnNode methodInsn = (MethodInsnNode) instruction;
            if (methodInsn.getOpcode() == Opcodes.INVOKEVIRTUAL || methodInsn.getOpcode() == Opcodes.INVOKEINTERFACE) {
                return classNode.isOwnerOf(methodInsn) ||
                        ContextAware.class.isAssignableFrom(getClassForInternalName(methodInsn.owner));
            }
        }
        return false;
    }

}
