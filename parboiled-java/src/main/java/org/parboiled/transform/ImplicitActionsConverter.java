/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

package org.parboiled.transform;

import static org.parboiled.common.Preconditions.*;
import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.parboiled.transform.AsmUtils.isBooleanValueOfZ;

/**
 * Makes all implicit action expressions in a rule method explicit.
 */
class ImplicitActionsConverter implements RuleMethodProcessor {

    private final Set<InstructionGraphNode> covered = new HashSet<InstructionGraphNode>();
    private RuleMethod method;

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.containsImplicitActions();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        this.method = checkArgNotNull(method, "method");
        covered.clear();
        walkNode(method.getReturnInstructionNode());
        method.setContainsImplicitActions(false);
    }

    private void walkNode(InstructionGraphNode node) {
        if (covered.contains(node)) return;
        covered.add(node);

        if (isImplicitAction(node)) {
            replaceWithActionWrapper(node);
            method.setContainsExplicitActions(true);
            return;
        }
        if (!node.isActionRoot()) {
            for (InstructionGraphNode predecessor : node.getPredecessors()) {
                walkNode(predecessor);
            }
        }
    }

    private void replaceWithActionWrapper(InstructionGraphNode node) {
        MethodInsnNode insn = createActionWrappingInsn();
        method.instructions.set(node.getInstruction(), insn);
        node.setIsActionRoot();
        node.setInstruction(insn);
    }

    private boolean isImplicitAction(InstructionGraphNode node) {
        // an implicit action must be a call to Boolean.valueOf(boolean)
        if (!isBooleanValueOfZ(node.getInstruction())) return false;

        // it must have exactly one other instruction that depends on it
        List<InstructionGraphNode> dependents = getDependents(node);
        if (dependents.size() != 1) return false;

        // this dependent instruction must be rule method call
        InstructionGraphNode dependent = dependents.get(0);
        return isObjectArgumentToRuleCreatingMethodCall(node, dependent) || isStoredIntoObjectArray(dependent);
    }

    private boolean isObjectArgumentToRuleCreatingMethodCall(InstructionGraphNode node,
                                                             InstructionGraphNode dependent) {
        // is the single dependent a method call ?
        AbstractInsnNode insn = dependent.getInstruction();
        if (insn.getType() != AbstractInsnNode.METHOD_INSN) return false;

        // Does this method call return a Rule ?
        MethodInsnNode mi = (MethodInsnNode) insn;
        if (!Types.RULE.equals(Type.getReturnType(mi.desc))) return false;

        // Doesthe result of the Boolean.valueOf(boolean) call correspond to an Object parameter ?
        Type[] argTypes = Type.getArgumentTypes(mi.desc);
        int argIndex = getArgumentIndex(dependent, node);
        checkState(argIndex < argTypes.length);
        return "java/lang/Object".equals(argTypes[argIndex].getInternalName());
    }

    private boolean isStoredIntoObjectArray(InstructionGraphNode dependent) {
        // is the single dependent an AASTORE instruction ?
        AbstractInsnNode insn = dependent.getInstruction();
        if (insn.getOpcode() != AASTORE) return false;

        // Does this instruction store into an array of Object ?
        List<InstructionGraphNode> dependents = getDependents(dependent);
        checkState(dependents.size() == 1); // an AASTORE instruction should have exactly one dependent
        AbstractInsnNode newArrayInsn = dependents.get(0).getInstruction();
        checkState(newArrayInsn.getOpcode() == ANEWARRAY); // which should be a n ANEWARRAY instruction
        return "java/lang/Object".equals(((TypeInsnNode) newArrayInsn).desc);
    }

    private int getArgumentIndex(InstructionGraphNode callNode, InstructionGraphNode predecessor) {
        int startIndex = callNode.getInstruction().getOpcode() == INVOKESTATIC ? 0 : 1;
        for (int i = startIndex; i < callNode.getPredecessors().size(); i++) {
            InstructionGraphNode argumentNode = callNode.getPredecessors().get(i);
            if (predecessor.equals(argumentNode)) {
                return i - startIndex;
            }
        }
        throw new IllegalStateException();
    }

    private List<InstructionGraphNode> getDependents(InstructionGraphNode predecessor) {
        List<InstructionGraphNode> dependents = new ArrayList<InstructionGraphNode>();
        for (InstructionGraphNode node : method.getGraphNodes()) {
            if (node.getPredecessors().contains(predecessor)) {
                dependents.add(node);
            }
        }
        return dependents;
    }

    private MethodInsnNode createActionWrappingInsn() {
        return new MethodInsnNode(INVOKESTATIC, Types.BASE_PARSER.getInternalName(), "ACTION",
                "(Z)" + Types.ACTION_DESC);
    }

}