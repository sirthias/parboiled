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

package org.parboiled.transform;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.parboiled.common.Base64;
import org.parboiled.support.Checks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class InstructionGroupCreator implements RuleMethodProcessor, Opcodes {

    private static final Base64 CUSTOM_BASE64 =
            new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789äö_");

    private MessageDigest md5Digest;
    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        this.method = method;

        // create groups
        createActionAndCaptureGroups();

        // prepare groups for later stages
        for (InstructionGroup group : method.getGroups()) {
            sort(group);
            verify(group);
            extractInstructions(group);
            normalize(group);
            name(group);
        }
    }

    private void createActionAndCaptureGroups() {
        for (InstructionGraphNode node : method.getGraphNodes()) {
            if (node.isActionRoot() || node.isCaptureRoot()) {
                InstructionGroup group = new InstructionGroup(node);
                markGroup(node, group);
                method.getGroups().add(group);
            }
        }
    }

    private void markGroup(InstructionGraphNode node, InstructionGroup group) {
        Checks.ensure(!node.isCaptureRoot() || !node.isActionRoot() || node == group.getRoot(),
                "Method '%s' contains illegal nesting of ACTION(...) and/or CAPTURE(...) calls", method.name);
        Checks.ensure(!node.isXStore(), "An ACTION or CAPTURE in rule method '%s' contains illegal writes to a " +
                "local variable or parameter", method.name);

        node.setGroup(group);
        if (!node.isXLoad()) {
            for (InstructionGraphNode pred : node.getPredecessors()) {
                markGroup(pred, group);
            }
        }
    }

    // sort the group instructions according to their method index
    private void sort(InstructionGroup group) {
        Collections.sort(group.getNodes(), new Comparator<InstructionGraphNode>() {
            public int compare(InstructionGraphNode a, InstructionGraphNode b) {
                return Integer.valueOf(a.getOriginalIndex()).compareTo(b.getOriginalIndex());
            }
        });
    }

    // ensure group instructions form a continuous block in the method
    private void verify(InstructionGroup group) {
        List<InstructionGraphNode> nodes = group.getNodes();
        int sizeMinus1 = nodes.size() - 1;
        Checks.ensure(nodes.get(sizeMinus1).getOriginalIndex() - nodes.get(0).getOriginalIndex() == sizeMinus1,
                "Error during bytecode analysis of rule method '%s': Incontinuous group block", method.name);
    }

    // move all group instructions from the underlying method into the groups Insnlist
    private void extractInstructions(InstructionGroup group) {
        // first insert a placeholder, so we later find the location were to insert the replacement code
        group.setPlaceHolder(new InsnNode(NOP));
        group.getInstructions().insert(group.getRoot().getInstruction(), group.getPlaceHolder());

        for (InstructionGraphNode node : group.getNodes()) {
            AbstractInsnNode insn = node.getInstruction();
            method.instructions.remove(insn);
            group.getInstructions().add(insn);
        }
    }

    // normalize all xLoad instructions
    private void normalize(InstructionGroup group) {
        int index = 0;
        for (InstructionGraphNode node : group.getNodes()) {
            if (node.isXLoad()) {
                ((VarInsnNode) node.getInstruction()).var = index++;
            }
        }

        // initialize the fields array we will need
        group.setFields(new FieldNode[index]);
    }

    // set a group name base on the hash across all group instructions
    private void name(InstructionGroup group) {
        // we use a classWriter to serialize the group instructions into a byte buffer
        ClassWriter classWriter = new ClassWriter(0);
        group.getInstructions().accept(classWriter.visitMethod(ACC_PUBLIC, "dummy", "()V", null, null));
        byte[] buffer = classWriter.toByteArray();

        // generate an MD5 hash across the buffer but throw away the last byte to have a bit count divisible by 6
        byte[] hash = md5hash(buffer);
        byte[] hash120Bits = new byte[15];
        System.arraycopy(hash, 0, hash120Bits, 0, 15);

        // generate a name for the group based on the hash
        String name = group.getRoot().isActionRoot() ? "Action$" : "Capture$";
        name += CUSTOM_BASE64.encodeToString(hash120Bits, false);
        group.setName(name);
    }

    private byte[] md5hash(byte[] buffer) {
        try {
            if (md5Digest == null) md5Digest = MessageDigest.getInstance("MD5");
            return md5Digest.digest(buffer);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}