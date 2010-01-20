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

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.parboiled.common.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class AsmTestUtils {

    public static String getMethodInstructionList(MethodNode methodNode) {
        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor();
        methodNode.accept(traceMethodVisitor);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        traceMethodVisitor.print(printWriter);
        printWriter.flush();
        String[] lines = stringWriter.toString().split("\n");
        for (int i = 0; i < lines.length; i++) {
            lines[i] = String.format("%2d %s", i, lines[i]);
        }
        return new StringBuilder()
                .append("Method '").append(methodNode.name).append("':\n")
                .append(StringUtils.join(lines, "\n"))
                .append('\n')
                .toString();
    }

    public static RuleMethodInfo getByName(List<RuleMethodInfo> methodInfos, String methodName) {
        for (RuleMethodInfo methodInfo : methodInfos) {
            if (methodName.equals(methodInfo.method.name)) return methodInfo;
        }
        throw new IllegalArgumentException("Method '" + methodName + "' not found");
    }

}
