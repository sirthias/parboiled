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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import org.parboiled.test.AsmTestUtils;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.test.TestUtils.computeCRC;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class ActionClassGeneratorTest {

    private final ParserClassNode classNode = new ParserClassNode(TestParser.class);
    private List<RuleMethodInfo> methodInfos;

    @BeforeClass
    private void setup() throws IOException, AnalyzerException {
        new ClassNodeInitializer(classNode).initialize();
        RuleMethodAnalyzer analyzer = new RuleMethodAnalyzer(classNode);
        methodInfos = analyzer.constructRuleMethodInstructionGraphs();
        new RuleMethodPartitioner(methodInfos).partitionMethodGraphs();
    }

    @Test
    public void testDefineActionClass() throws Exception {
        testActionClassGeneration("intComparison", 126014664L);
        testActionClassGeneration("skipInPredicate", 3543159925L);
        testActionClassGeneration("simpleTernary", 2533914645L);
        testActionClassGeneration("upDownAction", 1200884063L);
        testActionClassGeneration("twoActionsRule", 771298881L);
    }

    private void testActionClassGeneration(String methodName, long traceDumpCRC) throws Exception {
        RuleMethodInfo info = AsmTestUtils.getByName(methodInfos, methodName);

        for (InstructionSubSet subSet : info.getInstructionSubSets()) {
            if (subSet.isActionSet) {
                ActionClassGenerator actionClassGenerator = new ActionClassGenerator(classNode, info, subSet, 1);
                actionClassGenerator.defineActionClass();

                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                TraceClassVisitor traceClassVisitor = new TraceClassVisitor(printWriter);
                //ClassAdapter checkClassAdapter = new ClassAdapter(traceClassVisitor);
                ClassAdapter checkClassAdapter = new CheckClassAdapter(traceClassVisitor);
                ClassReader classReader = new ClassReader(actionClassGenerator.getCode());
                classReader.accept(checkClassAdapter, 0);
                printWriter.flush();

                String classListing = stringWriter.toString();
                long crc = computeCRC(classListing);
                if (crc != traceDumpCRC) {
                    System.out.println("Invalid class listing CRC: " + crc + 'L');
                    assertEqualsMultiline(classListing, "");
                }

                return;
            }
        }
    }

}
