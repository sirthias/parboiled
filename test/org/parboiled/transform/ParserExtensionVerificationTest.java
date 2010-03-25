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

package org.parboiled.transform;

import org.testng.annotations.Test;

import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;
import static org.parboiled.transform.AsmTestUtils.verifyIntegrity;
import static org.parboiled.transform.AsmTestUtils.verifyMethodIntegrity;

public class ParserExtensionVerificationTest extends TransformationTest {

    @Test
    public void testSpecificMethod() throws Exception {
        ParserClassNode classNode = ParserTransformer.extendParserClass(TestParser.class);
        RuleMethod ruleMethod = processMethod("RuleWithIndirectImplicitAction",
                ParserTransformer.createRuleMethodProcessors());
        try {
            verifyMethodIntegrity(classNode.name, ruleMethod);
        } catch (Exception e) {
            System.out.println(getMethodInstructionList(ruleMethod));
            throw e;
        }
    }

    //@Test
    public void verifyTestParserExtension() throws Exception {
        ParserClassNode classNode = ParserTransformer.extendParserClass(TestParser.class);
        verifyIntegrity(classNode.name, classNode.getClassCode());

        for (RuleMethod method : classNode.getRuleMethods()) {
            for (InstructionGroup group : method.getGroups()) {
                verifyIntegrity(group.getGroupClassType().getInternalName(), group.getGroupClassCode());
            }
        }
    }

}