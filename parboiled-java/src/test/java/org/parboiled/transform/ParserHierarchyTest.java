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
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.MemoMismatches;
import org.testng.annotations.Test;

import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;
import static org.parboiled.transform.AsmTestUtils.verifyIntegrity;
import static org.testng.Assert.assertEquals;

public class ParserHierarchyTest {

    static class Parser1 extends BaseParser<Object> {
        Rule A() {
            return EMPTY;
        }
        Rule B() {
            return Sequence('B', dup());
        }
        Rule C() {
            return ANY;
        }
        @MemoMismatches
        Rule E() {
            return EMPTY;
        }
    }

    @BuildParseTree
    static class Parser2 extends Parser1 {
        @Override Rule A() {
            return Sequence(super.A(), 'A');
        }
        @Override Rule C() {
            return Sequence(super.C(), dup());
        }
    }

    static class Parser3 extends Parser2 {
        @Override Rule B() {
            return Sequence(super.B(), 'B', dup());
        }
        @Override Rule C() {
            return Sequence('C', super.C());
        }
        @SuppressWarnings( {"UnusedDeclaration"})
        Rule D() {
            return Sequence(super.A(), super.B(), B(), dup());
        }
    }

    static class Parser4 extends Parser1 {
        @Override Rule E() {
            return super.E();
        }
    }

