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

import static org.parboiled.common.Preconditions.*;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.BaseParser;
import org.parboiled.common.StringUtils;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;
import static org.parboiled.transform.AsmUtils.*;

class RuleMethod extends MethodNode {

    private final List<InstructionGroup> groups = new ArrayList<InstructionGroup>();
    private final List<LabelNode> usedLabels = new ArrayList<LabelNode>();

    private final Class<?> ownerClass;
    private int parameterCount;
    private boolean containsImplicitActions; // calls to Boolean.valueOf(boolean)
    private boolean containsExplicitActions; // calls to BaseParser.ACTION(boolean)
    private boolean containsVars; // calls to Var.<init>(T)
    private boolean containsPotentialSuperCalls;
    private boolean hasDontExtend;
    private boolean hasExplicitActionOnlyAnnotation;
    private boolean hasCachedAnnotation;
    private boolean hasDontLabelAnnotation;
    private boolean hasSuppressNodeAnnotation;
    private boolean hasSuppressSubnodesAnnotation;
    private boolean hasSkipNodeAnnotation;
    private boolean hasMemoMismatchesAnnotation;
    private boolean hasSkipActionsInPredicatesAnnotation;
    private int numberOfReturns;
    private InstructionGraphNode returnInstructionNode;
    private List<InstructionGraphNode> graphNodes;
    private List<LocalVariableNode> localVarVariables;
    private boolean bodyRewritten;
    private boolean skipGeneration;

    public RuleMethod(Class<?> ownerClass, int access, String name, String desc, String signature, String[] exceptions,
                      boolean hasExplicitActionOnlyAnno, boolean hasDontLabelAnno, boolean hasSkipActionsInPredicates) {
        super(access, name, desc, signature, exceptions);
        this.ownerClass = ownerClass;
        parameterCount = Type.getArgumentTypes(desc).length;
        hasCachedAnnotation = parameterCount == 0;
        hasDontLabelAnnotation = hasDontLabelAnno;
        hasExplicitActionOnlyAnnotation = hasExplicitActionOnlyAnno;
        hasSkipActionsInPredicatesAnnotation = hasSkipActionsInPredicates;
        skipGeneration = isSuperMethod();
    }

    public List<InstructionGroup> getGroups() {
        return groups;
    }

