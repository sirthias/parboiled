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
import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.parboiled.transform.InstructionGroup.GroupType;

class ActionClassGenerator extends GroupClassGenerator {

    public ActionClassGenerator(boolean forceCodeBuilding) {
        super(forceCodeBuilding);
    }

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(method, "method");
        return method.containsExplicitActions();
    }

    @Override
    protected boolean appliesTo(InstructionGroup group) {
        return group.getGroupType() == GroupType.ACTION;
    }

    @Override
    protected Type getBaseType() {
        return Types.BASE_ACTION;
    }

    @Override
    protected void generateMethod(InstructionGroup group, ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", '(' + Types.CONTEXT_DESC + ")Z", null, null);

		// store context to 2nd local variable
		// this context is later used for setVariable and getVariable calls
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ASTORE, 2);
        
//        fixContextSwitches(group);
        insertSetContextCalls(group);
        convertXLoadsAndXStores(group);

        group.getInstructions().accept(mv);

        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
    }

}