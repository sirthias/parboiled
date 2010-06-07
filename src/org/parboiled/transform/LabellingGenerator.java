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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.parboiled.common.StringUtils;

/**
 * Adds automatic labelling code before the return instruction.
 */
class LabellingGenerator implements RuleMethodProcessor, Opcodes, Types {

    public boolean appliesTo(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) {
        return !method.hasDontLabelAnnotation();
    }

    public void process(@NotNull ParserClassNode classNode, @NotNull RuleMethod method) throws Exception {
        Preconditions.checkState(!method.isSuperMethod()); // super methods have flag moved to the overriding method

        InsnList instructions = method.instructions;

        AbstractInsnNode ret = instructions.getLast();
        while (ret.getOpcode() != ARETURN) {
            ret = ret.getPrevious();
        }

        LabelNode isNullLabel = new LabelNode();
        // stack: <rule>
        instructions.insertBefore(ret, new InsnNode(DUP));
        // stack: <rule> :: <rule>
        instructions.insertBefore(ret, new JumpInsnNode(IFNULL, isNullLabel));
        // stack: <rule>
        instructions.insertBefore(ret, new LdcInsnNode(getLabelText(method)));
        // stack: <rule> :: <labelText>
        instructions.insertBefore(ret, new MethodInsnNode(INVOKEINTERFACE, RULE.getInternalName(),
                "label", "(Ljava/lang/String;)" + RULE_DESC));
        // stack: <rule>
        instructions.insertBefore(ret, isNullLabel);
        // stack: <rule>
    }

    public String getLabelText(RuleMethod method) {
        if (method.visibleAnnotations != null) {
            for (Object annotationObj : method.visibleAnnotations) {
                AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(LABEL_DESC) && annotation.values != null) {
                    Preconditions.checkState("value".equals(annotation.values.get(0)));
                    String labelValue = (String) annotation.values.get(1);
                    return StringUtils.isEmpty(labelValue) ? method.name : labelValue;
                }
            }
        }
        return method.name;
    }

}