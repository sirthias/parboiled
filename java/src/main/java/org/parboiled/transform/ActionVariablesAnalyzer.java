package org.parboiled.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Determines the types of all variables which are accessed by actions contained
 * in a rule.
 * 
 * @author Ken Wenzel
 * 
 */
public class ActionVariablesAnalyzer implements RuleMethodProcessor, Opcodes, Types {
	public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
		return method.containsExplicitActions() || method.containsVarInitializers();
	}

	public void process(ParserClassNode classNode, RuleMethod method) {
		determineActionVariableTypes(method);
	}

	private void determineActionVariableTypes(RuleMethod method) {
		List<BasicValue> variableTypes = new ArrayList<BasicValue>();

		SimpleVerifier typeMerger = new SimpleVerifier();
		for (InstructionGroup group : method.getGroups()) {
			for (InstructionGraphNode node : group.getNodes()) {
				AbstractInsnNode insn = node.getInstruction();
				
				if (!(node.isXStore() || node.isXLoad() && method.getActionParams().get(((VarInsnNode) insn).var))) {
					continue;
				}
				
				int var = ((VarInsnNode) insn).var;
				if (var < variableTypes.size()) {
					BasicValue lastType = variableTypes.get(var);
					variableTypes.set(var,
							lastType == null ? node.getResultValue() : (BasicValue) typeMerger.merge(lastType, node.getResultValue()));
				} else {
					for (int j = variableTypes.size(); j < var; j++) {
						variableTypes.add(j, null);
					}
					variableTypes.add(var, node.getResultValue());
				}
			}
		}

		method.setActionVariableTypes(variableTypes);
	}
}
