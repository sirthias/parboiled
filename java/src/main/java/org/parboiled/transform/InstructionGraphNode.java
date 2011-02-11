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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.objectweb.asm.util.AbstractVisitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A node in the instruction dependency graph.
 */
class InstructionGraphNode implements Value {

    private AbstractInsnNode instruction;
    private final BasicValue resultValue;
    private final List<InstructionGraphNode> predecessors = new ArrayList<InstructionGraphNode>();
    private final List<InstructionGraphNode> successors = new ArrayList<InstructionGraphNode>();
    private boolean isAssignmentAction;
    private boolean isActionRoot;
    private final boolean isCallToRuleWithActionParams;
    private boolean isActionParam;
    private final boolean isCallOnContextAware;
    private final boolean isXLoad;
    private final boolean isXStore;
    private InstructionGroup group;

    public InstructionGraphNode(AbstractInsnNode instruction, BasicValue resultValue) {
        this.instruction = instruction;
        this.resultValue = resultValue;
        this.isActionRoot = AsmUtils.isActionRoot(instruction);
        this.isCallToRuleWithActionParams = AsmUtils.isCallToRuleWithActionParams(instruction);
        this.isCallOnContextAware = AsmUtils.isCallOnContextAware(instruction);
        this.isXLoad = ILOAD <= instruction.getOpcode() && instruction.getOpcode() < IALOAD;
        this.isXStore = ISTORE <= instruction.getOpcode() && instruction.getOpcode() < IASTORE;
    }

    public int getSize() {
        return resultValue.getSize();
    }

    public AbstractInsnNode getInstruction() {
        return instruction;
    }

    public void setInstruction(AbstractInsnNode instruction) {
        this.instruction = instruction;
    }

    public BasicValue getResultValue() {
        return resultValue;
    }

    public List<InstructionGraphNode> getPredecessors() {
        return predecessors;
    }
    
    public List<InstructionGraphNode> getSuccessors() {
        return successors;
    }

    public InstructionGroup getGroup() {
        return group;
    }

    public void setGroup(InstructionGroup newGroup) {
        if (newGroup != group) {
            if (group != null) {
                group.getNodes().remove(this);
            }
            group = newGroup;
            if (group != null) {
                group.getNodes().add(this);
            }
        }
    }

    public boolean isActionRoot() {
        return isActionRoot;
    }

    public void setIsActionRoot() {
        isActionRoot = true;
    }
    
    public void setIsAssignmentAction() {
    	setIsActionRoot();
		this.isAssignmentAction = true;
	}
    
    public boolean isAssignmentAction() {
		return isAssignmentAction;
	}

    public boolean isActionParam() {
    	return isActionParam;
    }
    
    public void setIsActionParam() {
    	this.isActionParam = true;
    }
    
	public boolean isCallToRuleWithActionParams() {
		return isCallToRuleWithActionParams;
	}

    public boolean isCallOnContextAware() {
        return isCallOnContextAware;
    }

    public boolean isXLoad() {
        return isXLoad;
    }

    public boolean isXStore() {
        return isXStore;
    }

    public void addPredecessors(Collection<Value> preds) {
        checkArgNotNull(preds, "preds");
        for (Value pred : preds) {
            if (pred instanceof InstructionGraphNode) {
                addPredecessor(((InstructionGraphNode) pred));
            }
        }
    }
    
	public void addPredecessor(InstructionGraphNode node) {
		if (!predecessors.contains(node)) {
			predecessors.add(node);
			node.successors.add(this);
			if (isCallToRuleWithActionParams()) {
				int paramIndex = predecessors.indexOf(node);

				MethodInsnNode insn = (MethodInsnNode) getInstruction();
				if (insn.getOpcode() != INVOKESTATIC) {
					paramIndex--;
				}
				if (paramIndex >= 0 && AsmUtils.isActionParam(AsmUtils.getClassMethod(insn.owner, insn.name, insn.desc), paramIndex)) {
					node.setIsActionParam();
				}
			}
		}
	}
	
	public boolean removePredecessor(InstructionGraphNode node) {
		if (predecessors.remove(node)) {
			node.successors.remove(this);
			return true;
		}
		return false;
	}
	
    @Override
    public String toString() {
        return instruction.getOpcode() != -1 ? AbstractVisitor.OPCODES[instruction.getOpcode()] : super.toString();
    }

}
