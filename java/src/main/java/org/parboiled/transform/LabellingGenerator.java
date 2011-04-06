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
import org.parboiled.common.StringUtils;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adds automatic labelling code before the return instruction.
 */
class LabellingGenerator implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return !method.hasDontLabelAnnotation();
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

        LabelNode isNullLabel = new LabelNode();
        // stack: <rule>
        instructions.insertBefore(ret, new InsnNode(DUP));
        // stack: <rule> :: <rule>
        instructions.insertBefore(ret, new JumpInsnNode(IFNULL, isNullLabel));
        // stack: <rule>
        instructions.insertBefore(ret, new LdcInsnNode(getLabelText(method)));
        // stack: <rule> :: <labelText>
        instructions.insertBefore(ret, new MethodInsnNode(INVOKEINTERFACE, Types.RULE.getInternalName(),
                "label", "(Ljava/lang/String;)" + Types.RULE_DESC));
        // stack: <rule>
        instructions.insertBefore(ret, isNullLabel);
        // stack: <rule>
    }

    public String getLabelText(RuleMethod method) {
        if (method.visibleAnnotations != null) {
            for (Object annotationObj : method.visibleAnnotations) {
                AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(Types.LABEL_DESC) && annotation.values != null) {
                    checkState("value".equals(annotation.values.get(0)));
                    String labelValue = (String) annotation.values.get(1);
                    return StringUtils.isEmpty(labelValue) ? method.name : labelValue;
                }
            }
        }
        return method.name;
    }

}