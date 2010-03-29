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

public class ImplicitActionsConverterTest extends TransformationTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void testReturnInstructionUnification() throws Exception {
        List<RuleMethodProcessor> processors = ImmutableList.of(
                new UnusedLabelsRemover(),
                new ReturnInstructionUnifier(),
                new InstructionGraphCreator(),
                new ImplicitActionsConverter()
        );

        assertTraceDumpEquality(processMethod("RuleWithIndirectImplicitAction", processors), "" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    IFNE L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    ICONST_5\n" +
                "    IF_ICMPNE L1\n" +
                "   L0\n" +
                "    ICONST_1\n" +
                "    GOTO L2\n" +
                "   L1\n" +
                "    ICONST_0\n" +
                "   L2\n" +
                "    INVOKESTATIC org/parboiled/BaseParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n");

        assertTraceDumpEquality(processMethod("RuleWithDirectImplicitAction", processors), "" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IFNE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    INVOKESTATIC org/parboiled/BaseParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    ICONST_2\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    AASTORE\n" +
                "    DUP\n" +
                "    ICONST_1\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n");

        assertTraceDumpEquality(processMethod("RuleWithDirectImplicitUpAction", processors), "" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.SET (Ljava/lang/Object;)Z\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    INVOKESTATIC org/parboiled/transform/TestParser.UP2 (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    INVOKESTATIC org/parboiled/BaseParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n");

        assertTraceDumpEquality(processMethod("RuleWithIndirectExplicitDownAction", processors), "" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IFGE L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    IFEQ L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    INVOKESTATIC org/parboiled/transform/TestParser.DOWN2 (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    INVOKESTATIC org/parboiled/transform/TestParser.UP3 (Ljava/lang/Object;)Ljava/lang/Object;\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    INVOKESTATIC org/parboiled/BaseParser.ACTION (Z)Lorg/parboiled/Action;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n");
    }

}