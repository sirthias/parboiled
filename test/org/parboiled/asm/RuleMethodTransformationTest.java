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

package org.parboiled.asm;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckMethodAdapter;
import org.objectweb.asm.util.TraceMethodVisitor;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RuleMethodTransformationTest {

    private final ParserClassNode classNode = new ParserClassNode(TestParser.class);

    @BeforeClass
    private void setup() throws Exception {
        new ClassNodeInitializer(
                new DontExtendMethodRemover(
                        new RuleMethodAnalyzer(
                                new RuleMethodInstructionGraphPartitioner(
                                        new RuleMethodTransformer(
                                                new ParserClassFinalizer(null)
                                        )
                                )
                        )
                )
        ).transform(classNode);
    }

    @Test
    public void testTransformRuleMethods() throws Exception {
        testRuleMethodTransformation("<init>", "" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser.<init> ()V\n" +
                "    RETURN\n" +
                "    MAXSTACK = 0\n" +
                "    MAXLOCALS = 0\n");

        testRuleMethodTransformation("noActionRule", "" +
                "   L0\n" +
                "    LINENUMBER 52 L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$noActionRule : Lorg/parboiled/Rule;\n" +
                "    IFNONNULL L1\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$noActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser.noActionRule ()Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"noActionRule\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$noActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$noActionRule : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/asm/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 7\n" +
                "    MAXLOCALS = 1\n");

        testRuleMethodTransformation("simpleActionRule", "" +
                "   L0\n" +
                "    LINENUMBER 64 L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$simpleActionRule : Lorg/parboiled/Rule;\n" +
                "    IFNONNULL L1\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$simpleActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$simpleActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$simpleActionRule_Action1.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"simpleActionRule\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$simpleActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$simpleActionRule : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/asm/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 4\n" +
                "    MAXLOCALS = 1\n");

        testRuleMethodTransformation("upSetActionRule", "" +
                "   L0\n" +
                "    LINENUMBER 71 L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$upSetActionRule : Lorg/parboiled/Rule;\n" +
                "    IFNONNULL L1\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$upSetActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 98\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$upSetActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$upSetActionRule_Action1.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"upSetActionRule\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$upSetActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$upSetActionRule : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/asm/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 11\n" +
                "    MAXLOCALS = 1\n");

        testRuleMethodTransformation("booleanExpressionActionRule", "" +
                "   L0\n" +
                "    LINENUMBER 84 L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$booleanExpressionActionRule : Lorg/parboiled/Rule;\n" +
                "    IFNONNULL L1\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$booleanExpressionActionRule : Lorg/parboiled/Rule;\n" +
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
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_0\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$booleanExpressionActionRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$booleanExpressionActionRule_Action1.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"booleanExpressionActionRule\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$booleanExpressionActionRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$booleanExpressionActionRule : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/asm/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 8\n" +
                "    MAXLOCALS = 1\n");

        testRuleMethodTransformation("complexActionsRule", "" +
                "   L0\n" +
                "    LINENUMBER 97 L0\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._enterRuleDef ()V\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$complexActionsRule : Lorg/parboiled/Rule;\n" +
                "    IFNONNULL L1\n" +
                "    ALOAD 0\n" +
                "    DUP\n" +
                "    NEW org/parboiled/matchers/ProxyMatcher\n" +
                "    DUP_X1\n" +
                "    DUP\n" +
                "    INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$complexActionsRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 97\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 98\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.ch (C)Lorg/parboiled/Rule;\n" +
                "    LDC \"b\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    BIPUSH 99\n" +
                "    INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.optional (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_2\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action1\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action1.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    DUP\n" +
                "    ICONST_1\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action2\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action2.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    LDC \"specialSeq\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.oneOrMore (Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    ICONST_1\n" +
                "    ANEWARRAY java/lang/Object\n" +
                "    DUP\n" +
                "    ICONST_0\n" +
                "    NEW org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action3\n" +
                "    DUP\n" +
                "    ALOAD 0\n" +
                "    INVOKESPECIAL org/parboiled/asm/TestParser$$parboiled$complexActionsRule_Action3.<init> (Lorg/parboiled/asm/TestParser$$parboiled;)V\n" +
                "    AASTORE\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser.sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "    DUP\n" +
                "    INSTANCEOF org/parboiled/matchers/AbstractMatcher\n" +
                "    IFEQ L2\n" +
                "    CHECKCAST org/parboiled/matchers/AbstractMatcher\n" +
                "    DUP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.isLocked ()Z\n" +
                "    IFNE L2\n" +
                "    DUP\n" +
                "    LDC \"complexActionsRule\"\n" +
                "    INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "    SWAP\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/AbstractMatcher.lock ()V\n" +
                "   L2\n" +
                "    DUP_X1\n" +
                "    CHECKCAST org/parboiled/matchers/Matcher\n" +
                "    INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)Lorg/parboiled/matchers/ProxyMatcher;\n" +
                "    POP\n" +
                "    PUTFIELD org/parboiled/asm/TestParser$$parboiled.cache$complexActionsRule : Lorg/parboiled/Rule;\n" +
                "    ALOAD 0\n" +
                "    INVOKEVIRTUAL org/parboiled/asm/TestParser$$parboiled._exitRuleDef ()V\n" +
                "   L1\n" +
                "    ALOAD 0\n" +
                "    GETFIELD org/parboiled/asm/TestParser$$parboiled.cache$complexActionsRule : Lorg/parboiled/Rule;\n" +
                "    ARETURN\n" +
                "   L3\n" +
                "    LOCALVARIABLE this Lorg/parboiled/asm/TestParser; L0 L3 0\n" +
                "    MAXSTACK = 14\n" +
                "    MAXLOCALS = 1\n");

    }

    private void testRuleMethodTransformation(String methodName, String traceDump) throws Exception {
        for (Object methodObj : classNode.methods) {
            MethodNode method = (MethodNode) methodObj;
            if (methodName.equals(method.name)) {
                TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor();
                // MethodAdapter checkMethodAdapter = new MethodAdapter(traceMethodVisitor);
                MethodAdapter checkMethodAdapter = new CheckMethodAdapter(traceMethodVisitor);
                method.accept(checkMethodAdapter);
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                traceMethodVisitor.print(printWriter);
                printWriter.flush();

                assertEqualsMultiline(stringWriter.toString(), traceDump);
                return;
            }
        }

        fail("Method '" + methodName + "' not found on classNode");
    }
}
