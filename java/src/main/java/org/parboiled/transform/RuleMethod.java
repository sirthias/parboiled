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

import static org.objectweb.asm.Opcodes.*;
import static org.parboiled.common.Preconditions.*;
import static org.parboiled.transform.AsmUtils.*;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.BaseParser;
import org.parboiled.common.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

class RuleMethod extends MethodNode {

    private final List<InstructionGroup> groups = new ArrayList<InstructionGroup>();
    private final List<LabelNode> usedLabels = new ArrayList<LabelNode>();

    private final Class<?> ownerClass;
    private boolean containsImplicitActions; // calls to Boolean.valueOf(boolean)
    private boolean containsExplicitActions; // calls to BaseParser.ACTION(boolean)
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
    private boolean bodyRewritten;
    private boolean skipGeneration;
    
    private BitSet actionParams;
    private List<InstructionGraphNode> rulesWithActionParams;
	private List<Type> actionVariableTypes;

    public RuleMethod(Class<?> ownerClass, int access, String name, String desc, String signature, String[] exceptions,
                      boolean hasExplicitActionOnlyAnno, boolean hasDontLabelAnno, boolean hasSkipActionsInPredicates) {
        super(access, name, desc, signature, exceptions);
        this.ownerClass = ownerClass;

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

    public boolean containsVarInitializers() {
    	return rulesWithActionParams != null;
    }

    public boolean containsPotentialSuperCalls() {
        return containsPotentialSuperCalls;
    }
    
    public BitSet getActionParams() {
        return actionParams;
    }
    
    public List<InstructionGraphNode> getRuleCallsWithActionParams() {
        return rulesWithActionParams;
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
    
    public InstructionGraphNode insertGraphNode(AbstractInsnNode insn, BasicValue resultValue, List<Value> predecessors) {
    	graphNodes.add(instructions.indexOf(insn), null);
    	return setGraphNode(insn, resultValue, predecessors);
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
            
            if (node.isCallToRuleWithActionParams()) {
            	getRuleCallsWithActionParams().add(node);
            }
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
			case INVOKEVIRTUAL:
				if (rulesWithActionParams == null && AsmUtils.hasActionParams(AsmUtils.getClassMethod(owner, name, desc))) {
					rulesWithActionParams = new ArrayList<InstructionGraphNode>();
				}
				break;
            case INVOKESPECIAL:
                if (isAssignableTo(owner, BaseParser.class)) {
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
    public void visitVarInsn(int opcode, int var) {
    	// actions that assign values to action variables
    	if (!hasExplicitActionOnlyAnnotation && !ownerClass.equals(BaseParser.class)) {
    		containsImplicitActions |= ISTORE <= opcode && opcode < IASTORE;
    	}
    	
    	super.visitVarInsn(opcode, var);
    }
    
	@Override
	public void visitEnd() {
		super.visitEnd();

		this.actionParams = analyzeActionParams();
		int parameterCount = Type.getArgumentTypes(desc).length;
		this.hasCachedAnnotation = actionParams.cardinality() == parameterCount;
	}
    
	private BitSet analyzeActionParams() {
		BitSet actionParams = new BitSet();
		if (visibleParameterAnnotations != null) {
			for (int param = 0; param < visibleParameterAnnotations.length; param++) {
				List<?> annotations = visibleParameterAnnotations[param];
				if (annotations != null) {
					for (Object annotation : annotations) {
						if (((AnnotationNode) annotation).desc.equals(Types.VAR_ANNOTATION.getDescriptor())) {
							actionParams.set(param + 1);
						}
					}
				}
			}
		}

		return actionParams;
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

    public List<Type> getActionVariableTypes() {
		return actionVariableTypes;
	}
    
	public void setActionVariableTypes(List<Type> actionVariableTypes) {
		this.actionVariableTypes = actionVariableTypes;
	}

	public void suppressNode() {
        hasSuppressNodeAnnotation = true;
    }
    
}
