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
import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;

public class RuleMethodRewriterTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new CaptureClassGenerator(true),
            new ActionClassGenerator(true),
            new RuleMethodRewriter(),
            new VarFramingGenerator()
    );

    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test(dependsOnGroups = "primary")
    public void testRuleMethodRewriting() throws Exception {
        assertEqualsMultiline(getMethodInstructionList(processMethod("RuleWithIndirectImplicitAction", processors)), "" +
                "Method 'RuleWithIndirectImplicitAction':\n" +
                " 0     ALOAD 0\n" +
                " 1     BIPUSH 97\n" +
                " 2     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                " 3     BIPUSH 98\n" +
                " 4     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                " 5     ICONST_1\n" +
                " 6     ANEWARRAY java/lang/Object\n" +
                " 7     DUP\n" +
                " 8     ICONST_0\n" +
                " 9     NEW org/parboiled/transform/Action$aqänW6YöJ6yk7FhN\n" +
                "10     DUP\n" +
                "11     LDC \"RuleWithIndirectImplicitAction_Action1\"\n" +
                "12     INVOKESPECIAL org/parboiled/transform/Action$aqänW6YöJ6yk7FhN.<init> (Ljava/lang/String;)V\n" +
                "13     DUP\n" +
                "14     ALOAD 0\n" +
                "15     PUTFIELD org/parboiled/transform/Action$aqänW6YöJ6yk7FhN.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "16     AASTORE\n" +
                "17     INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "18     ARETURN\n");

        assertEqualsMultiline(getMethodInstructionList(processMethod("RuleWithComplexActionSetup", processors)), "" +
                "Method 'RuleWithComplexActionSetup':\n" +
                " 0     BIPUSH 26\n" +
                " 1     ISTORE 2\n" +
                " 2     BIPUSH 18\n" +
                " 3     ISTORE 3\n" +
                " 4     NEW org/parboiled/support/Var\n" +
                " 5     DUP\n" +
                " 6     LDC \"text\"\n" +
                " 7     INVOKESPECIAL org/parboiled/support/Var.<init> (Ljava/lang/Object;)V\n" +
                " 8     ASTORE 4\n" +
                " 9     ILOAD 2\n" +
                "10     ILOAD 1\n" +
                "11     IADD\n" +
                "12     ISTORE 2\n" +
                "13     ILOAD 3\n" +
                "14     ILOAD 2\n" +
                "15     ISUB\n" +
                "16     ISTORE 3\n" +
                "17     ALOAD 0\n" +
                "18     BIPUSH 97\n" +
                "19     ILOAD 2\n" +
                "20     IADD\n" +
                "21     INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "22     NEW org/parboiled/transform/Action$HCsAlhftW7cYn1dT\n" +
                "23     DUP\n" +
                "24     LDC \"RuleWithComplexActionSetup_Action1\"\n" +
                "25     INVOKESPECIAL org/parboiled/transform/Action$HCsAlhftW7cYn1dT.<init> (Ljava/lang/String;)V\n" +
                "26     DUP\n" +
                "27     ILOAD 2\n" +
                "28     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$0 : I\n" +
                "29     DUP\n" +
                "30     ILOAD 1\n" +
                "31     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$1 : I\n" +
                "32     DUP\n" +
                "33     ILOAD 3\n" +
                "34     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$2 : I\n" +
                "35     ICONST_2\n" +
                "36     ANEWARRAY java/lang/Object\n" +
                "37     DUP\n" +
                "38     ICONST_0\n" +
                "39     ALOAD 4\n" +
                "40     AASTORE\n" +
                "41     DUP\n" +
                "42     ICONST_1\n" +
                "43     NEW org/parboiled/transform/Action$ARäVEaFtWytNAZGB\n" +
                "44     DUP\n" +
                "45     LDC \"RuleWithComplexActionSetup_Action2\"\n" +
                "46     INVOKESPECIAL org/parboiled/transform/Action$ARäVEaFtWytNAZGB.<init> (Ljava/lang/String;)V\n" +
                "47     DUP\n" +
                "48     ALOAD 0\n" +
                "49     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "50     DUP\n" +
                "51     ILOAD 1\n" +
                "52     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$1 : I\n" +
                "53     DUP\n" +
                "54     ALOAD 4\n" +
                "55     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$2 : Lorg/parboiled/support/Var;\n" +
                "56     DUP\n" +
                "57     ILOAD 2\n" +
                "58     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$3 : I\n" +
                "59     DUP\n" +
                "60     ILOAD 3\n" +
                "61     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$4 : I\n" +
                "62     AASTORE\n" +
                "63     INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "64     NEW org/parboiled/matchers/VarFramingMatcher\n" +
                "65     DUP_X1\n" +
                "66     SWAP\n" +
                "67     BIPUSH 1\n" +
                "68     ANEWARRAY org/parboiled/support/Var\n" +
                "69     DUP\n" +
                "70     BIPUSH 0\n" +
                "71     ALOAD 4\n" +
                "72     DUP\n" +
                "73     LDC \"RuleWithComplexActionSetup:string\"\n" +
                "74     INVOKEVIRTUAL org/parboiled/support/Var.setName (Ljava/lang/String;)V\n" +
                "75     AASTORE\n" +
                "76     INVOKESPECIAL org/parboiled/matchers/VarFramingMatcher.<init> (Lorg/parboiled/Rule;[Lorg/parboiled/support/Var;)V\n" +
                "77     ARETURN\n");

        assertEqualsMultiline(getMethodInstructionList(processMethod("RuleWithCapture", processors)), "" +
                "Method 'RuleWithCapture':\n" +
                " 0     ALOAD 0\n" +
                " 1     BIPUSH 97\n" +
                " 2     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                " 3     BIPUSH 98\n" +
                " 4     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                " 5     ICONST_1\n" +
                " 6     ANEWARRAY java/lang/Object\n" +
                " 7     DUP\n" +
                " 8     ICONST_0\n" +
                " 9     ALOAD 0\n" +
                "10     NEW org/parboiled/transform/Capture$RRä3HOYSfqVyRda2\n" +
                "11     DUP\n" +
                "12     LDC \"RuleWithCapture_Capture1\"\n" +
                "13     INVOKESPECIAL org/parboiled/transform/Capture$RRä3HOYSfqVyRda2.<init> (Ljava/lang/String;)V\n" +
                "14     DUP\n" +
                "15     ALOAD 0\n" +
                "16     PUTFIELD org/parboiled/transform/Capture$RRä3HOYSfqVyRda2.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "17     DUP\n" +
                "18     ASTORE 1\n" +
                "19     INVOKEVIRTUAL org/parboiled/transform/TestParser.RuleWithCaptureParameter (Lorg/parboiled/Capture;)Lorg/parboiled/Rule;\n" +
                "20     DUP\n" +
                "21     ALOAD 1\n" +
                "22     SWAP\n" +
                "23     PUTFIELD org/parboiled/transform/Capture$RRä3HOYSfqVyRda2.contextRule : Lorg/parboiled/Rule;\n" +
                "24     AASTORE\n" +
                "25     INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "26     ARETURN\n");
    }

}