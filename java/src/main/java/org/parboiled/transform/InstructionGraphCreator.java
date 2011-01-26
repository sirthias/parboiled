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

package org.parboiled.transform;

import static org.parboiled.common.Preconditions.checkArgNotNull;

import org.objectweb.asm.tree.analysis.Analyzer;

/**
 * Performs data/control flow analysis and constructs the instructions graph.
 */
class InstructionGraphCreator implements RuleMethodProcessor {

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.containsImplicitActions() || method.containsExplicitActions() || method.containsVarInitializers();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(method, "method");
        final RuleMethodInterpreter interpreter = new RuleMethodInterpreter(method);

        new Analyzer(interpreter) {
            @Override
            protected void newControlFlowEdge(int insn, int successor) {
                interpreter.newControlFlowEdge(insn, successor);
            }

            @Override
            protected boolean newControlFlowExceptionEdge(int insn, int successor) {
                interpreter.newControlFlowEdge(insn, successor);
                return true;
            }
        }.analyze(classNode.name, method);

        interpreter.finish();
    }

}