    @Test
    public void verifyTestParserHierarchyExtension() throws Exception {
        ParserClassNode classNode = ParserTransformer.extendParserClass(Parser3.class);
        verifyIntegrity(classNode.name, classNode.getClassCode());

        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("$A()Lorg/parboiled/Rule;")), "" +
                "Method '$A':\n" +
                " 0     GETSTATIC org/parboiled/transform/ParserHierarchyTest$Parser1.EMPTY : Lorg/parboiled/Rule;\n" +
                " 1     ARETURN\n");
        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("A()Lorg/parboiled/Rule;")), "" +
                "Method 'A':\n" +
                " 0     ALOAD 0\n" +
                " 1     GETFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$A : Lorg/parboiled/Rule;\n" +
                " 2     DUP\n" +
                " 3     IFNULL L0\n" +
                " 4     ARETURN\n" +
                " 5    L0\n" +
                " 6     POP\n" +
                " 7     NEW org/parboiled/matchers/ProxyMatcher\n" +
                " 8     DUP\n" +
                " 9     INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "10     DUP\n" +
                "11     ALOAD 0\n" +
                "12     SWAP\n" +
                "13     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$A : Lorg/parboiled/Rule;\n" +
                "14     ALOAD 0\n" +
                "15     ALOAD 0\n" +
                "16     INVOKESPECIAL org/parboiled/transform/ParserHierarchyTest$Parser1.A ()Lorg/parboiled/Rule;\n" +
                "17     BIPUSH 65\n" +
                "18     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "19     ICONST_0\n" +
                "20     ANEWARRAY java/lang/Object\n" +
                "21     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser2.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "22     DUP\n" +
                "23     IFNULL L1\n" +
                "24     LDC \"A\"\n" +
                "25     INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "26    L1\n" +
                "27     DUP_X1\n" +
                "28     CHECKCAST org/parboiled/matchers/Matcher\n" +
                "29     INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "30     DUP\n" +
                "31     ALOAD 0\n" +
                "32     SWAP\n" +
                "33     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$A : Lorg/parboiled/Rule;\n" +
                "34     ARETURN\n");
        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("$B()Lorg/parboiled/Rule;")), "" +
                "Method '$B':\n" +
                " 0     ALOAD 0\n" +
                " 1     BIPUSH 66\n" +
                " 2     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                " 3     NEW org/parboiled/transform/Action$Px2Jp4FIYS9AjKV7\n" +
                " 4     DUP\n" +
                " 5     LDC \"$B_Action1\"\n" +
                " 6     INVOKESPECIAL org/parboiled/transform/Action$Px2Jp4FIYS9AjKV7.<init> (Ljava/lang/String;)V\n" +
                " 7     DUP\n" +
                " 8     ALOAD 0\n" +
                " 9     PUTFIELD org/parboiled/transform/Action$Px2Jp4FIYS9AjKV7.field$0 : Lorg/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled;\n" +
                "10     ICONST_0\n" +
                "11     ANEWARRAY java/lang/Object\n" +
                "12     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser1.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "13     ARETURN\n");
        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("B()Lorg/parboiled/Rule;")), "" +
                "Method 'B':\n" +
                " 0     ALOAD 0\n" +
                " 1     GETFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$B : Lorg/parboiled/Rule;\n" +
                " 2     DUP\n" +
                " 3     IFNULL L0\n" +
                " 4     ARETURN\n" +
                " 5    L0\n" +
                " 6     POP\n" +
                " 7     NEW org/parboiled/matchers/ProxyMatcher\n" +
                " 8     DUP\n" +
                " 9     INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "10     DUP\n" +
                "11     ALOAD 0\n" +
                "12     SWAP\n" +
                "13     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$B : Lorg/parboiled/Rule;\n" +
                "14     ALOAD 0\n" +
                "15     ALOAD 0\n" +
                "16     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.$B ()Lorg/parboiled/Rule;\n" +
                "17     BIPUSH 66\n" +
                "18     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "19     ICONST_1\n" +
                "20     ANEWARRAY java/lang/Object\n" +
                "21     DUP\n" +
                "22     ICONST_0\n" +
                "23     NEW org/parboiled/transform/Action$k4qTyX8Zgn8Lm8a0\n" +
                "24     DUP\n" +
                "25     LDC \"B_Action1\"\n" +
                "26     INVOKESPECIAL org/parboiled/transform/Action$k4qTyX8Zgn8Lm8a0.<init> (Ljava/lang/String;)V\n" +
                "27     DUP\n" +
                "28     ALOAD 0\n" +
                "29     PUTFIELD org/parboiled/transform/Action$k4qTyX8Zgn8Lm8a0.field$0 : Lorg/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled;\n" +
                "30     AASTORE\n" +
                "31     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser3.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "32     DUP\n" +
                "33     IFNULL L1\n" +
                "34     LDC \"B\"\n" +
                "35     INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "36    L1\n" +
                "37     DUP_X1\n" +
                "38     CHECKCAST org/parboiled/matchers/Matcher\n" +
                "39     INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "40     DUP\n" +
                "41     ALOAD 0\n" +
                "42     SWAP\n" +
                "43     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$B : Lorg/parboiled/Rule;\n" +
                "44     ARETURN\n");
        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("$C()Lorg/parboiled/Rule;")), "" +
                "Method '$C':\n" +
                " 0     ALOAD 0\n" +
                " 1     ALOAD 0\n" +
                " 2     INVOKESPECIAL org/parboiled/transform/ParserHierarchyTest$Parser1.C ()Lorg/parboiled/Rule;\n" +
                " 3     NEW org/parboiled/transform/Action$zJfzDznnLMaJTxvg\n" +
                " 4     DUP\n" +
                " 5     LDC \"$C_Action1\"\n" +
                " 6     INVOKESPECIAL org/parboiled/transform/Action$zJfzDznnLMaJTxvg.<init> (Ljava/lang/String;)V\n" +
                " 7     DUP\n" +
                " 8     ALOAD 0\n" +
                " 9     PUTFIELD org/parboiled/transform/Action$zJfzDznnLMaJTxvg.field$0 : Lorg/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled;\n" +
                "10     ICONST_0\n" +
                "11     ANEWARRAY java/lang/Object\n" +
                "12     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser2.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "13     ARETURN\n");
        assertEquals(getMethodInstructionList(classNode.getRuleMethods().get("C()Lorg/parboiled/Rule;")), "" +
                "Method 'C':\n" +
                " 0     ALOAD 0\n" +
                " 1     GETFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$C : Lorg/parboiled/Rule;\n" +
                " 2     DUP\n" +
                " 3     IFNULL L0\n" +
                " 4     ARETURN\n" +
                " 5    L0\n" +
                " 6     POP\n" +
                " 7     NEW org/parboiled/matchers/ProxyMatcher\n" +
                " 8     DUP\n" +
                " 9     INVOKESPECIAL org/parboiled/matchers/ProxyMatcher.<init> ()V\n" +
                "10     DUP\n" +
                "11     ALOAD 0\n" +
                "12     SWAP\n" +
                "13     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$C : Lorg/parboiled/Rule;\n" +
                "14     ALOAD 0\n" +
                "15     BIPUSH 67\n" +
                "16     INVOKESTATIC java/lang/Character.valueOf (C)Ljava/lang/Character;\n" +
                "17     ALOAD 0\n" +
                "18     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.$C ()Lorg/parboiled/Rule;\n" +
                "19     ICONST_0\n" +
                "20     ANEWARRAY java/lang/Object\n" +
                "21     INVOKEVIRTUAL org/parboiled/transform/ParserHierarchyTest$Parser3.Sequence (Ljava/lang/Object;Ljava/lang/Object;[Ljava/lang/Object;)Lorg/parboiled/Rule;\n" +
                "22     DUP\n" +
                "23     IFNULL L1\n" +
                "24     LDC \"C\"\n" +
                "25     INVOKEINTERFACE org/parboiled/Rule.label (Ljava/lang/String;)Lorg/parboiled/Rule;\n" +
                "26    L1\n" +
                "27     DUP_X1\n" +
                "28     CHECKCAST org/parboiled/matchers/Matcher\n" +
                "29     INVOKEVIRTUAL org/parboiled/matchers/ProxyMatcher.arm (Lorg/parboiled/matchers/Matcher;)V\n" +
                "30     DUP\n" +
                "31     ALOAD 0\n" +
                "32     SWAP\n" +
                "33     PUTFIELD org/parboiled/transform/ParserHierarchyTest$Parser3$$parboiled.cache$C : Lorg/parboiled/Rule;\n" +
                "34     ARETURN\n");
    }

    @Test
    public void testBugIn101() throws Exception {
        // threw IllegalStateException in 1.0.1
        ParserTransformer.extendParserClass(Parser4.class);
    }

}