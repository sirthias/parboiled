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
import static org.parboiled.test.TestUtils.computeCRC;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

public class RuleMethodTransformationTest {

    private final ParserClassNode classNode = new ParserClassNode(TestParser.class);

    @BeforeClass
    private void setup() throws Exception {
        new ClassNodeInitializer(
                new RuleMethodAnalyzer(
                        new RuleMethodInstructionGraphPartitioner(
                                new RuleMethodTransformer(
                                        new RuleCachingGenerator(null)
                                )
                        )
                )
        ).transform(classNode);
    }

    @Test
    public void testTransformRuleMethods() throws Exception {
        testRuleMethodTransformation("any", 1199296662L);
        testRuleMethodTransformation("noActionRule", 2855897108L);
        testRuleMethodTransformation("simpleActionRule", 185852305L);
        testRuleMethodTransformation("upSetActionRule", 2586997939L);
        testRuleMethodTransformation("booleanExpressionActionRule", 436033411L);
        testRuleMethodTransformation("complexActionsRule", 1496808455L);
    }

    private void testRuleMethodTransformation(String methodName, long expectedTraceCRC) throws Exception {
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

                String classListing = stringWriter.toString();
                long crc = computeCRC(classListing);
                if (crc != expectedTraceCRC) {
                    System.err.printf("Invalid class listing CRC for method '%s': %sL\n", methodName, crc);
                    assertEqualsMultiline(classListing, "");
                }
                return;
            }
        }
    }
}
