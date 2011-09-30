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

import static org.parboiled.transform.AsmTestUtils.getClassDump;
import static org.testng.Assert.assertEquals;

public class ActionClassGeneratorTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new ActionClassGenerator(true),
            new VarInitClassGenerator(true)
    );


    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test(dependsOnGroups = "primary")
    public void testActionClassGeneration() throws Exception {
        RuleMethod method = processMethod("RuleWithComplexActionSetup", processors);

        assertEquals(method.getGroups().size(), 3);

        InstructionGroup group = method.getGroups().get(0);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/VarInit$ojjPlnt06r72YBBm extends org/parboiled/transform/BaseVarInit  {\n" +
                "\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseVarInit.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public create()Ljava/lang/Object;\n" +
                "    LDC \"text\"\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 1\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(1);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/Action$LmzJHalG7AngCUsX extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$0\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$1\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$2\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$LmzJHalG7AngCUsX.field$0 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$LmzJHalG7AngCUsX.field$1 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$LmzJHalG7AngCUsX.field$2 : I\n" +
                "    IADD\n" +
                "    IF_ICMPLE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        group = method.getGroups().get(2);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/Action$OrG2zjbz0MYoT8sO extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public Lorg/parboiled/transform/TestParser$$parboiled; field$0\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$1\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public Lorg/parboiled/support/Var; field$2\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$3\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public I field$4\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 0x1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OrG2zjbz0MYoT8sO.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OrG2zjbz0MYoT8sO.field$1 : I\n" +
                "    IADD\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OrG2zjbz0MYoT8sO.field$2 : Lorg/parboiled/support/Var;\n" +
                "    INVOKEVIRTUAL org/parboiled/support/Var.get ()Ljava/lang/Object;\n" +
                "    CHECKCAST java/lang/String\n" +
                "    INVOKEVIRTUAL java/lang/String.length ()I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OrG2zjbz0MYoT8sO.field$3 : I\n" +
                "    ISUB\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OrG2zjbz0MYoT8sO.field$4 : I\n" +
                "    ISUB\n" +
                "    IF_ICMPGE L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

}