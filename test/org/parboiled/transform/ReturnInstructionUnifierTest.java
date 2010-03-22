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

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.List;

import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;

public class ReturnInstructionUnifierTest extends TransformationTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        List<RuleMethodProcessor> processors = ImmutableList.of(
                new LineNumberRemover(),
                new ReturnInstructionUnifier()
        );

        assertTraceDumpEquality(processMethod("RuleWith2Returns", processors), "" +
                "   L0\n" +
                "    ILOAD 1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IF_ICMPNE L1\n" +
                "   L2\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    GOTO L3\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.eoi ()Lorg/parboiled/Rule;\n" +
                "   L3\n" +
                "    ARETURN\n" +
                "   L4\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser; L0 L4 0\n" +
                "    LOCALVARIABLE param I L0 L4 1\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n");

        assertTraceDumpEquality(processMethod("RuleWithDirectImplicitAction", processors), "" +
                "   L0\n" +
                "    LINENUMBER 53 L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IFNE L1\n" +
                "    ICONST_1\n" +
                "    GOTO L2\n" +
                "   L1\n" +
                "    ICONST_0\n" +
                "   L2\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 7\n" +
                "    MAXLOCALS = 1\n");
    }

}