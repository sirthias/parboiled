/*
 * Copyright (C) 2009 Mathias Doenitz
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

package org.parboiled.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.common.Preconditions;

import java.util.List;

public class RuleMethodInfo {

    public final MethodNode method;
    public final InstructionGraphNode[] instructionGraphNodes;

    private List<InstructionSubSet> instructionSubSets;

    public RuleMethodInfo(MethodNode method) {
        int n = method.instructions.size();
        this.method = method;
        this.instructionGraphNodes = new InstructionGraphNode[n];
    }

    public InstructionGraphNode getReturnNode() {
        InstructionGraphNode node = instructionGraphNodes[instructionGraphNodes.length - 2];
        Preconditions.checkState(node == null || node.instruction.getOpcode() == Opcodes.ARETURN);
        return node;
    }

    public boolean hasActions() {
        return instructionSubSets != null;
    }

    public List<InstructionSubSet> getInstructionSubSets() {
        return instructionSubSets;
    }

    protected void setInstructionSubSets(List<InstructionSubSet> instructionSubSets) {
        this.instructionSubSets = instructionSubSets;
    }

}
