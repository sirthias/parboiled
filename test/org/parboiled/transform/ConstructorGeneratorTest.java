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

import org.objectweb.asm.tree.MethodNode;
import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;
import static org.parboiled.transform.AsmUtils.getMethodByName;
import org.testng.annotations.Test;

import java.util.List;

public class ConstructorGeneratorTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testConstructorGeneration() throws Exception {
        ParserClassNode classNode = new ParserClassNode(TestParser.class);
        new ClassNodeInitializer(
                new MethodCategorizer(
                        new ConstructorGenerator(null)
                )
        ).transform(classNode);

        assertTraceDumpEquality(getMethodByName((List<MethodNode>) classNode.methods, "<init>"), "" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 0\n" +
                "    MAXLOCALS = 0\n");
    }

}