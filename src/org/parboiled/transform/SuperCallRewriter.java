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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Replaces the method code with a simple call to the super method.
 */
class SuperCallRewriter implements RuleMethodProcessor, Opcodes {

    public boolean appliesTo(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        return method.containsPotentialSuperCalls();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        InsnList instructions = method.instructions;
        AbstractInsnNode insn = instructions.getFirst();
        while (insn.getOpcode() != ARETURN) {
            if (insn.getOpcode() == INVOKESPECIAL) {
                process(classNode, method, (MethodInsnNode) insn);
            }
            insn = insn.getNext();
        }
    }

    private void process(ParserClassNode classNode, RuleMethod method, MethodInsnNode insn) {
        if ("<init>".equals(insn.name)) return;
        String superMethodName = getSuperMethodName(method, insn);
        RuleMethod superMethod = classNode.getRuleMethods().get(superMethodName.concat(insn.desc));
        if (superMethod == null) return;
        if (!superMethod.isBodyRewritten()) return;

        // since the super method is rewritten we do need to generate it
        superMethod.dontSkipGeneration();

        // we have a call to a super method that was rewritten, so we need to change the call to the generated method
        insn.setOpcode(INVOKEVIRTUAL);
        insn.name = superMethodName;
        insn.owner = classNode.name;

        method.setBodyRewritten();
    }

    @SuppressWarnings({"ConstantConditions"})
    private String getSuperMethodName(RuleMethod method, MethodInsnNode insn) {
        Class<?> clazz = method.getOwnerClass();
        String superMethodName = method.name;
        do {
            clazz = clazz.getSuperclass();
            Preconditions.checkState(clazz != null); // we should find the owner before hitting Object
            superMethodName = '$' + superMethodName;
        } while (!Type.getInternalName(clazz).equals(insn.owner));
        return superMethodName;
    }

}