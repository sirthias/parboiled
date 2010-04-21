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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
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
            new ActionClassGenerator(true)
    );


    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test(dependsOnGroups = "primary")
    public void testActionClassGeneration() throws Exception {
        RuleMethod method = processMethod("RuleWithComplexActionSetup", processors);

        assertEquals(method.getGroups().size(), 2);

        InstructionGroup group = method.getGroups().get(0);
        assertEqualsMultiline(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 17\n" +
                "public final class org/parboiled/transform/Action$HCsAlhftW7cYn1dT extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$0\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$1\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$0 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$1 : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$2 : I\n" +
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

        group = method.getGroups().get(1);
        assertEqualsMultiline(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 17\n" +
                "public final class org/parboiled/transform/Action$ZtTW8WICJeWWcGjq extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public Lorg/parboiled/transform/TestParser$$parboiled; field$0\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$1\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public Ljava/lang/String; field$2\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$3\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public I field$4\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$ZtTW8WICJeWWcGjq.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$ZtTW8WICJeWWcGjq.field$1 : I\n" +
                "    IADD\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$ZtTW8WICJeWWcGjq.field$2 : Ljava/lang/String;\n" +
                "    INVOKEVIRTUAL java/lang/String.length ()I\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$ZtTW8WICJeWWcGjq.field$3 : I\n" +
                "    ISUB\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$ZtTW8WICJeWWcGjq.field$4 : I\n" +
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

    @Test(dependsOnGroups = "primary", dependsOnMethods = "testActionClassGeneration")
    public void testActionClassGeneration2() throws Exception {
        RuleMethod method = processMethod("RuleWithIndirectExplicitDownAction", processors);

        assertEquals(method.getGroups().size(), 1);

        InstructionGroup group = method.getGroups().get(0);
        assertEqualsMultiline(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 17\n" +
                "public final class org/parboiled/transform/Action$4keN2urdHg0w1kKä extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public Lorg/parboiled/transform/TestParser$$parboiled; field$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseAction.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/BaseAction.UP3 (Lorg/parboiled/Context;)Lorg/parboiled/Context;\n" +
                "    ASTORE 1\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/BaseAction.DOWN2 (Lorg/parboiled/Context;)Lorg/parboiled/Context;\n" +
                "    ASTORE 1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$4keN2urdHg0w1kKä.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.integer : I\n" +
                "    IFGE L0\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$4keN2urdHg0w1kKä.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 1\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.action ()Z\n" +
                "    IFEQ L0\n" +
                "    ICONST_1\n" +
                "    GOTO L1\n" +
                "   L0\n" +
                "    ICONST_0\n" +
                "   L1\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/BaseAction.UP2 (Lorg/parboiled/Context;)Lorg/parboiled/Context;\n" +
                "    ASTORE 1\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/BaseAction.DOWN3 (Lorg/parboiled/Context;)Lorg/parboiled/Context;\n" +
                "    ASTORE 1\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

}