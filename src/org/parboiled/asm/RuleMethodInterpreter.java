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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.ContextAware;
import org.parboiled.common.Preconditions;
import org.parboiled.support.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class RuleMethodInterpreter extends BasicInterpreter {

    private static final String UP_DOWN_DESCRIPTOR =
            Type.getMethodDescriptor(Types.OBJECT_TYPE, new Type[] {Types.OBJECT_TYPE});

    private final ParserClassNode classNode;
    private final RuleMethodInfo methodInfo;
    private final List<int[]> additionalEdges = new ArrayList<int[]>();

    public RuleMethodInterpreter(ParserClassNode classNode, RuleMethodInfo methodInfo) {
        this.classNode = classNode;
        this.methodInfo = methodInfo;
    }

    public Value newValue(Type type) {
        BasicValue basicValue = (BasicValue) super.newValue(type);
        if (basicValue == BasicValue.REFERENCE_VALUE) {
            basicValue = new BasicValue(type); // record the exact type and not just "Ljava/lang/Object"
        }
        return basicValue;
    }

    public Value newOperation(AbstractInsnNode insn) throws AnalyzerException {
        return createNode(insn, super.newOperation(insn));
    }

    @Override
    public Value copyOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
        return createNode(insn, super.copyOperation(insn, value), value);
    }

    public Value unaryOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
        return createNode(insn, super.unaryOperation(insn, null), value);
    }

    public Value binaryOperation(AbstractInsnNode insn, Value value1, Value value2) throws AnalyzerException {
        return createNode(insn, super.binaryOperation(insn, null, null), value1, value2);
    }

    public Value ternaryOperation(AbstractInsnNode insn, Value v1, Value v2, Value v3) throws AnalyzerException {
        return createNode(insn, super.ternaryOperation(insn, null, null, null), v1, v2, v3);
    }

    @SuppressWarnings({"unchecked"})
    public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {
        return createNode(insn, super.naryOperation(insn, null), (Value[]) values.toArray(new Value[values.size()]));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
        Preconditions.checkState(insn.getOpcode() == Opcodes.ARETURN); // we return a Rule which is a reference type
        Checks.ensure(insn.getNext().getType() == AbstractInsnNode.LABEL && insn.getNext().getNext() == null,
                "Rule definitions must contain exactly one return statement");
    }

    public Value merge(Value v, Value w) {
        return v;
    }

    public void newControlFlowEdge(int instructionIndex, int successorIndex) {
        AbstractInsnNode instruction = methodInfo.method.instructions.get(instructionIndex);
        int instructionType = instruction.getType();
        if (instructionType == AbstractInsnNode.LABEL || instructionType == AbstractInsnNode.LINE ||
                instructionType == AbstractInsnNode.FRAME || instruction instanceof JumpInsnNode) {
            additionalEdges.add(new int[] {instructionIndex, successorIndex});
        }
    }

    public void finish() {
        // finally add all edges so far not included
        for (int[] edge : additionalEdges) {
            InstructionGraphNode node = methodInfo.instructionGraphNodes[edge[0]];
            if (node == null) node = createNode(methodInfo.method.instructions.get(edge[0]), null);
            InstructionGraphNode succ = methodInfo.instructionGraphNodes[edge[1]];
            if (succ == null) succ = createNode(methodInfo.method.instructions.get(edge[1]), null);
            succ.predecessors.add(node);
        }

        // set the finishing label
        int lastIndex = methodInfo.instructionGraphNodes.length - 1;
        AbstractInsnNode lastInstruction = methodInfo.method.instructions.get(lastIndex);
        Preconditions.checkState(methodInfo.instructionGraphNodes[lastIndex] == null);
        Preconditions.checkState(lastInstruction.getType() == AbstractInsnNode.LABEL);
        createNode(lastInstruction, null);
    }

    private InstructionGraphNode createNode(AbstractInsnNode insn, Value resultValue, Value... prevNodes) {
        int index = methodInfo.method.instructions.indexOf(insn);
        BasicValue resultBasicValue = getBasicValue(resultValue);
        InstructionGraphNode node = new InstructionGraphNode(
                insn,
                index,
                resultBasicValue,
                Arrays.asList(prevNodes),
                resultBasicValue != null && Types.BOOLEAN_TYPE.equals(resultBasicValue.getType()),
                isContextSwitch(insn),
                isCallOnContextAware(insn));
        methodInfo.instructionGraphNodes[index] = node;
        return node;
    }

    private BasicValue getBasicValue(Value resultValue) {
        return resultValue == null || resultValue instanceof BasicValue ?
                (BasicValue) resultValue : ((InstructionGraphNode) resultValue).basicValue;
    }

    private boolean isCallOnContextAware(AbstractInsnNode insn) {
        if (insn instanceof MethodInsnNode) {
            MethodInsnNode methodInsn = (MethodInsnNode) insn;
            if (methodInsn.getOpcode() == INVOKEVIRTUAL || methodInsn.getOpcode() == INVOKEINTERFACE) {
                if (isOwnerMethod(methodInsn)) return true;
                String targetClassName = methodInsn.owner.replace('/', '.');
                try {
                    Class<?> targetClass = Class.forName(targetClassName);
                    return ContextAware.class.isAssignableFrom(targetClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error loading class for rule method analysis", e);
                }
            }
        }
        return false;
    }

    private boolean isContextSwitch(AbstractInsnNode insn) {
        if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
            MethodInsnNode mi = (MethodInsnNode) insn;
            return ("UP".equals(mi.name) || "DOWN".equals(mi.name)) &&
                    UP_DOWN_DESCRIPTOR.equals(mi.desc) && isOwnerMethod(mi);
        }
        return false;
    }

    private boolean isOwnerMethod(MethodInsnNode methodInsn) {
        for (Type ownerType : classNode.ownerTypes) {
            if (ownerType.getInternalName().equals(methodInsn.owner)) return true;
        }
        return false;
    }

}
