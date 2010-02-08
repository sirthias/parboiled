/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Transforms the ParserClassNode.ruleMethods by replacing all action expressions with calls to respective
 * action classes (which are generated using the ActionClassGenerator).
 */
class RuleMethodTransformer implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public RuleMethodTransformer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (ParserMethod method : classNode.ruleMethods) {
            if (method.hasActions()) {
                transformRuleMethodContainingActions(classNode, method);
            }
        }
        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void transformRuleMethodContainingActions(ParserClassNode classNode, ParserMethod method) {
        int actionNr = 1;
        for (InstructionSubSet subSet : method.getInstructionSubSets()) {
            if (subSet.isActionSet) {
                ActionClassGenerator generator = new ActionClassGenerator(classNode, method, subSet, actionNr++);
                generator.defineActionClass();
                insertActionClassCreation(classNode, method, subSet, generator.actionType);

                classNode.actionClassGenerators.add(generator);
            }
        }

        method.localVariables.clear();
    }

    private void insertActionClassCreation(ParserClassNode classNode, ParserMethod method,
                                           InstructionSubSet subSet, Type actionType) {
        InsnList methodInstructions = method.instructions;
        AbstractInsnNode firstAfterAction = method.getInstructionGraphNodes()[subSet.lastIndex + 1].instruction;

        // we do not have to remove the action instructions from the rule method as this has already happened
        // during action class creation, all we have to do is insert the action class creation instructions
        methodInstructions.insertBefore(firstAfterAction, new TypeInsnNode(NEW, actionType.getInternalName()));
        methodInstructions.insertBefore(firstAfterAction, new InsnNode(DUP));
        methodInstructions.insertBefore(firstAfterAction, new VarInsnNode(ALOAD, 0));
        methodInstructions.insertBefore(firstAfterAction,
                new MethodInsnNode(INVOKESPECIAL, actionType.getInternalName(), "<init>",
                        "(" + classNode.getDescriptor() + ")V"));
    }

}

