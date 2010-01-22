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
