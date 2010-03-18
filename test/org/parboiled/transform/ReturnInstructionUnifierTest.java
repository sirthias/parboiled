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

import org.parboiled.Rule;
import org.testng.annotations.Test;

import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;

public class ReturnInstructionUnifierTest {

    @SuppressWarnings({"UnusedDeclaration"})
    private class TestClass {
        Rule someRule() {
            if (getClass() != null) {
                return null;
            }
            if (toString() != null) {
                return null;
            }
            return (Rule) this;
        }
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        ParserClassNode classNode = new ClassNodeInitializer(null).transform(new ParserClassNode(TestClass.class));
        RuleMethod method = classNode.ruleMethods.get(0);
        new LineNumberRemover(
                new ReturnInstructionUnifier(null)
        ).transform(classNode, method);

        assertTraceDumpEquality(method, "" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL java/lang/Object.getClass ()Ljava/lang/Class;\n" +
                "    IFNULL L1\n" +
                "   L2\n" +
                "    ACONST_NULL\n" +
                "    GOTO L3\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL java/lang/Object.toString ()Ljava/lang/String;\n" +
                "    IFNULL L4\n" +
                "   L5\n" +
                "    ACONST_NULL\n" +
                "    GOTO L3\n" +
                "   L4\n" +
                "    ALOAD 0\n" +
                "    CHECKCAST org/parboiled/Rule\n" +
                "   L3\n" +
                "    ARETURN\n" +
                "   L6\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/ReturnInstructionUnifierTest$TestClass; L0 L6 0\n" +
                "    MAXSTACK = 1\n" +
                "    MAXLOCALS = 1\n");
    }

}