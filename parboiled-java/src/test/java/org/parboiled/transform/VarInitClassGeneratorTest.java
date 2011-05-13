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

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.common.ImmutableList;
import org.parboiled.support.Var;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.parboiled.transform.AsmTestUtils.getClassDump;
import static org.testng.Assert.assertEquals;

public class VarInitClassGeneratorTest extends TransformationTest {

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

    static class Parser extends BaseParser<Integer> {

        @SuppressWarnings({"UnusedDeclaration"})
        public Rule A() {
            Var<List<String>> list = new Var<List<String>>(new ArrayList<String>());
            Var<Integer> i = new Var<Integer>(26);
            return Sequence('a', list.get().add(match()));
        }

    }

    @BeforeClass
    public void setup() throws IOException {
        setup(Parser.class);
    }

    @Test
    public void testVarInitClassGeneration() throws Exception {
        RuleMethod method = processMethod("A", processors);

        assertEquals(method.getGroups().size(), 3);

        InstructionGroup group = method.getGroups().get(0);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/VarInit$eYqwb95äYKb16FsS extends org/parboiled/transform/BaseVarInit  {\n" +
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
                "    NEW java/util/ArrayList\n" +
                "    DUP\n" +
                "    INVOKESPECIAL java/util/ArrayList.<init> ()V\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(1);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/VarInit$L6SMqNxExCwCkL8F extends org/parboiled/transform/BaseVarInit  {\n" +
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
                "    BIPUSH 26\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 1\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");

        group = method.getGroups().get(2);
        assertEquals(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 0x1011\n" +
                "public final class org/parboiled/transform/Action$OLan4U0W0uCiimxr extends org/parboiled/transform/BaseAction  {\n" +
                "\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public Lorg/parboiled/support/Var; field$0\n" +
                "\n" +
                "  // access flags 0x1001\n" +
                "  public Lorg/parboiled/transform/VarInitClassGeneratorTest$Parser$$parboiled; field$1\n" +
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
                "    GETFIELD org/parboiled/transform/Action$OLan4U0W0uCiimxr.field$0 : Lorg/parboiled/support/Var;\n" +
                "    INVOKEVIRTUAL org/parboiled/support/Var.get ()Ljava/lang/Object;\n" +
                "    CHECKCAST java/util/List\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Action$OLan4U0W0uCiimxr.field$1 : Lorg/parboiled/transform/VarInitClassGeneratorTest$Parser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 1\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/VarInitClassGeneratorTest$Parser.match ()Ljava/lang/String;\n" +
                "    INVOKEINTERFACE java/util/List.add (Ljava/lang/Object;)Z\n" +
                "    IRETURN\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

}