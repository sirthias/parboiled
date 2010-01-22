/*
 * Copyright (C) 2009 Mathias Doenitz
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.jetbrains.annotations.NotNull;

public class RuleMethodTransformer implements ClassTransformer, Opcodes {

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

