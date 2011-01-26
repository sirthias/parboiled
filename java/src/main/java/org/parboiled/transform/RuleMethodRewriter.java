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
import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
import org.parboiled.matchers.DelegatingActionMatcher;
import org.parboiled.transform.InstructionGroup.GroupType;
import org.parboiled.transform.InstructionGroup.VarInitGroup;
import org.parboiled.transform.support.InsnListGenerator;

/**
 * Inserts action group class instantiation code at the groups respective placeholders.
 */
class RuleMethodRewriter implements RuleMethodProcessor, Types {

    private RuleMethod method;
    private InstructionGroup group;
    private int actionNr;
    private int varInitNr;

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.containsExplicitActions() || method.containsVarInitializers();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        this.method = checkArgNotNull(method, "method");
        actionNr = 0;
        varInitNr = 0;

        for (InstructionGroup group : method.getGroups()) {
            this.group = group;
            
            if (group.getGroupType() == GroupType.VAR_INIT) {
            	insert(new TypeInsnNode(NEW, Type.getInternalName(DelegatingActionMatcher.class)));
            	insert(new InsnNode(DUP));
            }
            
            createNewGroupClassInstance();
            initializeFields();

            switch (group.getGroupType()) {
            case ACTION :
            	 removeGroupRootInstruction();
            	 break;
            case VAR_INIT :
            	initializeVarInitMatcher();
            	break;
            }
        }

        createSetMaxLocalsMatcher();
        
        method.setBodyRewritten();
    }
    
	private void initializeVarInitMatcher() {
		VarInitGroup varInitGroup = (VarInitGroup) group;
		AbstractInsnNode ruleCallInsn = varInitGroup.getRuleCreationCallNode().getInstruction();

		InsnListGenerator gen = new InsnListGenerator();

		// stack: <DelegatingActionMatcher> <Action> <Object>
		gen.checkCast(RULE);
		// stack: <DelegatingActionMatcher> <Action> <Rule>
		gen.swap();
		// stack: <DelegatingActionMatcher> <Rule> <Action>
		gen.invokeConstructor(Type.getType(DelegatingActionMatcher.class),
				new Method("<init>", Type.VOID_TYPE, new Type[] { RULE, ACTION }));

		method.instructions.insert(ruleCallInsn, gen.instructions);
	}

	private void createSetMaxLocalsMatcher() {
		int actionParams = method.getActionParams().cardinality();
		int maxLocals = method.getActionVariableTypes().size() - actionParams - 1;
		if (maxLocals > 0) {
			AbstractInsnNode returnInsn = method.getReturnInstructionNode().getInstruction();
			InsnListGenerator gen = new InsnListGenerator();
			gen.newInstance(SET_MAX_LOCALS_MATCHER);
			gen.dupX1();
			gen.swap();
			gen.push(maxLocals);
			gen.invokeConstructor(SET_MAX_LOCALS_MATCHER, new Method("<init>", Type.VOID_TYPE, new Type[] { RULE, Type.INT_TYPE }));
			method.instructions.insertBefore(returnInsn, gen.instructions);
		}
	}

	private void createNewGroupClassInstance() {
		String internalName = group.getGroupClassType().getInternalName();
		InstructionGraphNode root = group.getRoot();
		insert(new TypeInsnNode(NEW, internalName));
		insert(new InsnNode(DUP));
		insert(new LdcInsnNode(method.name + (root.isActionRoot() ? "_Action" + ++actionNr : "_VarInit" + ++varInitNr)));
		insert(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;)V"));

		if (group.getGroupType() == GroupType.ACTION && method.hasSkipActionsInPredicatesAnnotation()) {
			insert(new InsnNode(DUP));
			insert(new MethodInsnNode(INVOKEVIRTUAL, internalName, "setSkipInPredicates", "()V"));
		}
	}

    private void initializeFields() {
        String internalName = group.getGroupClassType().getInternalName();
        for (FieldNode field : group.getFields()) {
            insert(new InsnNode(DUP));
            // the FieldNodes access and value members have been reused for the var index / Type respectively!
            insert(new VarInsnNode(AsmUtils.getLoadingOpcode((Type) field.value), field.access));
            insert(new FieldInsnNode(PUTFIELD, internalName, field.name, field.desc));
        }
    }

    private void insert(AbstractInsnNode insn) {
        method.instructions.insertBefore(group.getRoot().getInstruction(), insn);
    }
    
    private void removeGroupRootInstruction() {
        method.instructions.remove(group.getRoot().getInstruction());
    }

}

