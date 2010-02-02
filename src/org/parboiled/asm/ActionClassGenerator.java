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

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import static org.parboiled.asm.AsmUtils.findLoadedClass;
import static org.parboiled.asm.AsmUtils.loadClass;
import org.parboiled.support.Checks;

class ActionClassGenerator implements Opcodes {

    public final ParserClassNode classNode;
    public final ParserMethod method;
    public final InstructionSubSet subSet;
    public final String actionSimpleName;
    public final Type actionType;
    public final InsnList runMethodInstructions = new InsnList();

    private byte[] actionClassCode;

    public ActionClassGenerator(ParserClassNode classNode, ParserMethod method, InstructionSubSet subSet,
                                int actionNumber) {
        this.classNode = classNode;
        this.method = method;
        this.subSet = subSet;
        this.actionSimpleName = method.name + "_Action" + actionNumber;
        this.actionType = Type.getObjectType(classNode.name + "$" + actionSimpleName);
    }

    public synchronized Class<?> defineActionClass() {
        String actionClassName = actionType.getClassName();
        ClassLoader classLoader = classNode.parentClass.getClassLoader();

        moveActionExpressionsInstructions();

        Class<?> actionClass = findLoadedClass(actionClassName, classLoader);
        if (actionClass == null) {
            generateActionClassCode();
            actionClass = loadClass(actionType.getClassName(), actionClassCode, classNode.parentClass.getClassLoader());
        }
        return actionClass;
    }

    public byte[] generateActionClassCode() {
        if (actionClassCode != null) return actionClassCode;

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        generateClassBasics(classWriter);
        generateConstructor(classWriter);
        generateRunMethod(classWriter);
        classWriter.visitEnd();
        actionClassCode = classWriter.toByteArray();
        return actionClassCode;
    }

