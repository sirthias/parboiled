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
import static org.parboiled.asm.AsmTestUtils.getByName;
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

        assertTraceDumpEquality(getByName(classNode.ruleMethods, "empty"), "" +
                "   L0\n" +
                "    LINENUMBER 292 L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$empty : Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    IFNULL L1\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
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
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"empty\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    SWAP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$empty : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L3 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 1\n");

        assertTraceDumpEquality(getByName(classNode.cachedMethods, "optional"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 199 L0\n" +
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
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
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
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    DUP\n" +
                "    ALOAD 2\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$optional : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L3 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE rule Ljava/lang/Object; L0 L3 1\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(getByName(classNode.cachedMethods, "sequence"), "" +
                "  @Lorg/parboiled/support/Cached;()\n" +
                "   L0\n" +
                "    LINENUMBER 212 L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$sequence : Ljava/util/HashMap;\n" +
                "    DUP\n" +
                "    IFNONNULL L1\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    NEW java/util/HashMap\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL java/util/HashMap.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$sequence : Ljava/util/HashMap;\n" +
                "   L1\n" +
                "    NEW org/parboiled/asm/CachingGenerator$Arguments\n" +
                "    DUP\n" +
                "    BIPUSH 3\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    BIPUSH 0\n" +
                "    ALOAD 1\n" +
                "    AASTORE\n" +
                "    DUP\n" +
                "    BIPUSH 1\n" +
                "    ALOAD 2\n" +
                "    AASTORE\n" +
                "    DUP\n" +
                "    BIPUSH 2\n" +
                "    ALOAD 3\n" +
                "    AASTORE\n" +
                "    INVOKESPECIAL org/parboiled/asm/CachingGenerator$Arguments.<init> ([Ljava/lang/Object;)V\n" +
                "    DUP\n" +
                "    ASTORE 4\n" +
                "    INVOKEVIRTUAL java/util/HashMap.get (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    CHECKCAST org/parboiled/Rule\n" +
                "    DUP\n" +
                "    IFNULL L2\n" +
                "    ARETURN\n" +
                "   L2\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    DUP\n" +
                "    ALOAD 4\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$sequence : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    NEW org/parboiled/matchers/SequenceMatcher\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    ALOAD 2\n" +
                "    ALOAD 3\n" +
                "    INVOKESTATIC org/parboiled/common/Utils.arrayOf (Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;\n" +
                "    INVOKESTATIC org/parboiled/common/Utils.arrayOf (Ljava/lang/Object;[Ljava/lang/Object;)[Ljava/lang/Object;\n" +
                "    INVOKEVIRTUAL org/parboiled/BaseParser.toRules ([Ljava/lang/Object;)[Lorg/parboiled/Rule;\n" +
                "    ICONST_0\n" +
                "    INVOKESPECIAL org/parboiled/matchers/SequenceMatcher.<init> ([Lorg/parboiled/Rule;Z)V\n" +
                "    LDC \"sequence\"\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/SequenceMatcher.label (Ljava/lang/String;)Lorg/parboiled/matchers/AbstractMatcher;\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    DUP\n" +
                "    ALOAD 4\n" +
                "    SWAP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$sequence : Ljava/util/HashMap;\n" +
                "    DUP_X2\n" +
                "    POP\n" +
                "    INVOKEVIRTUAL java/util/HashMap.put (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    POP\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/BaseParser; L0 L3 0\n" +
                "    // signature Lorg/parboiled/BaseParser<TV;>;\n" +
                "    // declaration: org.parboiled.BaseParser<V>\n" +
                "    LOCALVARIABLE rule Ljava/lang/Object; L0 L3 1\n" +
                "    LOCALVARIABLE rule2 Ljava/lang/Object; L0 L3 2\n" +
                "    LOCALVARIABLE moreRules [Ljava/lang/Object; L0 L3 3\n" +
                "    MAXSTACK = 6\n" +
                "    MAXLOCALS = 4\n");
    }

}