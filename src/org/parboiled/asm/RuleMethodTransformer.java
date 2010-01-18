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

import java.util.List;

public class RuleMethodTransformer implements Opcodes {

    private final ParserClassNode classNode;

    public RuleMethodTransformer(ParserClassNode classNode) {
        this.classNode = classNode;
    }

    public void transformRuleMethods(List<RuleMethodInfo> methodInfos) {
        for (RuleMethodInfo methodInfo : methodInfos) {
            transformMethod(methodInfo);
        }
    }

    private void transformMethod(RuleMethodInfo methodInfo) {
        if (methodInfo.hasActions()) {
            int actionNr = 1;
            for (InstructionSubSet subSet : methodInfo.getInstructionSubSets()) {
                ActionClassGenerator generator = new ActionClassGenerator(classNode, methodInfo, subSet, actionNr++);
            }
        }
    }

}