    private void moveActionExpressionsInstructions() {
        InsnList ruleMethodInstructions = method.instructions;
        for (int i = subSet.firstIndex; i <= subSet.lastIndex; i++) {
            AbstractInsnNode insn = method.getInstructionGraphNodes()[i].instruction;
            ruleMethodInstructions.remove(insn);
            runMethodInstructions.add(insn);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void generateClassBasics(ClassWriter cw) {
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, actionType.getInternalName(), null,
                AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName(), null);
        cw.visitSource(classNode.sourceFile, null);
        cw.visitInnerClass(actionType.getInternalName(), classNode.name, actionSimpleName, ACC_PRIVATE);
        classNode.innerClasses.add(new InnerClassNode(actionType.getInternalName(), classNode.name,
                actionSimpleName, ACC_PRIVATE));
        cw.visitField(ACC_FINAL + ACC_SYNTHETIC, "this$0", classNode.getDescriptor(), null, null).visitEnd();
    }

    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(" + classNode.getDescriptor() + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, actionType.getInternalName(), "this$0", classNode.getDescriptor());
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName(), "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
        mv.visitEnd();
    }

    private void generateRunMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "run",
                Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {AsmUtils.CONTEXT_TYPE}), null, null);
        mv.visitCode();

        Label l0 = new Label();
        mv.visitLabel(l0);

        generateRunMethodBody(mv);

        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", actionType.getDescriptor(), null, l0, l1, 0);
        mv.visitMaxs(0, 0); // trigger automatic computing
        mv.visitEnd();
    }

    @SuppressWarnings({"UnnecessaryContinue"})
    private void generateRunMethodBody(MethodVisitor mv) {
        // store Context parameter in protected field
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName(), "context",
                AsmUtils.CONTEXT_TYPE.getDescriptor());

        // work backwards through the old instructions list and apply adaptations to the new list
        for (int i = subSet.lastIndex; i >= subSet.firstIndex; i--) {
            InstructionGraphNode node = method.getInstructionGraphNodes()[i];
            AbstractInsnNode insn = node.instruction;

            if (insertContextSwitchCode(runMethodInstructions, node)) continue;
            if (insertSetContextCallBeforeCallsOnContextAware(runMethodInstructions, node)) continue;
            if (changeThisToInnerClassParent(runMethodInstructions, insn)) continue;
        }

        // make sure the method result is a "boolean" and not a "Boolean"
        if (isBooleanValueOf(runMethodInstructions.getLast())) {
            // if we are just converting a "boolean" into a "Boolean" at the end remove the conversion
            runMethodInstructions.remove(runMethodInstructions.getLast());
        } else {
            // convert the "Boolean" into the primitive
            runMethodInstructions.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z"));
        }

        // write new instructions
        runMethodInstructions.accept(mv);

        mv.visitInsn(IRETURN);
    }

    private boolean isBooleanValueOf(AbstractInsnNode insn) {
        if (insn.getOpcode() != INVOKESTATIC) return false;
        MethodInsnNode mi = (MethodInsnNode) insn;
        return "java/lang/Boolean".equals(mi.owner) && "valueOf".equals(mi.name) &&
                "(Z)Ljava/lang/Boolean;".equals(mi.desc);
    }

    private boolean insertContextSwitchCode(InsnList newInstructions, InstructionGraphNode node) {
        if (!node.isContextSwitch) return false;

        String contextSwitchType = ((MethodInsnNode) node.instruction).name;

        // insert context-switching call (UP/DOWN) before first instruction contributing to the argument
        AbstractInsnNode targetSettingInsn = node.getEarlierstPredecessor().instruction;
        Checks.ensure(targetSettingInsn.getOpcode() == ALOAD && ((VarInsnNode) targetSettingInsn).var == 0,
                "Illegal context switching construct in parser rule method '" + method.name + "': " +
                        "UP(...) or DOWN(...) can only be called on the parser instance itself");

        newInstructions.insert(targetSettingInsn, new MethodInsnNode(INVOKEVIRTUAL,
                AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName(), contextSwitchType, "()V"));

        // replace original context-switching call with the opposite one, reversing the context switch done before
        newInstructions.insertBefore(node.instruction, new VarInsnNode(ALOAD, 0));
        newInstructions.insertBefore(node.instruction,
                new MethodInsnNode(INVOKEVIRTUAL, AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName(),
                        "UP".equals(contextSwitchType) ? "DOWN" : "UP", "()V")
        );
        newInstructions.remove(node.instruction);

        return true;
    }

    private boolean insertSetContextCallBeforeCallsOnContextAware(InsnList newInstructions, InstructionGraphNode node) {
        if (!node.isCallOnContextAware) return false;

        AbstractInsnNode firstAfterTargetSettingInsn = node.getEarlierstPredecessor().instruction.getNext();
        newInstructions.insertBefore(firstAfterTargetSettingInsn, new InsnNode(DUP));
        newInstructions.insertBefore(firstAfterTargetSettingInsn, new VarInsnNode(ALOAD, 0));
        newInstructions.insertBefore(firstAfterTargetSettingInsn,
                new FieldInsnNode(GETFIELD, actionType.getInternalName(), "context",
                        AsmUtils.CONTEXT_TYPE.getDescriptor()));
        newInstructions.insertBefore(firstAfterTargetSettingInsn,
                new MethodInsnNode(INVOKEINTERFACE, AsmUtils.CONTEXT_AWARE_TYPE.getInternalName(),
                        "setContext", "(" + AsmUtils.CONTEXT_TYPE.getDescriptor() + ")V"));

        return true;
    }

    private boolean changeThisToInnerClassParent(InsnList newInstructions, AbstractInsnNode insn) {
        if (insn.getOpcode() != ALOAD || ((VarInsnNode) insn).var != 0) return false;
        if (insn.getNext() instanceof MethodInsnNode &&
                ((MethodInsnNode) insn.getNext()).owner.equals(AsmUtils.ACTION_WRAPPER_BASE_TYPE.getInternalName())) {
            // do not change the "ALOAD 0" we left in place for a following context switch call
            return false;
        }

        newInstructions.insert(insn,
                new FieldInsnNode(GETFIELD, actionType.getInternalName(), "this$0", classNode.getDescriptor())
        );
        return true;
    }

}
