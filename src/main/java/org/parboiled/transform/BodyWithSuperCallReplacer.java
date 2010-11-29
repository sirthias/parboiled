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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import static org.objectweb.asm.Opcodes.*;
import static org.parboiled.transform.AsmUtils.createArgumentLoaders;

/**
 * Replaces the method code with a simple call to the super method.
 */
class BodyWithSuperCallReplacer implements RuleMethodProcessor {

    public boolean appliesTo(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        return !method.isBodyRewritten() && method.getOwnerClass() == classNode.getParentClass() &&
                method.getLocalVarVariables() == null;  // if we have local variables we need to create a VarFramingMatcher
                                                        // which needs access to the local variables
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        // replace all method code with a simple call to the super method
        method.instructions.clear();
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(createArgumentLoaders(method.desc));
        method.instructions.add(new MethodInsnNode(INVOKESPECIAL,
                classNode.getParentType().getInternalName(), method.name, method.desc));
        method.instructions.add(new InsnNode(ARETURN));
    }

}