/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import static org.parboiled.common.Preconditions.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.parboiled.common.Base64;
import org.parboiled.common.StringUtils;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ALOAD;

class InstructionGroupPreparer implements RuleMethodProcessor {

    private static final Object lock = new Object();

    private static final Base64 CUSTOM_BASE64 =
            new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789äö_");

    private RuleMethod method;

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.containsExplicitActions() || method.containsVars();
    }

    public void process(ParserClassNode classNode, RuleMethod method) {
        this.method = checkArgNotNull(method, "method");

        // prepare groups for later stages
        for (InstructionGroup group : method.getGroups()) {
            extractInstructions(group);
            extractFields(group);
            name(group, classNode);
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
    private void name(InstructionGroup group, ParserClassNode classNode) {
        synchronized (lock) {
            // generate an MD5 hash across the buffer, use only the first 96 bit
            MD5Digester digester = new MD5Digester(classNode.name);
            group.getInstructions().accept(digester);
            byte[] hash = digester.getMD5Hash();
            byte[] hash96 = new byte[12];
            System.arraycopy(hash, 0, hash96, 0, 12);

            // generate a name for the group based on the hash
            String name = group.getRoot().isActionRoot() ? "Action$" : "VarInit$";
            name += CUSTOM_BASE64.encodeToString(hash96, false);
            group.setName(name);
        }
    }

    private static class MD5Digester extends EmptyVisitor {
        private static MessageDigest digest;
        private static ByteBuffer buffer;
        private final List<Label> labels = new ArrayList<Label>();
        private final String parserClassName;

        public MD5Digester(String parserClassName) {
            this.parserClassName = parserClassName;
            if (digest == null) {
                try {
                    digest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }
            if (buffer == null) {
                buffer = ByteBuffer.allocateDirect(4096);
            }
            buffer.clear();
        }

        @Override
        public void visitInsn(int opcode) {
            update(opcode);
        }

        @Override
        public void visitIntInsn(int opcode, int operand) {
            update(opcode);
            update(operand);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            update(opcode);
            update(var);
            if (opcode == ALOAD && var == 0) {
                // make sure the names of identical actions differ if they are defined in different parent classes
                update(parserClassName);
            }
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            update(opcode);
            update(type);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            update(opcode);
            update(owner);
            update(name);
            update(desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc) {
            update(opcode);
            update(owner);
            update(name);
            update(desc);
        }

        @Override
        public void visitJumpInsn(int opcode, Label label) {
            update(opcode);
            update(label);
        }

        @Override
        public void visitLabel(Label label) {
            update(label);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (cst instanceof String) {
                update((String) cst);
            } else if (cst instanceof Integer) {
                update((Integer) cst);
            } else if (cst instanceof Float) {
                ensureRemaining(4);
                buffer.putFloat((Float) cst);
            } else if (cst instanceof Long) {
                ensureRemaining(8);
                buffer.putLong((Long) cst);
            } else if (cst instanceof Double) {
                ensureRemaining(8);
                buffer.putDouble((Double) cst);
            } else {
                checkState(cst instanceof Type);
                update(((Type) cst).getInternalName());
            }
        }

        @Override
        public void visitIincInsn(int var, int increment) {
            update(var);
            update(increment);
        }

        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
            update(min);
            update(max);
            update(dflt);
            for (Label label : labels) {
                update(label);
            }
        }

        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            update(dflt);
            for (int i = 0; i < keys.length; i++) {
                update(keys[i]);
                update(labels[i]);
            }
        }

        @Override
        public void visitMultiANewArrayInsn(String desc, int dims) {
            update(desc);
            update(dims);
        }

        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            update(start);
            update(end);
            update(handler);
            update(type);
        }

        private void update(int i) {
            ensureRemaining(4);
            buffer.putInt(i);
        }

        private void update(String str) {
            if (StringUtils.isNotEmpty(str)) {
                int len = str.length();
                ensureRemaining(len * 2);
                for (int i = 0; i < len; i++) {
                    buffer.putChar(str.charAt(i));
                }
            }
        }

        private void update(Label label) {
            int index = labels.indexOf(label);
            if (index == -1) {
                index = labels.size();
                labels.add(label);
            }
            update(index);
        }

        private void ensureRemaining(int bytes) {
            if (buffer.remaining() < bytes) {
                digest();
            }
        }

        private void digest() {
            buffer.flip();
            digest.update(buffer);
            buffer.clear();
        }

        public byte[] getMD5Hash() {
            digest();
            return digest.digest();
        }
    }

}