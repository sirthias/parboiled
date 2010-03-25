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
import org.objectweb.asm.Opcodes;
import org.parboiled.support.Checks;

import java.util.*;

class InstructionGroupCreator implements RuleMethodProcessor, Opcodes {

    private final Set<InstructionGraphNode> covered = new HashSet<InstructionGraphNode>();
    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsExplicitActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        covered.clear();
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
    }

    private void markGroup(InstructionGraphNode node, InstructionGroup group) {
        if (covered.contains(node)) return;
        covered.add(node);

        Checks.ensure(node == group.getRoot() || (!node.isCaptureRoot() && !node.isActionRoot()),
                "Method '%s' contains illegal nesting of ACTION(...) and/or CAPTURE(...) constructs", method.name);
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

}