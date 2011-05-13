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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import static org.objectweb.asm.Opcodes.*;

/**
 * Replaces the method code with a simple call to the super method.
 */
class SuperCallRewriter implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.containsPotentialSuperCalls();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
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
            checkState(clazz != null); // we should find the owner before hitting Object
            superMethodName = '$' + superMethodName;
        } while (!Type.getInternalName(clazz).equals(insn.owner));
        return superMethodName;
    }

}