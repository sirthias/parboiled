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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

/**
 * Transforms the ParserClassNode.cachedMethods:
 * If a method contains more than one return instruction, all "non-last" return instructions are replaced with goto
 * instructions to the last return instruction. Afterwards all cachedMethods will contain exactly one return instruction.
 */
class ReturnInstructionUnifier implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public ReturnInstructionUnifier(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (ParserMethod method : classNode.cachedMethods) {
            unifyReturnInstructions(method);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void unifyReturnInstructions(ParserMethod method) {
        AbstractInsnNode current = method.instructions.getLast();

        // find last return
        while (current.getOpcode() != ARETURN) {
            current = current.getPrevious();
        }

        AbstractInsnNode lastReturn = current;
        LabelNode lastReturnLabel = null;

        // iterate backwards up to first instructions
        while ((current = current.getPrevious()) != null) {
            if (current.getOpcode() == ARETURN) {
                if (lastReturnLabel == null) {
                    lastReturnLabel = new LabelNode();
                    method.instructions.insertBefore(lastReturn, lastReturnLabel);
                }
                JumpInsnNode gotoInstruction = new JumpInsnNode(GOTO, lastReturnLabel);
                method.instructions.set(current, gotoInstruction);
                current = gotoInstruction;
            }
        }
    }

}