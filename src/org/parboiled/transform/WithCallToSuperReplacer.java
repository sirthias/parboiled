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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import static org.parboiled.transform.AsmUtils.createArgumentLoaders;
import static org.parboiled.common.Utils.merge;

import java.util.Set;

/**
 * Replaces the method code of so all methods except the action containing rule methods with simple calls to super.
 */
class WithCallToSuperReplacer implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public WithCallToSuperReplacer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    @SuppressWarnings("unchecked")
    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        Set<ParserMethod> methods = merge(classNode.cachedMethods, classNode.labelMethods, classNode.leafMethods);
        for (ParserMethod method : methods) {
            replaceWithCallToSuper(classNode, method);
        }
        for (ParserMethod method : classNode.ruleMethods) {
            if (!method.hasActions()) {
                replaceWithCallToSuper(classNode, method);
            }
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void replaceWithCallToSuper(ParserClassNode classNode, ParserMethod method) {
        // replace all method code with a simple call to the super method
        method.instructions.clear();
        method.instructions.add(new VarInsnNode(ALOAD, 0));
        method.instructions.add(createArgumentLoaders(method.desc));
        method.instructions.add(new MethodInsnNode(INVOKESPECIAL,
                classNode.getParentType().getInternalName(), method.name, method.desc));
        method.instructions.add(new InsnNode(ARETURN));
        
        method.localVariables.clear();
    }

}