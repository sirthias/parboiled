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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.parboiled.support.Checks;

import java.lang.reflect.Modifier;
import java.util.*;

import static org.parboiled.transform.AsmUtils.getClassField;
import static org.parboiled.transform.AsmUtils.getClassMethod;

class InstructionGroupCreator implements RuleMethodProcessor, Opcodes {

    private final Map<String, Integer> memberModifiers = new HashMap<String, Integer>();
    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsExplicitActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        this.method = method;

        // create groups
        createActionAndCaptureGroups();

        // prepare groups for later stages
        for (InstructionGroup group : method.getGroups()) {
            sort(group);
            verify(group);
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

        // also capture all group nodes "hidden" behind xLoads
        boolean repeat = true;
        while (repeat) {
            repeat = false;
            for (InstructionGraphNode node : method.getGraphNodes()) {
                if (node.getGroup() != null) continue;
                for (InstructionGraphNode predecessor : node.getPredecessors()) {
                    if (predecessor.getGroup() != null && predecessor != predecessor.getGroup().getRoot()) {
                        node.setGroup(predecessor.getGroup());
                        repeat = true;
                        break;
                    }
                }
            }
        }
    }

    private void markGroup(InstructionGraphNode node, InstructionGroup group) {
        Checks.ensure(node == group.getRoot() || (!node.isCaptureRoot() && !node.isActionRoot()),
                "Method '%s' contains illegal nesting of ACTION(...) and/or CAPTURE(...) constructs", method.name);

        if (node.getGroup() != null) return; // already visited

        node.setGroup(group);
        if (!node.isXLoad()) {
            for (InstructionGraphNode pred : node.getPredecessors()) {
                markGroup(pred, group);
            }
        }
    }

    // sort the group instructions according to their method index
    private void sort(InstructionGroup group) {
        final InsnList instructions = method.instructions;
        Collections.sort(group.getNodes(), new Comparator<InstructionGraphNode>() {
            public int compare(InstructionGraphNode a, InstructionGraphNode b) {
                return Integer.valueOf(instructions.indexOf(a.getInstruction()))
                        .compareTo(instructions.indexOf(b.getInstruction()));
            }
        });
    }

    private void verify(InstructionGroup group) {
        List<InstructionGraphNode> nodes = group.getNodes();
        int sizeMinus1 = nodes.size() - 1;

        // verify all instruction except for the last one (which must be the root)
        Preconditions.checkState(nodes.get(sizeMinus1) == group.getRoot());
        for (int i = 0; i < sizeMinus1; i++) {
            verify(nodes.get(i));
        }

        //Checks.ensure(nodes.get(sizeMinus1).getOriginalIndex() - nodes.get(0).getOriginalIndex() == sizeMinus1,
        //        "Error during bytecode analysis of rule method '%s': Incontinuous group block", method.name);
    }

    private void verify(InstructionGraphNode node) {
        Checks.ensure(!node.isXStore(), "An ACTION or CAPTURE in rule method '%s' contains illegal writes to a " +
                "local variable or parameter", method.name);

        switch (node.getInstruction().getOpcode()) {
            case GETFIELD:
            case GETSTATIC:
                FieldInsnNode field = (FieldInsnNode) node.getInstruction();
                Checks.ensure(!isPrivateField(field.owner, field.name),
                        "Rule method '%s' contains an illegal access to private field '%s'.\n" +
                                "Mark the field protected or package-private if you want to prevent public access!",
                        method.name, field.name);
                break;

            case INVOKEVIRTUAL:
            case INVOKESTATIC:
            case INVOKESPECIAL:
            case INVOKEINTERFACE:
                MethodInsnNode calledMethod = (MethodInsnNode) node.getInstruction();
                Checks.ensure(!isPrivateMethod(calledMethod.owner, calledMethod.name, calledMethod.desc),
                        "Rule method '%s' contains an illegal call to private method '%s'.\nMark '%s' protected or " +
                                "package-private if you want to prevent public access!",
                        method.name, calledMethod.name, calledMethod.name);
                break;
        }
    }

    private boolean isPrivateField(String owner, String name) {
        String key = owner + '#' + name;
        Integer modifiers = memberModifiers.get(key);
        if (modifiers == null) {
            modifiers = getClassField(owner, name).getModifiers();
            memberModifiers.put(key, modifiers);
        }
        return Modifier.isPrivate(modifiers);
    }

    private boolean isPrivateMethod(String owner, String name, String desc) {
        String key = owner + '#' + name + '#' + desc;
        Integer modifiers = memberModifiers.get(key);
        if (modifiers == null) {
            modifiers = getClassMethod(owner, name, desc).getModifiers();
            memberModifiers.put(key, modifiers);
        }
        return Modifier.isPrivate(modifiers);
    }

}