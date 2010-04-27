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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.parboiled.support.Checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.parboiled.transform.AsmUtils.getLoadingOpcode;

/**
 * Inserts action group class and capture group call instantiation code at the groups respective placeholders.
 */
class RuleMethodRewriter implements RuleMethodProcessor, Opcodes, Types {

    private RuleMethod method;
    private InstructionGroup group;
    private int actionNr;
    private int captureNr;
    private Map<InstructionGraphNode, Integer> captureVarIndices;

    public boolean appliesTo(@NotNull RuleMethod method) {
        return method.containsExplicitActions() || method.containsCaptures();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        this.method = method;
        actionNr = 0;
        captureNr = 0;
        captureVarIndices = null;

        for (InstructionGroup group : method.getGroups()) {
            this.group = group;
            createNewGroupClassInstance();
            initializeFields();
            if (group.getRoot().isCaptureRoot()) {
                insertStoreCapture();
            }
            removeGroupRootInstruction();
        }

        if (method.containsCaptures()) {
            finalizeCaptureSetup();
        }
    }

    private void createNewGroupClassInstance() {
        String internalName = group.getGroupClassType().getInternalName();
        insert(new TypeInsnNode(NEW, internalName));
        insert(new InsnNode(DUP));
        insert(new LdcInsnNode(method.name +
                (group.getRoot().isActionRoot() ? "_Action" + ++actionNr : "_Capture" + ++captureNr))
        );
        insert(new MethodInsnNode(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;)V"));

        if (method.hasSkipActionsInPredicatesAnnotation()) {
            insert(new InsnNode(DUP));
            insert(new MethodInsnNode(INVOKEVIRTUAL, internalName, "setSkipInPredicates", "()V"));
        }
    }

    private void initializeFields() {
        String internalName = group.getGroupClassType().getInternalName();
        for (FieldNode field : group.getFields()) {
            insert(new InsnNode(DUP));
            // the FieldNodes access and value members have been reused for the var index / Type respectively!
            insert(new VarInsnNode(getLoadingOpcode((Type) field.value), field.access));
            insert(new FieldInsnNode(PUTFIELD, internalName, field.name, field.desc));
        }
    }

    private void insertStoreCapture() {
        if (captureVarIndices == null) {
            captureVarIndices = new HashMap<InstructionGraphNode, Integer>();
        }
        int index = method.maxLocals++;
        captureVarIndices.put(group.getRoot(), index);

        insert(new InsnNode(DUP));
        insert(new VarInsnNode(ASTORE, index));
    }

    private void insert(AbstractInsnNode insn) {
        method.instructions.insertBefore(group.getRoot().getInstruction(), insn);
    }

    private void removeGroupRootInstruction() {
        method.instructions.remove(group.getRoot().getInstruction());
    }

    private void finalizeCaptureSetup() {
        Set<InstructionGroup> finalizedCaptureGroups = new HashSet<InstructionGroup>();
        for (InstructionGraphNode node : method.getGraphNodes()) {
            if (AsmUtils.isCallToRuleCreationMethod(node.getInstruction())) {
                insertSetContextRuleOnCaptureArguments(node, finalizedCaptureGroups);
            }
        }
        Checks.ensure(finalizedCaptureGroups.size() == captureVarIndices.size(), "Method '%s' contains illegal " +
                "CAPTURE(...) constructs that are not direct arguments to rule creating methods", method.name);
    }

    private void insertSetContextRuleOnCaptureArguments(InstructionGraphNode ruleCreationCall,
                                                        Set<InstructionGroup> finalizedCaptureGroups) {
        for (InstructionGraphNode predecessor : ruleCreationCall.getPredecessors()) {
            if (predecessor.isCaptureRoot()) {
                insertSetContextRule(ruleCreationCall, predecessor);
                finalizedCaptureGroups.add(predecessor.getGroup());
            }
        }
    }

    private void insertSetContextRule(InstructionGraphNode ruleCreationCall, InstructionGraphNode argument) {
        String internalName = argument.getGroup().getGroupClassType().getInternalName();
        AbstractInsnNode location = ruleCreationCall.getInstruction().getNext();
        // stack: <Rule>
        method.instructions.insertBefore(location, new InsnNode(DUP));
        // stack: <Rule> :: <Rule>
        method.instructions.insertBefore(location, new VarInsnNode(ALOAD, captureVarIndices.get(argument)));
        // stack: <Rule> :: <Rule> :: <Capture>
        method.instructions.insertBefore(location, new InsnNode(SWAP));
        // stack: <Rule> :: <Capture> :: <Rule>
        method.instructions.insertBefore(location, new FieldInsnNode(PUTFIELD, internalName, "contextRule", RULE_DESC));
        // stack: <Rule>
    }

}

