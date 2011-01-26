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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.support.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class RuleMethodInterpreter extends BasicInterpreter {

    private final RuleMethod method;
    private final List<Edge> additionalEdges = new ArrayList<Edge>();

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
	public Value copyOperation(AbstractInsnNode insn, Value value)
			throws AnalyzerException {
		Value node = createNode(insn, super.copyOperation(insn, value), value);

		// xSTORE instructions decouple different sets of action instructions
		if (insn.getType() == AbstractInsnNode.VAR_INSN
				&& insn.getOpcode() >= ISTORE && insn.getOpcode() <= ASTORE) {
			return unwrap(value);
		}

		return node;
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
        // this method is only called for xASTORE instructions, parameter v1 is the value corresponding to the
        // NEWARRAY, ANEWARRAY or MULTIANEWARRAY instruction having created the array, we need to insert a special
        // dependency edge from the array creator to this xSTORE instruction
        additionalEdges.add(new Edge(insn, findArrayCreatorPredecessor(v1)));
        return createNode(insn, super.ternaryOperation(insn, null, null, null), v1, v2, v3);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Value naryOperation(AbstractInsnNode insn, List values) throws AnalyzerException {
        return createNode(insn, super.naryOperation(insn, null), (Value[]) values.toArray(new Value[values.size()]));
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, Value value, Value expected) throws AnalyzerException {
        checkState(insn.getOpcode() == ARETURN);
        checkState(unwrap(value).getType().equals(Types.RULE));
        checkState(unwrap(expected).getType().equals(Types.RULE));
        checkState(method.getReturnInstructionNode() == null);
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
        if (fromInsn.getType() == AbstractInsnNode.LABEL || fromInsn.getType() == AbstractInsnNode.JUMP_INSN ||
                toInsn.getType() == AbstractInsnNode.LABEL || toInsn.getType() == AbstractInsnNode.JUMP_INSN) {
            additionalEdges.add(new Edge(fromInsn, toInsn));
        }
    }

    private AbstractInsnNode findArrayCreatorPredecessor(Value value) {
        String errorMessage = "Internal error during analysis of rule method '" + method.name +
                "', please report this error to info@parboiled.org! Thank you!";
        Checks.ensure(value instanceof InstructionGraphNode, errorMessage);
        InstructionGraphNode node = (InstructionGraphNode) value;
        while (true) {
            int opcode = node.getInstruction().getOpcode();
            if (opcode == ANEWARRAY || opcode == NEWARRAY || opcode == MULTIANEWARRAY) break;
            Checks.ensure(node.getPredecessors().size() == 1, errorMessage);
            node = node.getPredecessors().get(0);
        }
        return node.getInstruction();
    }

    public void finish() {
        // add all edges so far not included
        for (Edge edge : additionalEdges) {
            InstructionGraphNode node = getGraphNode(edge.from);
            if (node == null) node = createNode(edge.from, null);
            InstructionGraphNode succ = getGraphNode(edge.to);
            if (succ == null) succ = createNode(edge.to, null);
            succ.addPredecessor(node);
        }
    }

    private InstructionGraphNode getGraphNode(AbstractInsnNode insn) {
        return method.getGraphNodes().get(method.instructions.indexOf(insn));
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
