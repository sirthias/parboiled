/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import org.parboiled.common.ImmutableList;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;

public class ReturnInstructionUnifierTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier()
    );

    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        assertTraceDumpEquality(processMethod("RuleWithSwitchAndAction", processors), "" +
                "    ILOAD 1\n" +
                "    LOOKUPSWITCH\n" +
                "      0: L0\n" +
                "      default: L1\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    GETSTATIC org/parboiled/transform/TestParser.EMPTY : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    ICONST_1\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.push (Ljava/lang/Object;)Z\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    GOTO L2\n" +
                "   L1\n" +
                "    ACONST_NULL\n" +
                "   L2\n" +
                "    ARETURN\n");

        assertTraceDumpEquality(processMethod("RuleWith2Returns", processors), "" +
                "    ILOAD 1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IF_ICMPNE L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    INVOKESTATIC org/parboiled/transform/TestParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    GETSTATIC org/parboiled/transform/TestParser.EOI : Lorg/parboiled/Rule;\n" +
                "   L1\n" +
                "    ARETURN\n");

        assertTraceDumpEquality(processMethod("RuleWithDirectExplicitAction", processors), "" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    IFEQ L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IFLE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    INVOKESTATIC org/parboiled/transform/TestParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n");
    }

}