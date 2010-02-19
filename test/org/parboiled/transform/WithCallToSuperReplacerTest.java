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

import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;
import static org.parboiled.transform.AsmUtils.getMethodByName;
import org.testng.annotations.Test;

public class WithCallToSuperReplacerTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testCallToSuperReplacement() throws Exception {
        ParserClassNode classNode = new ParserClassNode(TestParser.class);
        new ClassNodeInitializer(
                new MethodCategorizer(
                        new WithCallToSuperReplacer(null)
                )
        ).transform(classNode);

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "ch"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "    ALOAD 0\n" +
                "    ILOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser.ch (C)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "charRange"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "    ALOAD 0\n" +
                "    ILOAD 1\n" +
                "    ILOAD 2\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser.charRange (CC)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 3\n");
    }

}