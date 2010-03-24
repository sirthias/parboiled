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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.parboiled.support.Checks;

import java.util.List;

import static org.parboiled.transform.AsmUtils.findLoadedClass;
import static org.parboiled.transform.AsmUtils.loadClass;

abstract class GroupClassGenerator implements RuleMethodProcessor, Opcodes, Types {

    private static final Object lock = new Object();

    protected ParserClassNode classNode;
    protected RuleMethod method;

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        this.classNode = classNode;
        this.method = method;

        for (InstructionGroup group : method.getGroups()) {
            if (appliesTo(group.getRoot())) {
                loadGroupClass(group);
            }
        }
    }

    protected abstract boolean appliesTo(InstructionGraphNode group);

    private void loadGroupClass(InstructionGroup group) {
        createGroupClassType(group);
        String className = group.getGroupClassType().getClassName();
        ClassLoader classLoader = classNode.parentClass.getClassLoader();

        Class<?> groupClass;
        synchronized (lock) {
            groupClass = findLoadedClass(className, classLoader);
            if (groupClass == null) {
                byte[] groupClassCode = generateGroupClassCode(group);
                groupClass = loadClass(className, groupClassCode, classLoader);
            }
        }
        group.setGroupClass(groupClass);
    }

    private void createGroupClassType(InstructionGroup group) {
        String s = classNode.name;
        String groupClassInternalName = s.substring(0, classNode.name.lastIndexOf('/')) + '/' + group.getName();
        group.setGroupClassType(Type.getObjectType(groupClassInternalName));
    }

    protected byte[] generateGroupClassCode(InstructionGroup group) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        generateClassBasics(group, classWriter);
        generateFields(group, classWriter);
        generateConstructor(classWriter);
        generateMethod(group, classWriter);
        return classWriter.toByteArray();
    }

    private void generateClassBasics(InstructionGroup group, ClassWriter cw) {
        cw.visit(V1_5, ACC_PUBLIC + ACC_FINAL, group.getGroupClassType().getInternalName(), null,
                getBaseType().getInternalName(), null);
        cw.visitSource(classNode.sourceFile, null);
    }

    protected abstract Type getBaseType();

    private void generateFields(InstructionGroup group, ClassWriter cw) {
        FieldNode[] fields = group.getFields();
        for (InstructionGraphNode node : group.getNodes()) {
            if (node.isXLoad()) {
                VarInsnNode insn = (VarInsnNode) node.getInstruction();
                if (fields[insn.var] == null) {
                    FieldNode field = new FieldNode(ACC_PUBLIC + ACC_SYNTHETIC, "field$" + insn.var,
                            node.getResultValue().getType().getDescriptor(), null, null);
                    field.accept(cw);
                    fields[insn.var] = field;
                }
            }
        }
    }

    private void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, getBaseType().getInternalName(), "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // trigger automatic computing
    }

    protected abstract void generateMethod(InstructionGroup group, ClassWriter cw);

    protected void fixContextSwitches(InstructionGroup group) {
        List<InstructionGraphNode> nodes = group.getNodes();
        InsnList instructions = group.getInstructions();
        for (int i = 0, nodesSize = nodes.size(); i < nodesSize; i++) {
            InstructionGraphNode node = nodes.get(i);
            if (!node.isContextSwitch()) continue;

            // insert context switch
            String contextSwitchType = ((MethodInsnNode) node.getInstruction()).name;
            InstructionGraphNode firstNode = getFirstOfSubtree(node);
            AbstractInsnNode firstInsn = firstNode.getInstruction();
            Checks.ensure(firstInsn == node.getPredecessors().get(0).getInstruction() &&
                    firstInsn.getOpcode() == ALOAD, "Error during bytecode analysis of rule method '%s': " +
                    "Unusual context switch call", method.name);
            instructions.insertBefore(firstInsn, new VarInsnNode(ALOAD, 0));
            instructions.insertBefore(firstInsn, new MethodInsnNode(INVOKEVIRTUAL,
                    getBaseType().getInternalName(), contextSwitchType, "()V"));

            // remove the target loading instruction
            instructions.remove(firstInsn);
            nodes.remove(firstNode);
            i--; // correct current index since the removed node always has a lower index

            // replace original context-switching call with the opposite one, reversing the context switch done before
            instructions.insertBefore(node.getInstruction(), new VarInsnNode(ALOAD, 0));
            instructions.set(node.getInstruction(), new MethodInsnNode(INVOKEVIRTUAL,
                    getBaseType().getInternalName(), contextSwitchType.startsWith("UP") ? contextSwitchType
                            .replace("UP", "DOWN") : contextSwitchType.replace("DOWN", "UP"), "()V"));
        }
    }

    protected void insertSetContextCalls(InstructionGroup group) {
        InsnList instructions = group.getInstructions();
        for (InstructionGraphNode node : group.getNodes()) {
            if (!node.isCallOnContextAware()) continue;

            AbstractInsnNode firstInsn = getFirstOfSubtree(node).getInstruction();
            Checks.ensure(firstInsn == node.getPredecessors().get(0).getInstruction() &&
                    firstInsn.getOpcode() == ALOAD, "Error during bytecode analysis of rule method '%s': " +
                    "Unusual call on ContextAware", method.name);
            AbstractInsnNode afterFirstInsn = firstInsn.getNext();
            instructions.insertBefore(afterFirstInsn, new InsnNode(DUP));
            instructions.insertBefore(afterFirstInsn, new VarInsnNode(ALOAD, 0));
            instructions.insertBefore(afterFirstInsn, new FieldInsnNode(GETFIELD,
                    group.getGroupClassType().getInternalName(), "context", CONTEXT_DESC));
            instructions.insertBefore(afterFirstInsn, new MethodInsnNode(INVOKEINTERFACE,
                    CONTEXT_AWARE.getInternalName(), "setContext", "(" + CONTEXT_DESC + ")V"));
        }
    }

    protected void convertXLoads(InstructionGroup group) {
        for (InstructionGraphNode node : group.getNodes()) {
            if (!node.isXLoad()) continue;

            VarInsnNode insn = (VarInsnNode) node.getInstruction();
            FieldNode field = group.getFields()[insn.var];

            // change the load to ALOAD 0
            group.getInstructions().set(insn, new VarInsnNode(ALOAD, 0));

            // insert the correct GETFIELD after the xLoad
            group.getInstructions().insert(insn, new FieldInsnNode(GETFIELD,
                    group.getGroupClassType().getInternalName(), field.name, field.desc));

        }
    }

    protected void removeWrapper(InstructionGroup group) {
        InstructionGraphNode node = group.getRoot();
        AbstractInsnNode insn = group.getInstructions().getLast();
        Preconditions.checkState(node.getInstruction() == insn);

        if (insn.getPrevious().getOpcode() == SWAP && insn.getPrevious().getPrevious().getOpcode() == ALOAD) {
            group.getInstructions().remove(insn.getPrevious().getPrevious());
            group.getInstructions().remove(insn.getPrevious());
        } else {
            AbstractInsnNode firstInsn = getFirstOfSubtree(node).getInstruction();
            Checks.ensure(firstInsn == node.getPredecessors().get(0).getInstruction() &&
                    firstInsn.getOpcode() == ALOAD, "Error during bytecode analysis of rule method '%s': " +
                    "Unusual explicit action or capture", method.name);
            group.getInstructions().remove(firstInsn);
        }
        group.getInstructions().remove(insn);
    }

    public static InstructionGraphNode getFirstOfSubtree(InstructionGraphNode node) {
        InstructionGraphNode first = node;
        for (InstructionGraphNode predecessor : node.getPredecessors()) {
            InstructionGraphNode firstOfPred = getFirstOfSubtree(predecessor);
            if (first.getOriginalIndex() > firstOfPred.getOriginalIndex()) {
                first = firstOfPred;
            }
        }
        return first;
    }

}