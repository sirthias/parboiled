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

public class CachingGeneratorTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        ParserClassNode classNode = new ParserClassNode(TestParser.class);
        new ClassNodeInitializer(
                new MethodCategorizer(
                        new ReturnInstructionUnifier(
                                new CachingGenerator(null)
                        )
                )
        ).transform(classNode);

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "ch"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 87 L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$ch : Ljava/util/HashMap;\n" +
                "    DUP\n" +
                "    IFNONNULL L1\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    NEW java/util/HashMap\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL java/util/HashMap.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$ch : Ljava/util/HashMap;\n" +
                "   L1\n" +
                "    ILOAD 1\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    DUP\n" +
                "    ASTORE 2\n" +
                "    INVOKEVIRTUAL java/util/HashMap.get (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    CHECKCAST org/parboiled/Rule\n" +
                "    DUP\n" +
                "    IFNULL L2\n" +
                "    ARETURN\n" +
                "   L2\n" +
                "    POP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    DUP\n" +
                "    ALOAD 2\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$ch : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    ILOAD 1\n" +
                "    LOOKUPSWITCH\n" +
                "      65007: L3\n" +
                "      65534: L4\n" +
                "      65535: L5\n" +
                "      default: L6\n" +
                "   L4\n" +
                "    LINENUMBER 89 L4\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.empty ()Lorg/parboiled/Rule;\n" +
                "    GOTO L7\n" +
                "   L3\n" +
                "    LINENUMBER 91 L3\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.any ()Lorg/parboiled/Rule;\n" +
                "    GOTO L7\n" +
                "   L5\n" +
                "    LINENUMBER 93 L5\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.eoi ()Lorg/parboiled/Rule;\n" +
                "    GOTO L7\n" +
                "   L6\n" +
                "    LINENUMBER 95 L6\n" +
                "    NEW org/parboiled/matchers/CharMatcher\n" +
                "    DUP\n" +
                "    ILOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/matchers/CharMatcher.<init> (C)V\n" +
                "   L7\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L8\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L8\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "    DUP\n" +
                "    ALOAD 2\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$ch : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    ARETURN\n" +
                "   L9\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L9 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE c C L0 L9 1\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(getMethodByName(classNode.cachedMethods, "optional"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 237 L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$optional : Ljava/util/HashMap;\n" +
                "    DUP\n" +
                "    IFNONNULL L1\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    NEW java/util/HashMap\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL java/util/HashMap.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$optional : Ljava/util/HashMap;\n" +
                "   L1\n" +
                "    ALOAD 1\n" +
                "    DUP\n" +
                "    ASTORE 2\n" +
                "    INVOKEVIRTUAL java/util/HashMap.get (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    CHECKCAST org/parboiled/Rule\n" +
                "    DUP\n" +
                "    IFNULL L2\n" +
                "    ARETURN\n" +
                "   L2\n" +
                "    POP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    DUP\n" +
                "    ALOAD 2\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$optional : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    NEW org/parboiled/matchers/OptionalMatcher\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.toRule (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    INVOKESPECIAL org/parboiled/matchers/OptionalMatcher.<init> (Lorg/parboiled/Rule;)V\n" +
                "    LDC \"optional\"\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/OptionalMatcher.label (Ljava/lang/String;)Lorg/parboiled/matchers/AbstractMatcher;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L3\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L3\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "    DUP\n" +
                "    ALOAD 2\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$optional : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    ARETURN\n" +
                "   L4\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L4 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE rule Ljava/lang/Object; L0 L4 1\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "empty"), "" +
                "   L0\n" +
                "    LINENUMBER 369 L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$empty : Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    IFNULL L1\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    POP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    SWAP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$empty : Lorg/parboiled/Rule;\n" +
                "    NEW org/parboiled/matchers/EmptyMatcher\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/EmptyMatcher.<init> ()V\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    SWAP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$empty : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L3 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 1\n");
    }

}