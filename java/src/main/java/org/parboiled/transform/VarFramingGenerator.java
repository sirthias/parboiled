/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import static org.parboiled.common.Preconditions.*;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;
import static org.parboiled.transform.Types.*;

/**
 * Inserts code for wrapping the created rule into a VarFramingMatcher if the method contains local variables
 * assignable to {@link org.parboiled.support.Var}.
 */
class VarFramingGenerator implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.getLocalVarVariables() != null;
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        InsnList instructions = method.instructions;
        
        AbstractInsnNode ret = instructions.getLast();
        while (ret.getOpcode() != ARETURN) {
            ret = ret.getPrevious();
        }

        // stack: <Matcher>
        instructions.insertBefore(ret, new TypeInsnNode(NEW, VAR_FRAMING_MATCHER.getInternalName()));
        // stack: <Matcher> :: <VarFramingMatcher>
        instructions.insertBefore(ret, new InsnNode(DUP_X1));
        // stack: <VarFramingMatcher> :: <Matcher> :: <VarFramingMatcher>
        instructions.insertBefore(ret, new InsnNode(SWAP));
        // stack: <VarFramingMatcher> :: <VarFramingMatcher> :: <Matcher>
        createVarFieldArray(method, instructions, ret);
        // stack: <VarFramingMatcher> :: <VarFramingMatcher> :: <Matcher> :: <VarFieldArray>
        instructions.insertBefore(ret, new MethodInsnNode(INVOKESPECIAL, VAR_FRAMING_MATCHER.getInternalName(),
                "<init>", '(' + RULE_DESC + '[' + VAR_DESC + ")V"));
        // stack: <VarFramingMatcher>

        method.setBodyRewritten();
    }

    private void createVarFieldArray(RuleMethod method, InsnList instructions, AbstractInsnNode ret) {
        int count = method.getLocalVarVariables().size();

        // stack:
        instructions.insertBefore(ret, new IntInsnNode(BIPUSH, count));
        // stack: <length>
        instructions.insertBefore(ret, new TypeInsnNode(ANEWARRAY, VAR.getInternalName()));
        // stack: <array>
        for (int i = 0; i < count; i++) {
            LocalVariableNode var = method.getLocalVarVariables().get(i);
            // stack: <array>
            instructions.insertBefore(ret, new InsnNode(DUP));
            // stack: <array> :: <array>
            instructions.insertBefore(ret, new IntInsnNode(BIPUSH, i));
            // stack: <array> :: <array> :: <index>
            instructions.insertBefore(ret, new VarInsnNode(ALOAD, var.index));
            // stack: <array> :: <array> :: <index> :: <var>
            instructions.insertBefore(ret, new InsnNode(DUP));
            // stack: <array> :: <array> :: <index> :: <var> :: <var>
            instructions.insertBefore(ret, new LdcInsnNode(method.name + ':' + var.name));
            // stack: <array> :: <array> :: <index> :: <var> :: <var> :: <varName>
            instructions.insertBefore(ret, new MethodInsnNode(INVOKEVIRTUAL, VAR.getInternalName(), "setName",
                    "(Ljava/lang/String;)V"));
            // stack: <array> :: <array> :: <index> :: <var>
            instructions.insertBefore(ret, new InsnNode(AASTORE));
            // stack: <array>
        }
        // stack: <array>
    }

}