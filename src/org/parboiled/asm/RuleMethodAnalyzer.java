/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.asm;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.parboiled.support.Checks;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RuleMethodAnalyzer implements ClassTransformer {

    private final ClassTransformer nextTransformer;

    public RuleMethodAnalyzer(ClassTransformer classTransformer) {
        this.nextTransformer = classTransformer;
    }

    @SuppressWarnings({"unchecked"})
    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (MethodNode method : (List<MethodNode>) classNode.methods) {
            RuleMethodInfo methodInfo = analyzeRuleMethod(classNode, method);
            classNode.methodInfos.add(methodInfo);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private RuleMethodInfo analyzeRuleMethod(ParserClassNode classNode, MethodNode method) throws AnalyzerException {
        Checks.ensure(method.maxLocals == 1, "Parser rule method '%s()' contains local variables, " +
                "which are not allowed in rule defining methods", method.name);

        RuleMethodInfo methodInfo = new RuleMethodInfo(method);
        final RuleMethodInterpreter interpreter = new RuleMethodInterpreter(classNode, methodInfo);

        new Analyzer(interpreter) {
            @Override
            protected void newControlFlowEdge(int insn, int successor) {
                interpreter.newControlFlowEdge(insn, successor);
            }
        }.analyze(classNode.name, method);

        interpreter.finish();

        return methodInfo;
    }

}
