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

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Removes all LineNumer "instructions" from the methods that are going to be affected by the rewriting.
 */
class LineNumberRemover implements ClassTransformer {

    private final ClassTransformer nextTransformer;

    public LineNumberRemover(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    @SuppressWarnings("unchecked")
    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (Object method : classNode.methods) {
            removeLineNumbers((ParserMethod) method);
        }
        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void removeLineNumbers(ParserMethod method) {
        AbstractInsnNode current = method.instructions.getFirst();
        while (current != null) {
            AbstractInsnNode next = current.getNext();
            if (current.getType() == AbstractInsnNode.LINE) {
                method.instructions.remove(current);
            }
            current = next;
        }
    }

}