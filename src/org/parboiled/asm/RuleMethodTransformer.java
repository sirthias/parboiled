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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.jetbrains.annotations.NotNull;

class RuleMethodTransformer implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public RuleMethodTransformer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (RuleMethodInfo methodInfo : classNode.methodInfos) {
            if (methodInfo.hasActions()) {
                transformRuleMethodContainingActions(classNode, methodInfo);
            } else {
                transformRuleMethodWithoutActions(classNode, methodInfo);
            }
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void transformRuleMethodContainingActions(ParserClassNode classNode, RuleMethodInfo methodInfo) {
        int actionNr = 1;
        for (InstructionSubSet subSet : methodInfo.getInstructionSubSets()) {
            if (subSet.isActionSet) {
                ActionClassGenerator generator = new ActionClassGenerator(classNode, methodInfo, subSet, actionNr++);
                generator.defineActionClass();
                insertActionClassCreation(classNode, methodInfo, subSet, generator.actionType);
                
                classNode.actionClassGenerators.add(generator);
            }
        }
    }

    private void insertActionClassCreation(ParserClassNode classNode, RuleMethodInfo methodInfo,
                                           InstructionSubSet subSet, Type actionType) {
        InsnList methodInstructions = methodInfo.method.instructions;
        AbstractInsnNode firstAfterAction = methodInfo.instructionGraphNodes[subSet.lastIndex + 1].instruction;

        // we do not have to remove the action instructions from the rule method as this has already happened
        // during action class creation, all we have to do is insert the action class creation instructions
        methodInstructions.insertBefore(firstAfterAction, new TypeInsnNode(NEW, actionType.getInternalName()));
        methodInstructions.insertBefore(firstAfterAction, new InsnNode(DUP));
        methodInstructions.insertBefore(firstAfterAction, new VarInsnNode(ALOAD, 0));
        methodInstructions.insertBefore(firstAfterAction,
                new MethodInsnNode(INVOKESPECIAL, actionType.getInternalName(), "<init>",
                        "(" + classNode.getDescriptor() + ")V"));
    }

    private void transformRuleMethodWithoutActions(ParserClassNode classNode, RuleMethodInfo methodInfo) {
        // replace all method code with a simple call to the super method
        // we do not just delete the method because its code is later going to wrapped with the caching code
        InsnList methodInstructions = methodInfo.method.instructions;
        AbstractInsnNode returnInsn = methodInfo.getReturnNode().instruction;

        // do not delete starting label and linenumber instructions
        AbstractInsnNode current = methodInstructions.getFirst();
        while (current.getType() == AbstractInsnNode.LABEL || current.getType() == AbstractInsnNode.LINE) {
            current = current.getNext();
        }

        // delete all instructions before the return statement
        while (current != returnInsn) {
            AbstractInsnNode next = current.getNext();
            methodInstructions.remove(current);
            current = next;
        }

        // insert the call to the super method
        methodInstructions.insertBefore(returnInsn, new VarInsnNode(ALOAD, 0));
        methodInstructions.insertBefore(returnInsn, new MethodInsnNode(INVOKESPECIAL, classNode.getParentType()
                .getInternalName(), methodInfo.method.name, "()" + AsmUtils.RULE_TYPE.getDescriptor()));
    }

}

