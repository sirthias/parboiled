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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.HashSet;
import java.util.Set;

import static org.parboiled.transform.AsmUtils.isBooleanValueOfZ;

/**
 * Makes all implicit action expressions in a rule method explicit.
 */
class ImplicitActionsConverter implements RuleMethodProcessor, Types, Opcodes {

    private final Set<InstructionGraphNode> covered = new HashSet<InstructionGraphNode>();
    private RuleMethod method;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsImplicitActions();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        this.method = method;
        covered.clear();
        walkNode(method.getReturnInstructionNode());
        method.setContainsImplicitActions(false);
    }

    private void walkNode(InstructionGraphNode node) {
        if (covered.contains(node)) return;
        covered.add(node);

        if (isBooleanValueOfZ(node.getInstruction())) {
            MethodInsnNode insn = new MethodInsnNode(INVOKESTATIC, BASE_PARSER.getInternalName(), "ACTION",
                    "(Z)" + ACTION_DESC);
            method.instructions.set(node.getInstruction(), insn);
            node.setIsActionRoot();
            node.setInstruction(insn);
            method.setContainsExplicitActions(true);
        }
        if (!node.isActionRoot() && !node.isCaptureRoot()) {
            for (InstructionGraphNode predecessor : node.getPredecessors()) {
                walkNode(predecessor);
            }
        }
    }
}