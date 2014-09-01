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

import static org.parboiled.common.Preconditions.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.*;
import org.parboiled.common.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.testng.Assert.assertEquals;

public class AsmTestUtils {

    public static String getClassDump(byte[] code) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(printWriter);
        ClassVisitor checkClassAdapter = new ClassVisitor(Opcodes.ASM4, traceClassVisitor) {};
        //ClassAdapter checkClassAdapter = new CheckClassAdapter(traceClassVisitor);
        ClassReader classReader;
        classReader = new ClassReader(code);
        classReader.accept(checkClassAdapter, 0);
        printWriter.flush();
        return stringWriter.toString();
    }

    public static String getMethodInstructionList(MethodNode methodNode) {
        checkArgNotNull(methodNode, "methodNode");
        Printer printer = new NonMaxTextifier();
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        methodNode.accept(traceMethodVisitor);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printer.print(printWriter);
        printWriter.flush();
        String[] lines = stringWriter.toString().split("\n");
        int lineNr = 0;
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].startsWith("  @")) {
                lines[i] = String.format("%2d %s", lineNr++, lines[i]);
            }
        }
        return "Method '" + methodNode.name + "':\n" + StringUtils.join(lines, "\n") + '\n';
    }

    public static void assertTraceDumpEquality(MethodNode method, String traceDump) throws Exception {
        checkArgNotNull(method, "method");
        Printer printer = new NonMaxTextifier();
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(printer);
        // MethodAdapter checkMethodAdapter = new MethodAdapter(traceMethodVisitor);
        MethodVisitor checkMethodAdapter = new CheckMethodAdapter(traceMethodVisitor);
        method.accept(checkMethodAdapter);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        printer.print(printWriter);
        printWriter.flush();

        assertEquals(stringWriter.toString(), traceDump);
    }

    public static void verifyIntegrity(String classInternalName, byte[] classCode) {
        checkArgNotNull(classCode, "classCode");
        ClassNode generatedClassNode = new ClassNode(Opcodes.ASM4);
        ClassReader classReader = new ClassReader(classCode);
        classReader.accept(generatedClassNode, 0);

        for (Object methodObj : generatedClassNode.methods) {
            verifyMethodIntegrity(classInternalName, (MethodNode) methodObj);
        }
    }

    public static void verifyMethodIntegrity(String ownerInternalName, MethodNode method) {
        try {
            new Analyzer(new SimpleVerifier()).analyze(ownerInternalName, method);
        } catch (AnalyzerException e) {
            throw new RuntimeException(
                    "Integrity error in method '" + method.name + "' of type '" + ownerInternalName + "': ", e);
        }
    }

    private static class NonMaxTextifier extends Textifier {
        private NonMaxTextifier() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            // don't include max values
        }
    }

}
