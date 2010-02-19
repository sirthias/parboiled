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

import static org.parboiled.transform.AsmTestUtils.getClassDump;
import static org.parboiled.transform.AsmUtils.getMethodByName;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import com.google.common.base.Preconditions;

public class ActionClassGeneratorTest {

    private final ParserClassNode classNode = new ParserClassNode(TestParser.class);
    private final List<ParserMethod> ruleMethods = classNode.ruleMethods;

    @BeforeClass
    private void setup() throws Exception {
        new ClassNodeInitializer(
                new MethodCategorizer(
                        new RuleMethodAnalyzer(
                                new RuleMethodInstructionGraphPartitioner(null)
                        )
                )
        ).transform(classNode);
    }

    @Test
    public void testActionClassGeneration() throws Exception {
        testActionClassGeneration("simpleActionRule", 0, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1 org/parboiled/transform/TestParser$$parboiled simpleActionRule_Action1\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.actions : Lorg/parboiled/transform/SimpleActions;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.LAST_CHAR ()Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL java/lang/Character.charValue ()C\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/SimpleActions.testX (C)Z\n" +
                "    IRETURN\n" +
                "   L1\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$simpleActionRule_Action1; L0 L1 0\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        testActionClassGeneration("upSetActionRule", 0, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1 org/parboiled/transform/TestParser$$parboiled upSetActionRule_Action1\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.UP ()V\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.UP ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.actions : Lorg/parboiled/transform/SimpleActions;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/SimpleActions.return5 ()I\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.SET (Ljava/lang/Object;)Z\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.DOWN ()V\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.DOWN ()V\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    IRETURN\n" +
                "   L1\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$upSetActionRule_Action1; L0 L1 0\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        testActionClassGeneration("booleanExpressionActionRule", 0, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1 org/parboiled/transform/TestParser$$parboiled booleanExpressionActionRule_Action1\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.IN_PREDICATE ()Z\n" +
                "    IFNE L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.LAST_CHAR ()Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL java/lang/Character.charValue ()C\n" +
                "    BIPUSH 98\n" +
                "    IF_ICMPNE L1\n" +
                "    ICONST_1\n" +
                "    GOTO L2\n" +
                "   L1\n" +
                "    ICONST_0\n" +
                "   L2\n" +
                "    IRETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$booleanExpressionActionRule_Action1; L0 L3 0\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        testActionClassGeneration("complexActionsRule", 0, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1 org/parboiled/transform/TestParser$$parboiled complexActionsRule_Action1\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.UP ()V\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.DOWN ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.contextActions : Lorg/parboiled/transform/ContextActions;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    LDC \"b\"\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.NODE (Ljava/lang/String;)Lorg/parboiled/Node;\n" +
                "    INVOKEINTERFACE org/parboiled/Node.getEndLocation ()Lorg/parboiled/support/InputLocation;\n" +
                "    GETFIELD org/parboiled/support/InputLocation.row : I\n" +
                "    BIPUSH 26\n" +
                "    IADD\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ContextActions.action2 (I)Z\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.UP ()V\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.DOWN ()V\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    IRETURN\n" +
                "   L1\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action1; L0 L1 0\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        testActionClassGeneration("complexActionsRule", 1, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2 org/parboiled/transform/TestParser$$parboiled complexActionsRule_Action2\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.UP ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    GETFIELD org/parboiled/transform/TestParser.actions : Lorg/parboiled/transform/SimpleActions;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/SimpleActions.return5 ()I\n" +
                "    INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.SET (Ljava/lang/Object;)Z\n" +
                "    INVOKESTATIC java/lang/Boolean.valueOf (Z)Ljava/lang/Boolean;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/ActionWrapperBase.DOWN ()V\n" +
                "    INVOKEVIRTUAL java/lang/Boolean.booleanValue ()Z\n" +
                "    IRETURN\n" +
                "   L1\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action2; L0 L1 0\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");

        testActionClassGeneration("complexActionsRule", 2, "" +
                "// class version 49.0 (49)\n" +
                "// access flags 33\n" +
                "public class org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3 extends org/parboiled/transform/ActionWrapperBase  {\n" +
                "\n" +
                "  // access flags 2\n" +
                "  private INNERCLASS org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3 org/parboiled/transform/TestParser$$parboiled complexActionsRule_Action3\n" +
                "\n" +
                "  // access flags 4112\n" +
                "  final Lorg/parboiled/transform/TestParser$$parboiled; this$0\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public <init>(Lorg/parboiled/transform/TestParser$$parboiled;)V\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/transform/ActionWrapperBase.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 2\n" +
                "    MAXLOCALS = 2\n" +
                "\n" +
                "  // access flags 1\n" +
                "  public run(Lorg/parboiled/Context;)Z\n" +
                "   L0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 1\n" +
                "    PUTFIELD org/parboiled/transform/ActionWrapperBase.context : Lorg/parboiled/Context;\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3.this$0 : Lorg/parboiled/transform/TestParser$$parboiled;\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3.context : Lorg/parboiled/Context;\n" +
                "    INVOKEINTERFACE org/parboiled/ContextAware.setContext (Lorg/parboiled/Context;)V\n" +
                "    INVOKEVIRTUAL org/parboiled/transform/TestParser.SET ()Z\n" +
                "    IRETURN\n" +
                "   L1\n" +
                "    LOCALVARIABLE this Lorg/parboiled/transform/TestParser$$parboiled$complexActionsRule_Action3; L0 L1 0\n" +
                "    MAXSTACK = 3\n" +
                "    MAXLOCALS = 2\n" +
                "}\n");
    }

    @SuppressWarnings({"ConstantConditions"})
    private void testActionClassGeneration(String methodName, int actionNr, String expectedTrace) throws Exception {
        ParserMethod info = getMethodByName(ruleMethods, methodName);
        Preconditions.checkState(info != null, "Method '" + methodName + "' not found");

        int actionNumber = 0;
        for (InstructionSubSet subSet : info.getInstructionSubSets()) {
            if (subSet.isActionSet && actionNumber++ == actionNr) {
                ActionClassGenerator generator = new ActionClassGenerator(classNode, info, subSet, actionNumber);
                generator.defineActionClass();
                assertEqualsMultiline(getClassDump(generator.generateActionClassCode()), expectedTrace);
                return;
            }
        }
    }

}
