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

import com.google.common.collect.Lists;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;

import java.util.List;

/**
 * A group of instructions belongig to a CAPTURE or ACTION
 */
class InstructionGroup {

    private final List<InstructionGraphNode> nodes = Lists.newArrayList();
    private final InsnList instructions = new InsnList();
    private final InstructionGraphNode root;
    private AbstractInsnNode placeHolder;
    private String name;
    private Type groupClassType;
    private Class<?> groupClass;
    private FieldNode[] fields;

    public InstructionGroup(InstructionGraphNode root) {
        this.root = root;
    }

    public InstructionGraphNode getRoot() {
        return root;
    }

    public List<InstructionGraphNode> getNodes() {
        return nodes;
    }

    public InsnList getInstructions() {
        return instructions;
    }

    public AbstractInsnNode getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(AbstractInsnNode placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getGroupClassType() {
        return groupClassType;
    }

    public void setGroupClassType(Type groupClassType) {
        this.groupClassType = groupClassType;
    }

    public Class<?> getGroupClass() {
        return groupClass;
    }

    public void setGroupClass(Class<?> groupClass) {
        this.groupClass = groupClass;
    }

    public FieldNode[] getFields() {
        return fields;
    }

    public void setFields(FieldNode[] fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
}
