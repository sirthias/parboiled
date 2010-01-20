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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class RuleCachingGenerator implements ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;

    public RuleCachingGenerator(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public Class<?> transform(ParserClassNode classNode) throws Exception {
        for (Object methodObj : classNode.methods) {
            createCachingConstructs(classNode, (MethodNode) methodObj);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : null;
    }

    @SuppressWarnings({"unchecked"})
    private void createCachingConstructs(ParserClassNode classNode, MethodNode method) {
        String cacheFieldName = "cache$" + method.name;
        String ruleTypeDesc = Types.RULE_TYPE.getDescriptor();
        String proxyMatcherType = Types.PROXY_MATCHER_TYPE.getInternalName();
        InsnList methodInstructions = method.instructions;

        // create a caching field for the rule
        classNode.fields.add(new FieldNode(ACC_PRIVATE, cacheFieldName, ruleTypeDesc, null, null));

        // skip starting label and linenumber instructions
        AbstractInsnNode current = methodInstructions.getFirst();
        while (current.getType() == AbstractInsnNode.LABEL || current.getType() == AbstractInsnNode.LINE) {
            current = current.getNext();
        }

        // insert:
        // if (cacheField == null) {
        //     cacheField = new ProxyMatcher();
        methodInstructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        methodInstructions.insertBefore(current,
                new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        LabelNode elseLabel = new LabelNode();
        methodInstructions.insertBefore(current, new JumpInsnNode(IFNONNULL, elseLabel));
        // stack:
        methodInstructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        // stack: <this>
        methodInstructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <this>
        methodInstructions.insertBefore(current, new TypeInsnNode(NEW, proxyMatcherType));
        // stack: <this> :: <this> :: <proxyMatcher>
        methodInstructions.insertBefore(current, new InsnNode(DUP_X1));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher>
        methodInstructions.insertBefore(current, new InsnNode(DUP));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher> :: <proxyMatcher>
        methodInstructions.insertBefore(current, new MethodInsnNode(INVOKESPECIAL, proxyMatcherType, "<init>", "()V"));
        // stack: <this> :: <proxyMatcher> :: <this> :: <proxyMatcher>
        methodInstructions.insertBefore(current,
                new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack: <this> :: <proxyMatcher>

        // seek through result creating code to return instruction
        while (current.getOpcode() != ARETURN) {
            current = current.getNext();
        }

        // insert:
        //     <proxyMatcher>.arm(<result>)
        //     cacheField = <result>
        // }
        // return cacheField;

        // stack: <this> :: <proxyMatcher> :: <result>
        methodInstructions.insertBefore(current, new InsnNode(DUP_X1));
        // stack: <this> :: <result> :: <proxyMatcher> :: <result>
        methodInstructions.insertBefore(current, new TypeInsnNode(CHECKCAST, Types.MATCHER_TYPE.getInternalName()));
        // stack: <this> :: <result> :: <proxyMatcher> :: <Matcher>
        methodInstructions.insertBefore(current, new MethodInsnNode(INVOKEVIRTUAL, proxyMatcherType, "arm",
                Type.getMethodDescriptor(Types.PROXY_MATCHER_TYPE, new Type[] {Types.MATCHER_TYPE})));
        // stack: <this> :: <result> :: <proxyMatcher>
        methodInstructions.insertBefore(current, new InsnNode(POP));
        // stack: <this> :: <result>
        methodInstructions.insertBefore(current,
                new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
        // stack:
        methodInstructions.insertBefore(current, elseLabel);
        methodInstructions.insertBefore(current, new VarInsnNode(ALOAD, 0));
        methodInstructions.insertBefore(current,
                new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, ruleTypeDesc));
    }

}
