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

import org.parboiled.Rule;
import static org.parboiled.asm.AsmTestUtils.verifyIntegrity;
import org.parboiled.common.Utils;
import static org.parboiled.common.Utils.findConstructor;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class ParserExtensionVerificationTest extends AbstractTest {

    @Test
    public void verifyParserExtension() throws Exception {
        TestParser parser = createAndVerifyTestParser();
        Rule rule = parser.noActionRule();
        test(parser, rule, "abcb", "" +
                "[noActionRule] 'abcb'\n" +
                "    ['a'] 'a'\n" +
                "    [bsAndCs] 'bcb'\n" +
                "        [sequence] 'bc'\n" +
                "            ['b'] 'b'\n" +
                "            [optional] 'c'\n" +
                "                ['c'] 'c'\n" +
                "        [sequence] 'b'\n" +
                "            ['b'] 'b'\n" +
                "            [optional]\n");
    }

    public TestParser createAndVerifyTestParser() throws Exception {
        ClassTransformer transformer = ParserTransformer.createTransformer();
        ParserClassNode classNode = transformer.transform(new ParserClassNode(TestParser.class));

        verifyIntegrity(classNode.name, classNode.classCode);

        for (ActionClassGenerator generator : classNode.actionClassGenerators) {
            verifyIntegrity(generator.actionType.getInternalName(), generator.actionClassCode);
        }

        return (TestParser) findConstructor(classNode.extendedClass, Utils.EMPTY_OBJECT_ARRAY).newInstance();
    }

}
