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

import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;
import static org.testng.Assert.assertEquals;

public class RuleMethodRewriterTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new ActionClassGenerator(true),
            new VarInitClassGenerator(true),
            new RuleMethodRewriter(),
            new VarFramingGenerator()
    );

    @BeforeClass
    public void setup() throws IOException {
        setup(TestParser.class);
    }

    @Test(dependsOnGroups = "primary")
    public void testRuleMethodRewriting() throws Exception {
        assertEquals(getMethodInstructionList(processMethod("RuleWithIndirectImplicitAction", processors)), "" +
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

        assertEquals(getMethodInstructionList(processMethod("RuleWithComplexActionSetup", processors)), "" +
                "Method 'RuleWithComplexActionSetup':\n" +
                " 0     BIPUSH 26\n" +
                " 1     ISTORE 2\n" +
                " 2     BIPUSH 18\n" +
                " 3     ISTORE 3\n" +
                " 4     NEW org/parboiled/support/Var\n" +
                " 5     DUP\n" +
                " 6     NEW org/parboiled/transform/VarInit$ojjPlntz5r61YBBm\n" +
                " 7     DUP\n" +
                " 8     LDC \"RuleWithComplexActionSetup_VarInit1\"\n" +
                " 9     INVOKESPECIAL org/parboiled/transform/VarInit$ojjPlntz5r61YBBm.<init> (Ljava/lang/String;)V\n" +
                "10     INVOKESPECIAL org/parboiled/support/Var.<init> (Lorg/parboiled/common/Factory;)V\n" +
                "11     ASTORE 4\n" +
                "12     ILOAD 2\n" +
                "13     ILOAD 1\n" +
                "14     IADD\n" +
                "15     ISTORE 2\n" +
                "16     ILOAD 3\n" +
                "17     ILOAD 2\n" +
                "18     ISUB\n" +
                "19     ISTORE 3\n" +
                "20     ALOAD 0\n" +
                "21     BIPUSH 97\n" +
                "22     ILOAD 2\n" +
                "23     IADD\n" +
                "24     INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "25     NEW org/parboiled/transform/Action$HCsAlhftW7cYn1dT\n" +
                "26     DUP\n" +
                "27     LDC \"RuleWithComplexActionSetup_Action1\"\n" +
                "28     INVOKESPECIAL org/parboiled/transform/Action$HCsAlhftW7cYn1dT.<init> (Ljava/lang/String;)V\n" +
                "29     DUP\n" +
                "30     ILOAD 2\n" +
                "31     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$0 : I\n" +
                "32     DUP\n" +
                "33     ILOAD 1\n" +
                "34     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$1 : I\n" +
                "35     DUP\n" +
                "36     ILOAD 3\n" +
                "37     PUTFIELD org/parboiled/transform/Action$HCsAlhftW7cYn1dT.field$2 : I\n" +
                "38     ICONST_2\n" +
                "39     ANEWARRAY java/lang/Object\n" +
                "40     DUP\n" +
                "41     ICONST_0\n" +
                "42     ALOAD 4\n" +
                "43     AASTORE\n" +
                "44     DUP\n" +
                "45     ICONST_1\n" +
                "46     NEW org/parboiled/transform/Action$ARäVEaFtWytNAZGB\n" +
                "47     DUP\n" +
                "48     LDC \"RuleWithComplexActionSetup_Action2\"\n" +
                "49     INVOKESPECIAL org/parboiled/transform/Action$ARäVEaFtWytNAZGB.<init> (Ljava/lang/String;)V\n" +
                "50     DUP\n" +
                "51     ALOAD 0\n" +
                "52     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "53     DUP\n" +
                "54     ILOAD 1\n" +
                "55     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$1 : I\n" +
                "56     DUP\n" +
                "57     ALOAD 4\n" +
                "58     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$2 : Lorg/parboiled/support/Var;\n" +
                "59     DUP\n" +
                "60     ILOAD 2\n" +
                "61     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$3 : I\n" +
                "62     DUP\n" +
                "63     ILOAD 3\n" +
                "64     PUTFIELD org/parboiled/transform/Action$ARäVEaFtWytNAZGB.field$4 : I\n" +
                "65     AASTORE\n" +
                "66     INVOKEVIRTUAL org/parboiled/transform/TestParser.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "67     NEW org/parboiled/matchers/VarFramingMatcher\n" +
                "68     DUP_X1\n" +
                "69     SWAP\n" +
                "70     BIPUSH 1\n" +
                "71     ANEWARRAY org/parboiled/support/Var\n" +
                "72     DUP\n" +
                "73     BIPUSH 0\n" +
                "74     ALOAD 4\n" +
                "75     DUP\n" +
                "76     LDC \"RuleWithComplexActionSetup:string\"\n" +
                "77     INVOKEVIRTUAL org/parboiled/support/Var.setName (Ljava/lang/String;)V\n" +
                "78     AASTORE\n" +
                "79     INVOKESPECIAL org/parboiled/matchers/VarFramingMatcher.<init> (Lorg/parboiled/Rule;[Lorg/parboiled/support/Var;)V\n" +
                "80     ARETURN\n");
    }

}