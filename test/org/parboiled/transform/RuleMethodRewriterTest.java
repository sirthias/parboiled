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
import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;

public class RuleMethodRewriterTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator(),
            new InstructionGroupPreparer(),
            new CaptureClassGenerator(),
            new ActionClassGenerator(),
            new RuleMethodRewriter()
    );

    @Test
    public void testActionClassGeneration() throws Exception {
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
                " 9     NEW org/parboiled/transform/Action$MoALzYkUjUT8mJäs\n" +
                "10     DUP\n" +
                "11     LDC \"RuleWithIndirectImplicitAction_Action1\"\n" +
                "12     INVOKESPECIAL org/parboiled/transform/Action$MoALzYkUjUT8mJäs.<init> (Ljava/lang/String;)V\n" +
                "13     DUP\n" +
                "14     ALOAD 0\n" +
                "15     PUTFIELD org/parboiled/transform/Action$MoALzYkUjUT8mJäs.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "16     AASTORE\n" +
                "17     INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "18     ARETURN\n");

        assertEqualsMultiline(getMethodInstructionList(processMethod("RuleWithComplexActionSetup", processors)), "" +
                "Method 'RuleWithComplexActionSetup':\n" +
                " 0     BIPUSH 26\n" +
                " 1     ISTORE 2\n" +
                " 2     BIPUSH 18\n" +
                " 3     ISTORE 3\n" +
                " 4     LDC \"text\"\n" +
                " 5     ASTORE 4\n" +
                " 6     ILOAD 2\n" +
                " 7     ILOAD 1\n" +
                " 8     IADD\n" +
                " 9     ISTORE 2\n" +
                "10     ILOAD 3\n" +
                "11     ILOAD 2\n" +
                "12     ISUB\n" +
                "13     ISTORE 3\n" +
                "14     ALOAD 0\n" +
                "15     BIPUSH 97\n" +
                "16     ILOAD 2\n" +
                "17     IADD\n" +
                "18     INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "19     NEW org/parboiled/transform/Action$bcdrqiT5Ofp84Pöy\n" +
                "20     DUP\n" +
                "21     LDC \"RuleWithComplexActionSetup_Action1\"\n" +
                "22     INVOKESPECIAL org/parboiled/transform/Action$bcdrqiT5Ofp84Pöy.<init> (Ljava/lang/String;)V\n" +
                "23     DUP\n" +
                "24     ILOAD 2\n" +
                "25     PUTFIELD org/parboiled/transform/Action$bcdrqiT5Ofp84Pöy.field$0 : I\n" +
                "26     DUP\n" +
                "27     ILOAD 1\n" +
                "28     PUTFIELD org/parboiled/transform/Action$bcdrqiT5Ofp84Pöy.field$1 : I\n" +
                "29     DUP\n" +
                "30     ILOAD 3\n" +
                "31     PUTFIELD org/parboiled/transform/Action$bcdrqiT5Ofp84Pöy.field$2 : I\n" +
                "32     ICONST_2\n" +
                "33     ANEWARRAY java/lang/Object\n" +
                "34     DUP\n" +
                "35     ICONST_0\n" +
                "36     ALOAD 4\n" +
                "37     AASTORE\n" +
                "38     DUP\n" +
                "39     ICONST_1\n" +
                "40     NEW org/parboiled/transform/Action$vLSRdXHTo6M2s2py\n" +
                "41     DUP\n" +
                "42     LDC \"RuleWithComplexActionSetup_Action2\"\n" +
                "43     INVOKESPECIAL org/parboiled/transform/Action$vLSRdXHTo6M2s2py.<init> (Ljava/lang/String;)V\n" +
                "44     DUP\n" +
                "45     ALOAD 0\n" +
                "46     PUTFIELD org/parboiled/transform/Action$vLSRdXHTo6M2s2py.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "47     DUP\n" +
                "48     ILOAD 1\n" +
                "49     PUTFIELD org/parboiled/transform/Action$vLSRdXHTo6M2s2py.field$1 : I\n" +
                "50     DUP\n" +
                "51     ALOAD 4\n" +
                "52     PUTFIELD org/parboiled/transform/Action$vLSRdXHTo6M2s2py.field$2 : Ljava/lang/String;\n" +
                "53     DUP\n" +
                "54     ILOAD 2\n" +
                "55     PUTFIELD org/parboiled/transform/Action$vLSRdXHTo6M2s2py.field$3 : I\n" +
                "56     DUP\n" +
                "57     ILOAD 3\n" +
                "58     PUTFIELD org/parboiled/transform/Action$vLSRdXHTo6M2s2py.field$4 : I\n" +
                "59     AASTORE\n" +
                "60     INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "61     ARETURN\n");

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
                "10     NEW org/parboiled/transform/Capture$äY9ptjsv0EazxIRZ\n" +
                "11     DUP\n" +
                "12     LDC \"RuleWithCapture_Capture1\"\n" +
                "13     INVOKESPECIAL org/parboiled/transform/Capture$äY9ptjsv0EazxIRZ.<init> (Ljava/lang/String;)V\n" +
                "14     DUP\n" +
                "15     ALOAD 0\n" +
                "16     PUTFIELD org/parboiled/transform/Capture$äY9ptjsv0EazxIRZ.field$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "17     DUP\n" +
                "18     ASTORE 1\n" +
                "19     INVOKEVIRTUAL org/parboiled/transform/TestParser.RuleWithCaptureParameter (Lorg/parboiled/Capture;)Lorg/parboiled/Rule;\n" +
                "20     DUP\n" +
                "21     ALOAD 1\n" +
                "22     SWAP\n" +
                "23     PUTFIELD org/parboiled/transform/Capture$äY9ptjsv0EazxIRZ.contextRule : Lorg/parboiled/Rule;\n" +
                "24     AASTORE\n" +
                "25     INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "26     ARETURN\n");
    }

}