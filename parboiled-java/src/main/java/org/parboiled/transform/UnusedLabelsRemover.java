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
import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * Removes all unused labels.
 */
class UnusedLabelsRemover implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        return true;
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        AbstractInsnNode current = method.instructions.getFirst();
        while (current != null) {
            AbstractInsnNode next = current.getNext();
            if (current.getType() == AbstractInsnNode.LABEL && !method.getUsedLabels().contains(current)) {
                method.instructions.remove(current);
            }
            current = next;
        }
    }

}