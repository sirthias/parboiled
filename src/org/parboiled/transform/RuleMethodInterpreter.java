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
import com.google.common.collect.Lists;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;

import java.util.Arrays;
import java.util.List;

class RuleMethodInterpreter extends BasicInterpreter implements Types {

    private final RuleMethod method;
    private final List<Edge> additionalEdges = Lists.newArrayList();

    public RuleMethodInterpreter(RuleMethod method) {
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
        return createNode(insn, super.naryOperation(insn, null), (Value[]) values.toArray(new Value[values.size()]));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
        Preconditions.checkState(insn.getOpcode() == Opcodes.ARETURN);
        Preconditions.checkState(unwrap(value).getType().equals(RULE));
        Preconditions.checkState(unwrap(expected).getType().equals(RULE));
        Preconditions.checkState(method.getReturnInstructionNode() == null);
        method.setReturnInstructionNode(createNode(insn, null, value));
    }

    private InstructionGraphNode createNode(AbstractInsnNode insn, Value resultValue, Value... prevNodes) {
        return method.setGraphNode(insn, unwrap(resultValue), Arrays.asList(prevNodes));
    }

    @Override
    public Value merge(Value v, Value w) {
        // we don't actually merge values but use the control flow detection to deal with conditionals
        return v;
    }

    public void newControlFlowEdge(int instructionIndex, int successorIndex) {
        AbstractInsnNode fromInsn = method.instructions.get(instructionIndex);
        AbstractInsnNode toInsn = method.instructions.get(successorIndex);
        switch (fromInsn.getType()) {
            case AbstractInsnNode.LABEL:
            case AbstractInsnNode.JUMP_INSN:
                additionalEdges.add(new Edge(fromInsn, toInsn));
                return;
        }

        switch (toInsn.getType()) {
            case AbstractInsnNode.JUMP_INSN:
                additionalEdges.add(new Edge(fromInsn, toInsn));
        }
    }

    public void finish() {
        // finally add all edges so far not included
        for (Edge edge : additionalEdges) {
            InstructionGraphNode node = method.getGraphNode(edge.from);
            if (node == null) node = createNode(edge.from, null);
            InstructionGraphNode succ = method.getGraphNode(edge.to);
            if (succ == null) succ = createNode(edge.to, null);
            if (!succ.getPredecessors().contains(node)) succ.getPredecessors().add(node);
        }
    }

    private BasicValue unwrap(Value resultValue) {
        return resultValue == null || resultValue instanceof BasicValue ?
                (BasicValue) resultValue : ((InstructionGraphNode) resultValue).getResultValue();
    }

    private class Edge {
        public final AbstractInsnNode from;
        public final AbstractInsnNode to;

        public Edge(AbstractInsnNode from, AbstractInsnNode to) {
            this.from = from;
            this.to = to;
        }
    }

}
