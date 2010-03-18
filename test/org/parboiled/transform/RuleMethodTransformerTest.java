/*
 * Copyright (C) 2009 Mathias Doenitz
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

import org.testng.annotations.Test;

import static org.parboiled.transform.AsmTestUtils.assertTraceDumpEquality;
import static org.parboiled.transform.AsmUtils.getMethodByName;

public class RuleMethodTransformerTest {

    @Test
    public void testRuleMethodTransformation() throws Exception {
        ParserClassNode classNode = new ParserClassNode(TestParser.class);
        /*new ClassNodeInitializer(
                new MethodCategorizer(
                        new LineNumberRemover(
                                new InstructionGraphCreator(
                                        new InstructionGraphPartitioner(
                                                new RuleMethodRewriter(
                                                        new WithCallToSuperReplacer(null)
                                                )
                                        )
                                )
                        )
                )
        ).transform(classNode);*/

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "noActionRule"), "" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser.noActionRule ()Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "    MAXSTACK = 7\n" +
                "    MAXLOCALS = 1\n");

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "simpleActionRule"), "" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 1\n");

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "upSetActionRule"), "" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    MAXSTACK = 11\n" +
                "    MAXLOCALS = 1\n");

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "booleanExpressionActionRule"), "" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    MAXSTACK = 8\n" +
                "    MAXLOCALS = 1\n");

        assertTraceDumpEquality(getMethodByName(classNode.ruleMethods, "complexActionsRule"), "" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 98\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.ch (C)Lorg/parboiled/Rule;\n" +
                "    LDC \"b\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_2\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    DUP\n" +
                "    ICONST_1\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    LDC \"specialSeq\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3.<init> (Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L1\n" +
                "    MAXSTACK = 14\n" +
                "    MAXLOCALS = 1\n");

    }

}
