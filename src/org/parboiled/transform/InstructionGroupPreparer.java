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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.parboiled.common.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

class InstructionGroupPreparer implements RuleMethodProcessor, Opcodes {

    private static final Base64 CUSTOM_BASE64 =
            new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789äö_");

    private MessageDigest md5Digest;
    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsExplicitActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        this.method = method;

        // prepare groups for later stages
        for (InstructionGroup group : method.getGroups()) {
            extractInstructions(group);
            extractFields(group);
            name(group);
        }
    }

    // move all group instructions except for the root from the underlying method into the groups Insnlist
    private void extractInstructions(InstructionGroup group) {
        for (InstructionGraphNode node : group.getNodes()) {
            if (node != group.getRoot()) {
                AbstractInsnNode insn = node.getInstruction();
                method.instructions.remove(insn);
                group.getInstructions().add(insn);
            }
        }
    }

    // create FieldNodes for all xLoad instructions
    private void extractFields(InstructionGroup group) {
        List<FieldNode> fields = group.getFields();
        for (InstructionGraphNode node : group.getNodes()) {
            if (node.isXLoad()) {
                VarInsnNode insn = (VarInsnNode) node.getInstruction();

                // check whether we already have a field for the var with this index
                int index;
                for (index = 0; index < fields.size(); index++) {
                    if (fields.get(index).access == insn.var) break;
                }

                // if we don't, create a new field for the var
                if (index == fields.size()) {
                    // CAUTION, HACK!: for brevity we reuse the access field and the value field of the FieldNode
                    // for keeping track of the original var index as well as the FieldNodes Type (respectively)
                    // so we need to make sure that we correct for this when the field is actually written
                    Type type = node.getResultValue().getType();
                    fields.add(new FieldNode(insn.var, "field$" + index, type.getDescriptor(), null, type));
                }

                // normalize the instruction so instruction groups that are identical except for the variable
                // indexes are still mapped to the same group class (name)
                insn.var = index;
            }
        }
    }

    // set a group name base on the hash across all group instructions
    private void name(InstructionGroup group) {
        // we use a classWriter to serialize the group instructions into a byte buffer
        ClassWriter classWriter = new ClassWriter(0);
        group.getInstructions().accept(classWriter.visitMethod(ACC_PUBLIC, "dummy", "()V", null, null));
        byte[] buffer = classWriter.toByteArray();
        group.getInstructions().resetLabels(); // we need to reset all labels before being using another ClassWriter

        // generate an MD5 hash across the buffer, use only the first 96 bit
        byte[] hash = md5hash(buffer);
        byte[] hash96 = new byte[12];
        System.arraycopy(hash, 0, hash96, 0, 12);

        // generate a name for the group based on the hash
        String name = group.getRoot().isActionRoot() ? "Action$" : "Capture$";
        name += CUSTOM_BASE64.encodeToString(hash96, false);
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