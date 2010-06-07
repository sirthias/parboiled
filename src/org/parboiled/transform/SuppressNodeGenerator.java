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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Adds a suppressNode() / suppressSubnodes() call before the return instruction.
 */
class SuppressNodeGenerator implements RuleMethodProcessor, Opcodes, Types {

    public boolean appliesTo(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        return method.hasSuppressNodeAnnotation() || method.hasSuppressSubnodesAnnotation();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        Preconditions.checkState(!method.isSuperMethod()); // super methods have flag moved to the overriding method
        
        InsnList instructions = method.instructions;

        AbstractInsnNode ret = instructions.getLast();
        while (ret.getOpcode() != ARETURN) {
            ret = ret.getPrevious();
        }

        // stack: <rule>
        instructions.insertBefore(ret, new InsnNode(DUP));
        // stack: <rule> :: <rule>
        LabelNode isNullLabel = new LabelNode();
        instructions.insertBefore(ret, new JumpInsnNode(IFNULL, isNullLabel));
        // stack: <rule>
        instructions.insertBefore(ret, new MethodInsnNode(INVOKEINTERFACE, RULE.getInternalName(),
                method.hasSuppressNodeAnnotation() ? "suppressNode" : "suppressSubnodes", "()" + RULE.getDescriptor()));
        // stack: <rule>
        instructions.insertBefore(ret, isNullLabel);
        // stack: <rule>
    }

}