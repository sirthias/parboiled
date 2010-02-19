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
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import com.google.common.base.Preconditions;

class ParserMethod extends MethodNode {

    public final Class<?> ownerClass;
    private InstructionGraphNode[] instructionGraphNodes;
    private List<InstructionSubSet> instructionSubSets;

    public ParserMethod(Class<?> ownerClass, int access, String name, String desc, String signature,
                        String[] exceptions) {
        super(access, name, desc, signature, exceptions);
        this.ownerClass = ownerClass;
    }

    public InstructionGraphNode[] getInstructionGraphNodes() {
        if (instructionGraphNodes == null) {
            instructionGraphNodes = new InstructionGraphNode[instructions.size()];
        }
        return instructionGraphNodes;
    }

    public InstructionGraphNode getReturnNode() {
        InstructionGraphNode node = instructionGraphNodes[instructionGraphNodes.length - 2];
        Preconditions.checkState(node == null || node.instruction.getOpcode() == Opcodes.ARETURN);
        return node;
    }

    public boolean hasAccess(int access) {
        return (this.access & access) > 0;
    }

    public boolean hasActions() {
        return instructionSubSets != null;
    }

    public List<InstructionSubSet> getInstructionSubSets() {
        return instructionSubSets;
    }

    protected void setInstructionSubSets(List<InstructionSubSet> instructionSubSets) {
        this.instructionSubSets = instructionSubSets;
    }

}
