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

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.parboiled.transform.AsmUtils.isBooleanValueOfZ;

/**
 * Makes all implicit action expressions in a rule method explicit.
 */
class ImplicitActionsConverter implements RuleMethodProcessor, Types, Opcodes {

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsImplicitActions();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        AbstractInsnNode current = method.instructions.getFirst();
        while (current != null) {
            AbstractInsnNode next = current.getNext();
            if (isBooleanValueOfZ(current)) {
                method.instructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
                method.instructions.insertBefore(current, new InsnNode(SWAP));
                method.instructions.set(current, new MethodInsnNode(INVOKEVIRTUAL,
                        BASE_PARSER.getInternalName(), "ACTION", "(Z)" + ACTION_DESC));
            }
            current = next;
        }

        method.setContainsImplicitActions(false);
        method.setContainsActions(true);
    }

}