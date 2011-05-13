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
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds the required flag marking calls before the return instruction.
 */
class FlagMarkingGenerator implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.hasSuppressNodeAnnotation() || method.hasSuppressSubnodesAnnotation() ||
                method.hasSkipNodeAnnotation() || method.hasMemoMismatchesAnnotation();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        checkState(!method.isSuperMethod()); // super methods have flag moved to the overriding method
        
        InsnList instructions = method.instructions;

        AbstractInsnNode ret = instructions.getLast();
        while (ret.getOpcode() != ARETURN) {
            ret = ret.getPrevious();
        }

        // stack: <rule>
        instructions.insertBefore(ret, new InsnNode(DUP));
        // stack: <rule> :: <rule>
        LabelNode isNullLabel = new LabelNode();
        instructions.insertBefore(ret, new JumpInsnNode(IFNULL, isNullLabel));
        // stack: <rule>

        if (method.hasSuppressNodeAnnotation()) generateMarkerCall(instructions, ret, "suppressNode");
        if (method.hasSuppressSubnodesAnnotation()) generateMarkerCall(instructions, ret, "suppressSubnodes");
        if (method.hasSkipNodeAnnotation()) generateMarkerCall(instructions, ret, "skipNode");
        if (method.hasMemoMismatchesAnnotation()) generateMarkerCall(instructions, ret, "memoMismatches");
        
        // stack: <rule>
        instructions.insertBefore(ret, isNullLabel);
        // stack: <rule>
    }

    private void generateMarkerCall(InsnList instructions, AbstractInsnNode ret, String call) {
        instructions.insertBefore(ret, new MethodInsnNode(INVOKEINTERFACE, Types.RULE.getInternalName(), call,
                "()" + Types.RULE.getDescriptor()));
    }

}