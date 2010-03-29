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

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.transform.AsmTestUtils.getClassDump;
import static org.testng.Assert.assertEquals;

public class CaptureClassGeneratorTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new CaptureClassGenerator(true)
    );

    @Test(dependsOnGroups = "primary")
    public void testCaptureClassGeneration() throws Exception {
        RuleMethod method = processMethod("RuleWithCapture", processors);

        assertEquals(method.getGroups().size(), 1);

        InstructionGroup group = method.getGroups().get(0);
        assertEqualsMultiline(getClassDump(group.getGroupClassCode()), "" +
                "// class version 49.0 (49)\n" +
                "// access flags 17\n" +
                "public final class org/parboiled/transform/Capture$1MäBfU3öEmOr2YDR extends org/parboiled/transform/BaseCapture  {\n" +
                "\n" +
                "\n" +
                "  // access flags 4097\n" +
                "  public Lorg/parboiled/transform/TestParser$$parboiled; field$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Ljava/lang/String;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    INVOKESPECIAL org/parboiled/transform/BaseCapture.<init> (Ljava/lang/String;)V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public get()Ljava/lang/Object;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/BaseCapture.checkContext ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Capture$1MäBfU3öEmOr2YDR.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/Capture$1MäBfU3öEmOr2YDR.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    LDC \"a\"\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.TEXT (Ljava/lang/String;)Ljava/lang/String;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 1\n" +
                "}\n");
    }

}