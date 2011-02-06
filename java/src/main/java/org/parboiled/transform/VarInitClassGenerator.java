/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.InsnList;
import org.parboiled.transform.InstructionGroup.GroupType;
import org.parboiled.transform.InstructionGroup.VarInitGroup;
import org.parboiled.transform.support.InsnListGenerator;

import java.util.List;

class VarInitClassGenerator extends GroupClassGenerator {

	public VarInitClassGenerator(boolean forceCodeBuilding) {
		super(forceCodeBuilding);
	}

	public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
		return method.containsVarInitializers();
	}

	@Override
	protected boolean appliesTo(InstructionGroup group) {
		return group.getGroupType() == GroupType.VAR_INIT;
	}

	@Override
	protected Type getBaseType() {
		return BASE_ACTION;
	}

	@Override
	protected void generateMethod(InstructionGroup group, ClassWriter cw) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", '(' + CONTEXT_DESC + ")Z", null, null);

		// store context to 2nd local variable
		// this context is later used for setVariable and getVariable calls
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ASTORE, 2);
		
		insertSetContextCalls(group);
		
		createAndSetArgumentArray((VarInitGroup)group);
		
		convertXLoadsAndXStores(group);

		group.getInstructions().accept(mv);

		mv.visitInsn(IRETURN);
		mv.visitMaxs(0, 0); // trigger automatic computing
	}

	private void createAndSetArgumentArray(VarInitGroup group) {
		InsnList instructions = group.getInstructions();
		List<Type> targetArgumentTypes = group.getRuleActionArgumentTypes();

		InsnListGenerator gen = new InsnListGenerator(ACC_PUBLIC, "run", '(' + CONTEXT_DESC + ")Z");
		// load variable context
		gen.loadLocal(2, CONTEXT);
		// load variable stack
		gen.invokeInterface(CONTEXT, new Method("getCallStack", CALL_STACK, new Type[0]));
		
		// create array for original arguments and local variables
		gen.push(targetArgumentTypes.size());
		gen.newArray(OBJECT);

		// prepare for first argument
		// duplicate array reference
		gen.dup();
		// load argument index onto stack
		gen.push(0);

		instructions.insertBefore(instructions.getFirst(), gen.instructions);

		List<InstructionGraphNode> argNodes = group.getRuleActionArgumentNodes();
		for (int arg = 0; arg < argNodes.size(); ) {
			InstructionGraphNode argNode = argNodes.get(arg);

			// store argument in array
			gen.box(argNode.getResultValue().getType());
			gen.arrayStore(OBJECT);

			// prepare for next argument
			arg++;
			if (arg < argNodes.size()) {
				// duplicate array reference
				gen.dup();
				// load argument index onto stack
				gen.push(arg);
			}

			instructions.insert(argNode.getInstruction(), gen.instructions);
		}

		// store arguments as new variables
		gen.invokeInterface(CALL_STACK, new Method("setArguments", Type.VOID_TYPE, new Type[] { Type.getType(Object[].class) }));
		
		// load result value
		gen.push(true);
		gen.box(BOOLEAN_TYPE);
		
		instructions.add(gen.instructions);
	}
}