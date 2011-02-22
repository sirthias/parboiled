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

import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.*;
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
        
        // map variable indexes by subtracting the number of preceding action arguments
        for (InstructionGraphNode node : method.getGraphNodes()) {
			if (node.getGroup() == null && (node.isXLoad() || node.isXStore())) {
				VarInsnNode varInsn = (VarInsnNode)node.getInstruction();
				varInsn.var = mapParamIndex(varInsn.var);
			}
        }

        for (InstructionGroup group : method.getGroups()) {
            this.group = group;
            
            InsnListGenerator gen = new InsnListGenerator();
            
            createNewGroupClassInstance(gen);
            initializeFields(gen);

            switch (group.getGroupType()) {
            case VAR_INIT : 
                initializeVarInitMatcher((VarInitGroup) group, gen);
                method.instructions.insert(group.getRoot().getInstruction(), gen.instructions);
                break;
            case ACTION :
                method.instructions.insertBefore(group.getRoot().getInstruction(), gen.instructions);
                removeGroupRootInstruction();
                break;
            }
        }

        createExecutionFrameMatcher();
        
        method.setBodyRewritten();
    }
    
    private void initializeVarInitMatcher(VarInitGroup group, InsnListGenerator gen) {
        // execute construction of DelegatingActionMatcher before creation of
        // var init action
        gen.pushInsns();
        // stack: <Object>
        
        if (! group.isRetargetedCall()) {
            // a call to a rule-creation method that is executed with fake
            // parameters, hence the resulting matcher for initializing the
            // arguments has to be removed
            gen.checkCast(DELEGATING_MATCHER);
            gen.invokeVirtual(DELEGATING_MATCHER,  new Method("getDelegate", MATCHER, new Type[0]));
            // stack: <Matcher>
        } else {
            gen.checkCast(RULE);
            // stack: <Rule>
        }

        gen.newInstance(DELEGATING_ACTION_MATCHER);
        // stack: <Rule> <DelegatingActionMatcher>
        gen.dupX1();
        // stack: <DelegatingActionMatcher> <Rule> <DelegatingActionMatcher>
        gen.swap();
        // stack: <DelegatingActionMatcher> <DelegatingActionMatcher> <Rule>

        gen.peekInsns().insert(gen.instructions);
        gen.popInsns();
        // stack: ... <DelegatingActionMatcher> <Rule> <Action>

        // stack: ... <DelegatingActionMatcher> <Rule> <Action>
        gen.invokeConstructor(DELEGATING_ACTION_MATCHER,
                new Method("<init>", Type.VOID_TYPE, new Type[] { RULE, ACTION }));
    }

	private void createExecutionFrameMatcher() {
		int actionParams = method.getActionParams().cardinality();
		int args = Type.getArgumentTypes(method.desc).length;
		
		int maxLocals = 0;
		for (int i = args; i < method.getActionVariableTypes().size(); i++) {
		    if (method.getActionVariableTypes().get(i) != null) {
		        maxLocals++;
		    }
		}
		
		if (actionParams > 0 || maxLocals > 0) {
			AbstractInsnNode returnInsn = method.getReturnInstructionNode().getInstruction();
			InsnListGenerator gen = new InsnListGenerator();
			gen.newInstance(EXECUTION_FRAME_MATCHER);
			gen.dupX1();
			gen.swap();
			gen.push(maxLocals);
			gen.invokeConstructor(EXECUTION_FRAME_MATCHER, new Method("<init>", Type.VOID_TYPE, new Type[] { RULE, Type.INT_TYPE }));
			method.instructions.insertBefore(returnInsn, gen.instructions);
		}
	}

	private void createNewGroupClassInstance(InsnListGenerator gen) {
		InstructionGraphNode root = group.getRoot();
		gen.newInstance(group.getGroupClassType());
		gen.dup();
		gen.push(method.name + (root.isActionRoot() ? "_Action" + ++actionNr : "_VarInit" + ++varInitNr));
		gen.invokeConstructor(group.getGroupClassType(), new Method("<init>", "(Ljava/lang/String;)V"));

		if (group.getGroupType() == GroupType.ACTION && method.hasSkipActionsInPredicatesAnnotation()) {
			gen.dup();
			gen.invokeVirtual(group.getGroupClassType(), new Method("setSkipInPredicates", "()V"));
		}
	}

    private void initializeFields(InsnListGenerator gen) {
        for (FieldNode field : group.getFields()) {
            gen.dup();
            
            // the FieldNodes access and value members have been reused for the var index / Type respectively!
            gen.instructions.add(new VarInsnNode(((Type) field.value).getOpcode(Opcodes.ILOAD), mapParamIndex(field.access)));
            gen.putField(group.getGroupClassType(), field.name, Type.getType(field.desc));
        }
    }

    private void removeGroupRootInstruction() {
        method.instructions.remove(group.getRoot().getInstruction());
    }

    private int mapParamIndex(int param) {
        int maxParam = Math.min(param, method.getActionParams().length());
        for (int i = 0; i < maxParam; i++) {
            if (method.getActionParams().get(i)) {
                param--;
            }
        }
        return param;
    }
}

