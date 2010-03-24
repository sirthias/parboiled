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
import org.objectweb.asm.tree.*;

/**
 * Inserts action group class and capture group call instantiation code at the groups respective placeholders.
 */
class RuleMethodRewriter implements RuleMethodProcessor, Opcodes, Types {

    private int actionNr;
    private int captureNr;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        actionNr = 0;
        captureNr = 0;
        for (InstructionGroup group : method.getGroups()) {
            rewriteGroup(method, group);
        }
    }

    private void rewriteGroup(RuleMethod method, InstructionGroup group) {
        String internalName = group.getGroupClassType().getInternalName();
        insert(group, new TypeInsnNode(NEW, internalName));
        insert(group, new InsnNode(DUP));
        insert(group, new LdcInsnNode(method.name +
                (group.getRoot().isActionRoot() ? "_Action" + ++actionNr : "_Capture" + ++captureNr))
        );
        insert(group, new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;)V"));
    }

    private void insert(InstructionGroup group, AbstractInsnNode insn) {
        group.getInstructions().insertBefore(group.getPlaceHolder(), insn);
    }

}

