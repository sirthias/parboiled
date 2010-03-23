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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

import java.util.List;

import static org.parboiled.transform.AsmUtils.*;

class RuleMethod extends MethodNode implements Opcodes, Types {

    private final Class<?> ownerClass;
    private final List<InstructionGroup> groups = Lists.newArrayList();
    private final List<LabelNode> usedLabels = Lists.newArrayList();

    private boolean containsImplicitActions; // calls to Boolean.valueOf(boolean)
    private boolean containsActions; // calls to BaseParser.ACTION(boolean)
    private boolean containsCaptures; // calls to BaseParser.CAPTURE(boolean)
    private boolean hasExplicitActionOnlyAnnotation;
    private boolean hasCachedAnnotation;
    private boolean hasLabelAnnotation;
    private boolean hasLeafAnnotation;
    private int numberOfReturns;
    private InstructionGraphNode returnInstructionNode;

    private List<InstructionGraphNode> graphNodes;

    public RuleMethod(Class<?> ownerClass, int access, String name, String desc, String signature,
                      String[] exceptions, boolean hasExplicitActionOnlyAnnotation) {
        super(access, name, desc, signature, exceptions);
        this.ownerClass = ownerClass;

        if (Type.getArgumentTypes(desc).length == 0) {
            // no parameter rules are automatically cached and labelled
            hasCachedAnnotation = true;
            hasLabelAnnotation = true;
        }

        this.hasExplicitActionOnlyAnnotation = hasExplicitActionOnlyAnnotation;
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    public List<InstructionGroup> getGroups() {
        return groups;
    }

    public List<LabelNode> getUsedLabels() {
        return usedLabels;
    }

    public boolean containsImplicitActions() {
        return containsImplicitActions;
    }

    public void setContainsImplicitActions(boolean containsImplicitActions) {
        this.containsImplicitActions = containsImplicitActions;
    }

    public boolean containsActions() {
        return containsActions;
    }

    public void setContainsActions(boolean containsActions) {
        this.containsActions = containsActions;
    }

    public boolean containsCaptures() {
        return containsCaptures;
    }

    public boolean hasCachedAnnotation() {
        return hasCachedAnnotation;
    }

    public boolean hasLabelAnnotation() {
        return hasLabelAnnotation;
    }

    public boolean hasLeafAnnotation() {
        return hasLeafAnnotation;
    }

    public int getNumberOfReturns() {
        return numberOfReturns;
    }

    public InstructionGraphNode getReturnInstructionNode() {
        return returnInstructionNode;
    }

    public void setReturnInstructionNode(InstructionGraphNode returnInstructionNode) {
        this.returnInstructionNode = returnInstructionNode;
    }

    public InstructionGraphNode getGraphNode(AbstractInsnNode insn) {
        return graphNodes != null ? graphNodes.get(instructions.indexOf(insn)) : null;
    }

    public InstructionGraphNode setGraphNode(AbstractInsnNode insn, BasicValue resultValue, List<Value> predecessors) {
        if (graphNodes == null) {
            // initialize with a list of null values
            graphNodes = Lists.newArrayList(new InstructionGraphNode[instructions.size()]);
        }
        int index = instructions.indexOf(insn);
        InstructionGraphNode node = new InstructionGraphNode(insn, index, resultValue, predecessors);
        graphNodes.set(index, node);
        return node;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (EXPLICIT_ACTIONS_ONLY_DESC.equals(desc)) {
            hasExplicitActionOnlyAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (CACHED_DESC.equals(desc)) {
            hasCachedAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (LEAF_DESC.equals(desc)) {
            hasLeafAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (LABEL_DESC.equals(desc)) {
            hasLabelAnnotation = true;
        }
        return visible ? super.visitAnnotation(desc, true) : null; // only keep visible annotations
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (!hasExplicitActionOnlyAnnotation && opcode == INVOKESTATIC && isBooleanValueOfZ(owner, name, desc)) {
            containsImplicitActions = true;
        }
        if (opcode == INVOKEVIRTUAL && isActionRoot(owner, name)) {
            containsActions = true;
        }
        if (opcode == INVOKEVIRTUAL && isCaptureRoot(owner, name)) {
            containsCaptures = true;
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == ARETURN) numberOfReturns++;
        super.visitInsn(opcode);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        usedLabels.add(getLabelNode(label));
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // do not record line numbers
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        // do not add local variables
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // do not record old max values
    }

    @Override
    public String toString() {
        return name;
    }

}
