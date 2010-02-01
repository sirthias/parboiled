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

package org.parboiled.asm;

import static org.parboiled.asm.AsmTestUtils.assertTraceDumpEquality;
import static org.parboiled.asm.AsmUtils.getMethodByName;
import org.testng.annotations.Test;

public class ReturnInstructionUnifierTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        ParserClassNode classNode = new ParserClassNode(TestParser.class);
        new ClassNodeInitializer(
                new MethodCategorizer(
                        new ReturnInstructionUnifier(null)
                )
        ).transform(classNode);

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "ch"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 99 L0\n" +
                "    ILOAD 1\n" +
                "    LOOKUPSWITCH\n" +
                "      65007: L1\n" +
                "      65534: L2\n" +
                "      65535: L3\n" +
                "      default: L4\n" +
                "   L2\n" +
                "    LINENUMBER 101 L2\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.empty ()Lorg/parboiled/Rule;\n" +
                "    GOTO L5\n" +
                "   L1\n" +
                "    LINENUMBER 103 L1\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.any ()Lorg/parboiled/Rule;\n" +
                "    GOTO L5\n" +
                "   L3\n" +
                "    LINENUMBER 105 L3\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.eoi ()Lorg/parboiled/Rule;\n" +
                "    GOTO L5\n" +
                "   L4\n" +
                "    LINENUMBER 107 L4\n" +
                "    NEW org/parboiled/matchers/CharMatcher\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    GETFIELD org/parboiled/BaseParser._nextIndex : I\n" +
                "    DUP_X1\n" +
                "    ICONST_1\n" +
                "    IADD\n" +
                "    PUTFIELD org/parboiled/BaseParser._nextIndex : I\n" +
                "    ILOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/matchers/CharMatcher.<init> (IC)V\n" +
                "   L5\n" +
                "    ARETURN\n" +
                "   L6\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L6 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE c C L0 L6 1\n" +
                "    MAXSTACK = 6\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "charRange"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 136 L0\n" +
                "    ILOAD 1\n" +
                "    ILOAD 2\n" +
                "    IF_ICMPNE L1\n" +
                "    ALOAD 0\n" +
                "    ILOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.ch (C)Lorg/parboiled/Rule;\n" +
                "    GOTO L2\n" +
                "   L1\n" +
                "    NEW org/parboiled/matchers/CharRangeMatcher\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    GETFIELD org/parboiled/BaseParser._nextIndex : I\n" +
                "    DUP_X1\n" +
                "    ICONST_1\n" +
                "    IADD\n" +
                "    PUTFIELD org/parboiled/BaseParser._nextIndex : I\n" +
                "    ILOAD 1\n" +
                "    ILOAD 2\n" +
                "    INVOKESPECIAL org/parboiled/matchers/CharRangeMatcher.<init> (ICC)V\n" +
                "   L2\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L3 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE cLow C L0 L3 1\n" +
                "    LOCALVARIABLE cHigh C L0 L3 2\n" +
                "    MAXSTACK = 6\n" +
                "    MAXLOCALS = 3\n");
    }

}