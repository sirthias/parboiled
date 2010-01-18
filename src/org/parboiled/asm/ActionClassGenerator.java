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

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class ActionClassGenerator extends ClassLoader implements Opcodes, Types {

    private final ParserClassNode classNode;
    private final RuleMethodInfo methodInfo;
    private final InstructionSubSet subSet;
    private final String actionSimpleName;
    private final Type actionType;
    private final String classNodeTypeDesc;

    public ActionClassGenerator(ParserClassNode classNode, RuleMethodInfo methodInfo, InstructionSubSet subSet,
                                int actionNumber) {
        this.classNode = classNode;
        this.methodInfo = methodInfo;
        this.subSet = subSet;
        this.actionSimpleName = methodInfo.method.name + "_Action" + actionNumber;
        this.actionType = Type.getObjectType(classNode.name + "$" + actionSimpleName);
        this.classNodeTypeDesc = classNode.getType().getDescriptor();
    }

    public Type defineActionClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        generateClassBasics(cw);
        generateConstructor(cw);
        generateRunMethod(cw);
        cw.visitEnd();

        byte[] code = cw.toByteArray();
        Class<?> actionClass = defineClass(null, code, 0, code.length);

        return actionType;
    }

    @SuppressWarnings({"unchecked"})
    private void generateClassBasics(ClassWriter cw) {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, actionType.getInternalName(), null,
                ACTION_WRAPPER_BASE_TYPE.getInternalName(), null);
        cw.visitSource(classNode.sourceFile, null);
        cw.visitInnerClass(actionType.getInternalName(), classNode.name, actionSimpleName, ACC_PRIVATE);
        classNode.innerClasses.add(new InnerClassNode(actionType.getInternalName(), classNode.name,
                actionSimpleName, ACC_PRIVATE));
        cw.visitField(ACC_FINAL + ACC_SYNTHETIC, "this$0", classNodeTypeDesc, null, null).visitEnd();
    }

    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + classNodeTypeDesc + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, actionType.getInternalName(), "this$0", classNodeTypeDesc);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, ACTION_WRAPPER_BASE_TYPE.getInternalName(), "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
        mv.visitEnd();
    }

    private void generateRunMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run", ACTION_RESULT_TYPE.getInternalName(), null, null);
        mv.visitCode();

        Label l0 = new Label();
        mv.visitLabel(l0);

        generateRunMethodBody(mv);

        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", actionType.getDescriptor(), null, l0, l1, 0);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    private void generateRunMethodBody(MethodVisitor mv) {
        InsnList newInstructions = new InsnList();

        // initialize with the old instruction list
        for (int i = subSet.firstIndex; i <= subSet.lastIndex; i++) {
            newInstructions.add(methodInfo.instructionGraphNodes[i].instruction);
        }

        // work backwards through the old instructions list and apply adaptations to the new list
        for (int i = subSet.lastIndex; i >= subSet.firstIndex; i--) {
            InstructionGraphNode node = methodInfo.instructionGraphNodes[i];
            AbstractInsnNode insn = node.instruction;

            if (changeThisToInnerClassParent(newInstructions, insn)) continue;
            if (insertMagicUpDownCode(newInstructions, node)) continue;
            if (insertMagicSkipInPredicatesCode(newInstructions, node)) continue;
            insertSetContextCallBeforeCallsOnContextAware(newInstructions, node);
        }

        // write new instructions
        newInstructions.accept(mv);

        mv.visitInsn(ARETURN);
    }

    private boolean changeThisToInnerClassParent(InsnList newInstructions, AbstractInsnNode insn) {
        if (insn.getOpcode() != ALOAD || ((VarInsnNode) insn).var != 0) return false;
        newInstructions.insertBefore(insn.getNext(),
                new FieldInsnNode(GETFIELD, actionType.getInternalName(), "this$0", classNodeTypeDesc)
        );
        return true;
    }

    private boolean insertMagicUpDownCode(InsnList newInstructions, InstructionGraphNode node) {
        if (!node.isMagicUp || !node.isMagicDown) return false;

        return true;
    }

    private boolean insertMagicSkipInPredicatesCode(InsnList newInstructions, InstructionGraphNode node) {
        if (node.isMagicSkipInPredicates) return false;

        return true;
    }

    private boolean insertSetContextCallBeforeCallsOnContextAware(InsnList newInstructions, InstructionGraphNode node) {
        if (!node.isCallOnContextAware) return false;

        AbstractInsnNode earliestPredecessor = node.getEarlierstPredecessor().instruction;
        InsnList inserts = new InsnList();
        inserts.add(new InsnNode(DUP));
        inserts.add(new VarInsnNode(ALOAD, 0));
        inserts.add(new FieldInsnNode(GETFIELD, actionType.getInternalName(), "context",
                CONTEXT_TYPE.getDescriptor()));
        inserts.add(new MethodInsnNode(INVOKEINTERFACE, CONTEXT_AWARE_TYPE.getInternalName(),
                "setContext", "(" + CONTEXT_TYPE.getDescriptor() + ")V"));
        newInstructions.insert(earliestPredecessor, inserts);
        return true;
    }

}
