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

import com.google.common.base.Preconditions;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.errors.GrammarException;
import org.parboiled.support.Checks;

import java.lang.reflect.Modifier;
import java.util.*;

import static org.parboiled.transform.AsmUtils.getOwnerField;
import static org.parboiled.transform.AsmUtils.getOwnerMethod;

class RuleMethodInterpreter extends BasicInterpreter {

    private final ParserClassNode classNode;
    private final RuleMethod method;
    private final List<int[]> additionalEdges = new ArrayList<int[]>();
    private final Map<String, Integer> memberModifiers = new HashMap<String, Integer>();

    public RuleMethodInterpreter(ParserClassNode classNode, RuleMethod method) {
        this.classNode = classNode;
        this.method = method;
    }

    @Override
    public Value newValue(Type type) {
        BasicValue basicValue = (BasicValue) super.newValue(type);
        if (basicValue == BasicValue.REFERENCE_VALUE) {
            basicValue = new BasicValue(type); // record the exact type and not just "Ljava/lang/Object"
        }
        return basicValue;
    }

    @Override
    public Value newOperation(AbstractInsnNode insn) throws AnalyzerException {
        return createNode(insn, super.newOperation(insn));
    }

    @Override
    public Value copyOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
        return createNode(insn, super.copyOperation(insn, value), value);
    }

    @Override
    public Value unaryOperation(AbstractInsnNode insn, Value value) throws AnalyzerException {
        verifyInstruction(insn);
        return createNode(insn, super.unaryOperation(insn, null), value);
    }

    @Override
    public Value binaryOperation(AbstractInsnNode insn, Value value1, Value value2) throws AnalyzerException {
        return createNode(insn, super.binaryOperation(insn, null, null), value1, value2);
    }

    @Override
    public Value ternaryOperation(AbstractInsnNode insn, Value v1, Value v2, Value v3) throws AnalyzerException {
        return createNode(insn, super.ternaryOperation(insn, null, null, null), v1, v2, v3);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {
        verifyInstruction(insn);
        return createNode(insn, super.naryOperation(insn, null), (Value[]) values.toArray(new Value[values.size()]));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
        Preconditions.checkState(insn.getOpcode() == Opcodes.ARETURN); // we return a Rule which is a reference type
        Checks.ensure(insn.getNext() == null ||
                insn.getNext().getType() == AbstractInsnNode.LABEL && insn.getNext().getNext() == null,
                "Illegal parser rule definition '" + method.name + "':\n" +
                        "Rule definition methods must contain exactly one return statement");
    }

    @Override
    public Value merge(Value v, Value w) {
        // we don't actually merge values but use the control flow detection to deal with conditionals
        return v;
    }

    public void newControlFlowEdge(int instructionIndex, int successorIndex) {
        switch (method.instructions.get(instructionIndex).getType()) {
            case AbstractInsnNode.LABEL:
            case AbstractInsnNode.JUMP_INSN:
                additionalEdges.add(new int[] {instructionIndex, successorIndex});
                return;
        }

        switch (method.instructions.get(successorIndex).getType()) {
            case AbstractInsnNode.JUMP_INSN:
                additionalEdges.add(new int[] {instructionIndex, successorIndex});
        }
    }

    public void finish() {
        // finally add all edges so far not included
        InstructionGraphNode[] instructionGraphNodes = method.getInstructionGraphNodes();
        for (int[] edge : additionalEdges) {
            InstructionGraphNode node = instructionGraphNodes[edge[0]];
            if (node == null) node = createNode(method.instructions.get(edge[0]), null);
            InstructionGraphNode succ = instructionGraphNodes[edge[1]];
            if (succ == null) succ = createNode(method.instructions.get(edge[1]), null);
            if (!succ.predecessors.contains(node)) succ.predecessors.add(node);
        }

        // set the finishing label, if existing
        int lastIndex = instructionGraphNodes.length - 1;
        AbstractInsnNode lastInstruction = method.instructions.get(lastIndex);
        if (instructionGraphNodes[lastIndex] == null) {
            Preconditions.checkState(lastInstruction.getType() == AbstractInsnNode.LABEL);
            createNode(lastInstruction, null);
        }
    }

    private InstructionGraphNode createNode(AbstractInsnNode insn, Value resultValue, Value... prevNodes) {
        int index = method.instructions.indexOf(insn);
        BasicValue resultBasicValue = getBasicValue(resultValue);
        InstructionGraphNode node =
                new InstructionGraphNode(classNode, insn, index, resultBasicValue, Arrays.asList(prevNodes));
        method.getInstructionGraphNodes()[index] = node;
        return node;
    }

    private BasicValue getBasicValue(Value resultValue) {
        return resultValue == null || resultValue instanceof BasicValue ?
                (BasicValue) resultValue : ((InstructionGraphNode) resultValue).basicValue;
    }

    private void verifyInstruction(AbstractInsnNode insn) {
        try {
            switch (insn.getOpcode()) {
                case PUTFIELD:
                case PUTSTATIC:
                    throw new GrammarException("Writing to a field is not allowed from within a parser rule method");

                case GETFIELD:
                case GETSTATIC:
                    FieldInsnNode field = (FieldInsnNode) insn;
                    Checks.ensure(isNoPrivateField(field.owner, field.name),
                            "Accessing a private field from within a parser rule method is not allowed.\n" +
                                    "Mark the field protected or package-private if you want to prevent public access!");
                    break;

                case INVOKEVIRTUAL:
                case INVOKESTATIC:
                case INVOKESPECIAL:
                case INVOKEINTERFACE:
                    MethodInsnNode method = (MethodInsnNode) insn;
                    Checks.ensure("<init>".equals(method.name) ||
                            isNoPrivateMethod(method.owner, method.name, method.desc),
                            "Calling a private method from within a parser rule method is not allowed.\n" +
                                    "Mark the method protected or package-private if you want to prevent public access!");
                    break;
            }
        } catch (GrammarException e) {
            throw new GrammarException(
                    "Illegal parser rule definition '" + method.name + "':\n" + e.getMessage());
        }
    }

    private boolean isNoPrivateField(String owner, String name) {
        String key = owner + '#' + name;
        Integer modifiers = memberModifiers.get(key);
        if (modifiers == null) {
            modifiers = getOwnerField(owner, name).getModifiers();
            memberModifiers.put(key, modifiers);
        }
        return !Modifier.isPrivate(modifiers);
    }

    private boolean isNoPrivateMethod(String owner, String name, String desc) {
        String key = owner + '#' + name + '#' + desc;
        Integer modifiers = memberModifiers.get(key);
        if (modifiers == null) {
            modifiers = getOwnerMethod(owner, name, desc).getModifiers();
            memberModifiers.put(key, modifiers);
        }
        return !Modifier.isPrivate(modifiers);
    }

}
