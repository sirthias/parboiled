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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.HashSet;
import java.util.List;

import static org.parboiled.transform.AsmUtils.*;

abstract class GroupClassGenerator implements RuleMethodProcessor, Opcodes, Types {

    private static final Object lock = new Object();

    private final boolean forceCodeBuilding;
    protected ParserClassNode classNode;
    protected RuleMethod method;

    protected GroupClassGenerator(boolean forceCodeBuilding) {
        this.forceCodeBuilding = forceCodeBuilding;
    }

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
        ClassLoader classLoader = classNode.getParentClass().getClassLoader();

        Class<?> groupClass;
        synchronized (lock) {
            groupClass = findLoadedClass(className, classLoader);
            if (groupClass == null || forceCodeBuilding) {
                byte[] groupClassCode = generateGroupClassCode(group);
                group.setGroupClassCode(groupClassCode);
                if (groupClass == null) {
                    loadClass(className, groupClassCode, classLoader);
                }
            }
        }
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
        for (FieldNode field : group.getFields()) {
            // CAUTION: the FieldNode has illegal access flags and an illegal value field since these two members
            // are reused for other purposes, so we need to write out the field "manually" here rather than
            // just call "field.accept(cw)"
            cw.visitField(ACC_PUBLIC + ACC_SYNTHETIC, field.name, field.desc, null, null);
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
            AbstractInsnNode firstInsn = getFirstOfSubtree(instructions, node, new HashSet<InstructionGraphNode>())
                    .getInstruction();
            instructions.insertBefore(firstInsn, new VarInsnNode(ALOAD, 0));
            instructions.insertBefore(firstInsn, new MethodInsnNode(INVOKEVIRTUAL,
                    getBaseType().getInternalName(), contextSwitchType, "()V"));

            // replace original context-switching call with the opposite one, reversing the context switch done before
            instructions.insertBefore(node.getInstruction(), new VarInsnNode(ALOAD, 0));
            instructions.set(node.getInstruction(), new MethodInsnNode(INVOKEVIRTUAL,
                    getBaseType().getInternalName(), contextSwitchType.startsWith("UP") ? contextSwitchType
                            .replace("UP", "DOWN") : contextSwitchType.replace("DOWN", "UP"), "()V"));

            // remove Boolean.valueOf(boolean) if preceding
            AbstractInsnNode precedingInsn = node.getInstruction().getPrevious().getPrevious();
            if (isBooleanValueOfZ(precedingInsn)) {
                instructions.remove(precedingInsn);
            }

            // remove following Boolean.value
            AbstractInsnNode following = node.getInstruction().getNext();
            if (following.getType() == AbstractInsnNode.METHOD_INSN) {
                MethodInsnNode mi = (MethodInsnNode) following;
                if ("java/lang/Boolean".equals(mi.desc) && "booleanValue".equals(mi.name) && "()Z".equals(mi.desc)) {
                    instructions.remove(following);
                }
            }
        }
    }

    protected void insertSetContextCalls(InstructionGroup group) {
        InsnList instructions = group.getInstructions();
        for (InstructionGraphNode node : group.getNodes()) {
            if (!node.isCallOnContextAware()) continue;

            AbstractInsnNode loadTarget = node.getPredecessors().get(0).getInstruction();
            AbstractInsnNode afterFirstInsn = loadTarget.getNext();
            instructions.insertBefore(afterFirstInsn, new InsnNode(DUP));
            instructions.insertBefore(afterFirstInsn, new VarInsnNode(ALOAD, 0));
            instructions.insertBefore(afterFirstInsn, new FieldInsnNode(GETFIELD,
                    group.getGroupClassType().getInternalName(), "context", CONTEXT_DESC));
            instructions.insertBefore(afterFirstInsn, new MethodInsnNode(INVOKEINTERFACE,
                    CONTEXT_AWARE.getInternalName(), "setContext", "(" + CONTEXT_DESC + ")V"));
        }
    }

    protected void convertXLoads(InstructionGroup group) {
        String owner = group.getGroupClassType().getInternalName();
        for (InstructionGraphNode node : group.getNodes()) {
            if (!node.isXLoad()) continue;

            VarInsnNode insn = (VarInsnNode) node.getInstruction();
            FieldNode field = group.getFields().get(insn.var);

            // insert the correct GETFIELD after the xLoad
            group.getInstructions().insert(insn, new FieldInsnNode(GETFIELD, owner, field.name, field.desc));

            // change the load to ALOAD 0
            group.getInstructions().set(insn, new VarInsnNode(ALOAD, 0));
        }
    }

    public static InstructionGraphNode getFirstOfSubtree(InsnList instructions, InstructionGraphNode node,
                                                         HashSet<InstructionGraphNode> covered) {
        InstructionGraphNode first = node;
        if (!covered.contains(node)) {
            covered.add(node);
            for (InstructionGraphNode predecessor : node.getPredecessors()) {
                InstructionGraphNode firstOfPred = getFirstOfSubtree(instructions, predecessor, covered);
                if (instructions.indexOf(first.getInstruction()) > instructions.indexOf(firstOfPred.getInstruction())) {
                    first = firstOfPred;
                }
            }
        }
        return first;
    }

}