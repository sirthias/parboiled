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
import org.objectweb.asm.tree.analysis.Value;
import org.parboiled.support.Checks;

class InstructionGrouper implements RuleMethodProcessor {

    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        this.method = method;
        addToGroup(method.getReturnInstructionNode(), new InstructionGroup(InstructionGroup.RETURN));
    }

    private void addToGroup(InstructionGraphNode node, InstructionGroup group) {
        if (node.isCaptureRoot()) {
            group = new InstructionGroup(InstructionGroup.CAPTURE);
        } else if (node.isActionRoot()) {
            group = new InstructionGroup(InstructionGroup.ACTION);
        }

        if (group.getType() == InstructionGroup.RETURN) {
            // do not add to RETURN group twice
            if (alreadyBelongsToGroupOfType(node, InstructionGroup.RETURN)) return;
        } else {
            Checks.ensure(!alreadyBelongsToGroupOfType(node, InstructionGroup.ACTION | InstructionGroup.CAPTURE),
                    "Method '" + method.name + "' contains illegal nesting of ACTION(...) and/or CAPTURE(...) calls");
        }

        node.getGroups().add(group);
        group.getNodes().add(node);

        // recurse into predecessors
        for (Value pred : node.getPredecessors()) {
            addToGroup((InstructionGraphNode) pred, group);
        }
    }

    private boolean alreadyBelongsToGroupOfType(InstructionGraphNode node, int groupTypes) {
        for (InstructionGroup group : node.getGroups()) {
            if ((group.getType() & groupTypes) > 0) return true;
        }
        return false;
    }

}