package org.parboiled.transform;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
		List<Type> variableTypes = new ArrayList<Type>();

		SimpleVerifier typeMerger = new SimpleVerifier();
        for (InstructionGroup group : method.getGroups()) {
            for (InstructionGraphNode node : group.getNodes()) {
                AbstractInsnNode insn = node.getInstruction();

                if (!(node.isXStore() || node.isXLoad() && method.getActionParams().get(((VarInsnNode) insn).var))) {
                    continue;
                }

                int var = ((VarInsnNode) insn).var;
                if (var < variableTypes.size()) {
                    Type lastType = variableTypes.get(var);
                    variableTypes.set(
                            var,
                            lastType == null ? node.getResultValue().getType() : ((BasicValue) typeMerger.merge(new BasicValue(lastType),
                                    node.getResultValue())).getType());
                } else {
                    for (int j = variableTypes.size(); j < var; j++) {
                        variableTypes.add(j, null);
                    }
                    variableTypes.add(var, node.getResultValue().getType());
                }
            }
        }
        
        Type[] argTypes = Type.getArgumentTypes(method.desc);
        for (int i = 0; i < argTypes.length; i++) {
            int var = i + 1;
            if (method.getActionParams().get(var)) {
                for (int j = variableTypes.size(); j <= var; j++) {
                    variableTypes.add(j, null);
                }
                variableTypes.set(var, argTypes[i]);
            }
        }

		method.setActionVariableTypes(variableTypes);
	}
}