    public List<LabelNode> getUsedLabels() {
        return usedLabels;
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    public boolean hasDontExtend() {
        return hasDontExtend;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    public boolean containsImplicitActions() {
        return containsImplicitActions;
    }

    public void setContainsImplicitActions(boolean containsImplicitActions) {
        this.containsImplicitActions = containsImplicitActions;
    }

    public boolean containsExplicitActions() {
        return containsExplicitActions;
    }

    public void setContainsExplicitActions(boolean containsExplicitActions) {
        this.containsExplicitActions = containsExplicitActions;
    }

    public boolean containsVars() {
        return containsVars;
    }

    public boolean containsPotentialSuperCalls() {
        return containsPotentialSuperCalls;
    }

    public boolean hasCachedAnnotation() {
        return hasCachedAnnotation;
    }

    public boolean hasDontLabelAnnotation() {
        return hasDontLabelAnnotation;
    }

    public boolean hasSuppressNodeAnnotation() {
        return hasSuppressNodeAnnotation;
    }

    public boolean hasSuppressSubnodesAnnotation() {
        return hasSuppressSubnodesAnnotation;
    }

    public boolean hasSkipActionsInPredicatesAnnotation() {
        return hasSkipActionsInPredicatesAnnotation;
    }

    public boolean hasSkipNodeAnnotation() {
        return hasSkipNodeAnnotation;
    }

    public boolean hasMemoMismatchesAnnotation() {
        return hasMemoMismatchesAnnotation;
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

    public List<InstructionGraphNode> getGraphNodes() {
        return graphNodes;
    }

    public List<LocalVariableNode> getLocalVarVariables() {
        return localVarVariables;
    }

    public boolean isBodyRewritten() {
        return bodyRewritten;
    }

    public void setBodyRewritten() {
        this.bodyRewritten = true;
    }

    public boolean isSuperMethod() {
        checkState(StringUtils.isNotEmpty(name));
        return name.charAt(0) == '$';
    }

    public InstructionGraphNode setGraphNode(AbstractInsnNode insn, BasicValue resultValue, List<Value> predecessors) {
        if (graphNodes == null) {
            // initialize with a list of null values
            graphNodes = new ArrayList<InstructionGraphNode>(
                    Arrays.asList(new InstructionGraphNode[instructions.size()]));
        }
        int index = instructions.indexOf(insn);
        InstructionGraphNode node = graphNodes.get(index);
        if (node == null) {
            node = new InstructionGraphNode(insn, resultValue);
            graphNodes.set(index, node);
        }
        node.addPredecessors(predecessors);
        return node;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Types.EXPLICIT_ACTIONS_ONLY_DESC.equals(desc)) {
            hasExplicitActionOnlyAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.CACHED_DESC.equals(desc)) {
            hasCachedAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.SUPPRESS_NODE_DESC.equals(desc)) {
            hasSuppressNodeAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.SUPPRESS_SUBNODES_DESC.equals(desc)) {
            hasSuppressSubnodesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.SKIP_NODE_DESC.equals(desc)) {
            hasSkipNodeAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.MEMO_MISMATCHES_DESC.equals(desc)) {
            hasMemoMismatchesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.SKIP_ACTIONS_IN_PREDICATES_DESC.equals(desc)) {
            hasSkipActionsInPredicatesAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.DONT_SKIP_ACTIONS_IN_PREDICATES_DESC.equals(desc)) {
            hasSkipActionsInPredicatesAnnotation = false;
            return null; // we do not need to record this annotation
        }
        if (Types.DONT_LABEL_DESC.equals(desc)) {
            hasDontLabelAnnotation = true;
            return null; // we do not need to record this annotation
        }
        if (Types.DONT_EXTEND_DESC.equals(desc)) {
            hasDontExtend = true;
            return null; // we do not need to record this annotation
        }
        return visible ? super.visitAnnotation(desc, true) : null; // only keep visible annotations
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        switch (opcode) {
            case INVOKESTATIC:
                if (!hasExplicitActionOnlyAnnotation && isBooleanValueOfZ(owner, name, desc)) {
                    containsImplicitActions = true;
                } else if (isActionRoot(owner, name)) {
                    containsExplicitActions = true;
                }
                break;

            case INVOKESPECIAL:
                if ("<init>".equals(name)) {
                    if (isVarRoot(owner, name, desc)) {
                        containsVars = true;
                    }
                } else if (isAssignableTo(owner, BaseParser.class)) {
                    containsPotentialSuperCalls = true;
                }
                break;
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
        // only remember the local variables of Type org.parboiled.support.Var that are not parameters
        if (index > parameterCount && Var.class.isAssignableFrom(getClassForType(Type.getType(desc)))) {
            if (localVarVariables == null) localVarVariables = new ArrayList<LocalVariableNode>();
            localVarVariables.add(new LocalVariableNode(name, desc, null, null, null, index));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void moveFlagsTo(RuleMethod overridingMethod) {
        checkArgNotNull(overridingMethod, "overridingMethod");
        overridingMethod.hasCachedAnnotation |= hasCachedAnnotation;
        overridingMethod.hasDontLabelAnnotation |= hasDontLabelAnnotation;
        overridingMethod.hasSuppressNodeAnnotation |= hasSuppressNodeAnnotation;
        overridingMethod.hasSuppressSubnodesAnnotation |= hasSuppressSubnodesAnnotation;
        overridingMethod.hasSkipNodeAnnotation |= hasSkipNodeAnnotation;
        hasCachedAnnotation = false;
        hasDontLabelAnnotation = true;
        hasSuppressNodeAnnotation = false;
        hasSuppressSubnodesAnnotation = false;
        hasSkipNodeAnnotation = false;
    }

    public boolean isGenerationSkipped() {
        return skipGeneration;
    }

    public void dontSkipGeneration() {
        skipGeneration = false;
    }

    public void suppressNode() {
        hasSuppressNodeAnnotation = true;
    }
    
}
