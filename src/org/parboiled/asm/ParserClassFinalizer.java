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

package org.parboiled.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.parboiled.support.Checks;
import org.jetbrains.annotations.NotNull;

public class ParserClassFinalizer implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public ParserClassFinalizer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (Object methodObj : classNode.methods) {
            createCachingConstructs(classNode, (MethodNode) methodObj);
        }

        Checks.ensure(!classNode.constructors.isEmpty(),
                "Could not extend parser class '" + classNode.getParentType().getClassName() +
                        "'. No constructor visible to derived classes found!");

        for (MethodNode constructor : classNode.constructors) {
            createConstuctor(classNode, constructor);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    @SuppressWarnings({"unchecked"})
    private void createConstuctor(ParserClassNode classNode, MethodNode constructor) {
        InsnList instructions = constructor.instructions;
        Type[] argTypes = Type.getArgumentTypes(constructor.desc);
        for (int i = 0; i <= argTypes.length; i++) {
            instructions.add(new VarInsnNode(ALOAD, i));
        }
        instructions.add(new MethodInsnNode(INVOKESPECIAL, classNode.getParentType().getInternalName(),
                "<init>", constructor.desc));
        instructions.add(new InsnNode(RETURN));

        classNode.methods.add(constructor);
    }

    @SuppressWarnings({"unchecked"})
    private void createCachingConstructs(ParserClassNode classNode, MethodNode method) {
        String cacheFieldName = "cache$" + method.name;
        String ruleTypeDesc = AsmUtils.RULE_TYPE.getDescriptor();
        String proxyMatcherType = AsmUtils.PROXY_MATCHER_TYPE.getInternalName();
        InsnList instructions = method.instructions;

        // create a caching field for the rule
        classNode.fields.add(new FieldNode(ACC_PRIVATE, cacheFieldName, ruleTypeDesc, null, null));

        // skip starting label and linenumber instructions
        AbstractInsnNode current = instructions.getFirst();
        while (current.getType() == AbstractInsnNode.LABEL || current.getType() == AbstractInsnNode.LINE) {
            current = current.getNext();
        }

        // insert:
        // if (cacheField == null) {
        //     cacheField = new ProxyMatcher();

        // stack:
        instructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        // stack: <this>
        instructions.insertBefore(current, new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack: <cacheField>
        LabelNode cacheHitLabel = new LabelNode();
        instructions.insertBefore(current, new JumpInsnNode(IFNONNULL, cacheHitLabel));
        // stack:
        instructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        // stack: <this>
        instructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <this>
        instructions.insertBefore(current, new TypeInsnNode(NEW, proxyMatcherType));
        // stack: <this> :: <this> :: <proxyMatcher>
        instructions.insertBefore(current, new InsnNode(DUP_X1));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher>
        instructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher> :: <proxyMatcher>
        instructions.insertBefore(current, new MethodInsnNode(INVOKESPECIAL, proxyMatcherType, "<init>", "()V"));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher>
        instructions.insertBefore(current, new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack: <this> :: <proxyMatcher>

        // seek through result creating code to return instruction
        while (current.getOpcode() != ARETURN) {
            current = current.getNext();
        }

        // insert:
        //     if (<rule> instanceof AbstractMatcher && !((AbstractMatcher)<rule>).isLocked()) {
        //        <rule>.label("someRuleCached");
        //        <rule>.lock();
        //     }
        //     <proxyMatcher>.arm(<rule>)
        //     cacheField = <rule>
        // }
        // return cacheField;

        // stack: <this> :: <proxyMatcher> :: <rule>
        instructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <proxyMatcher> :: <rule> :: <rule>
        instructions
                .insertBefore(current, new TypeInsnNode(INSTANCEOF, AsmUtils.ABSTRACT_MATCHER_TYPE.getInternalName()));
        // stack: <this> :: <proxyMatcher> :: <rule> :: <0 or 1>
        LabelNode elseLabel = new LabelNode();
        instructions.insertBefore(current, new JumpInsnNode(IFEQ, elseLabel));
        // stack: <this> :: <proxyMatcher> :: <rule>
        instructions
                .insertBefore(current, new TypeInsnNode(CHECKCAST, AsmUtils.ABSTRACT_MATCHER_TYPE.getInternalName()));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher>
        instructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher> :: <abstractMatcher>
        instructions.insertBefore(current, new MethodInsnNode(INVOKEVIRTUAL, AsmUtils.ABSTRACT_MATCHER_TYPE
                .getInternalName(), "isLocked", "()Z"));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher> :: <0 or 1>
        instructions.insertBefore(current, new JumpInsnNode(IFNE, elseLabel));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher>
        instructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher> :: <abstractMatcher>
        instructions.insertBefore(current, new LdcInsnNode(method.name));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher> :: <abstractMatcher> :: <methodname>
        instructions.insertBefore(current, new MethodInsnNode(INVOKEINTERFACE, AsmUtils.RULE_TYPE.getInternalName(),
                "label", Type.getMethodDescriptor(AsmUtils.RULE_TYPE, new Type[] {Type.getType(String.class)})));
        // stack: <this> :: <proxyMatcher> :: <abstractMatcher> :: <rule>
        instructions.insertBefore(current, new InsnNode(SWAP));
        // stack: <this> :: <proxyMatcher> :: <rule> :: <abstractMatcher>
        instructions.insertBefore(current, new MethodInsnNode(INVOKEVIRTUAL, AsmUtils.ABSTRACT_MATCHER_TYPE
                .getInternalName(), "lock", "()V"));
        // stack: <this> :: <proxyMatcher> :: <rule>
        instructions.insertBefore(current, elseLabel);
        // stack: <this> :: <proxyMatcher> :: <rule>
        instructions.insertBefore(current, new InsnNode(DUP_X1));
        // stack: <this> :: <rule> :: <proxyMatcher> :: <rule>
        instructions.insertBefore(current, new TypeInsnNode(CHECKCAST, AsmUtils.MATCHER_TYPE.getInternalName()));
        // stack: <this> :: <rule> :: <proxyMatcher> :: <matcher>
        instructions.insertBefore(current, new MethodInsnNode(INVOKEVIRTUAL, proxyMatcherType, "arm",
                Type.getMethodDescriptor(AsmUtils.PROXY_MATCHER_TYPE, new Type[] {AsmUtils.MATCHER_TYPE})));
        // stack: <this> :: <rule> :: <proxyMatcher>
        instructions.insertBefore(current, new InsnNode(POP));
        // stack: <this> :: <rule>
        instructions.insertBefore(current, new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack:
        instructions.insertBefore(current, cacheHitLabel);
        // stack:
        instructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        // stack: <this>
        instructions.insertBefore(current, new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack: <cacheField>
    }

}
