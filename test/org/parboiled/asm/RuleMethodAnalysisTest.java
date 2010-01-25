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

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.analysis.Value;
import static org.parboiled.asm.AsmTestUtils.getMethodInstructionList;
import static org.parboiled.test.TestUtils.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.List;

@SuppressWarnings({"FieldCanBeLocal"})
public class RuleMethodAnalysisTest {

    private final ParserClassNode classNode = new ParserClassNode(TestParser.class);
    private final List<RuleMethodInfo> methodInfos = classNode.methodInfos;
    private String dotSource;

    @BeforeClass
    public void setup() throws Exception {
        new ClassNodeInitializer(
                new DontExtendMethodRemover(
                        new RuleMethodAnalyzer(
                                new RuleMethodInstructionGraphPartitioner(null)
                        )
                )
        ).transform(classNode);
    }

    @Test
    public void testMethodAnalysis() throws Exception {
        testMethodAnalysis("noActionRule", 1998088504L, false);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("simpleActionRule", 450882377L, true);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("upSetActionRule", 2759802575L, true);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("booleanExpressionActionRule", 727215768L, true);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("complexActionsRule", 2150516099L, true);
        // renderToGraphViz(dotSource);
    }

    private void testMethodAnalysis(String methodName, long dotSourceCRC, boolean hasActions) throws Exception {
        RuleMethodInfo info = AsmTestUtils.getByName(methodInfos, methodName);

        // make sure all instructions are covered
        for (InstructionGraphNode node : info.instructionGraphNodes) assertNotNull(node);

        // check action detection
        assertEquals(info.hasActions(), hasActions);

        dotSource = generateDotSource(info, info.getInstructionSubSets());
        long crc = computeCRC(dotSource);
        if (crc != dotSourceCRC) {
            System.err.println("Invalid dotSource CRC for method '" + methodName + "': " + crc + 'L');
            assertEqualsMultiline(dotSource, "");
        }
    }

    public String generateDotSource(@NotNull RuleMethodInfo info, List<InstructionSubSet> instructionSubSets) {
        // generate graph attributes
        StringBuilder sb = new StringBuilder()
                .append("digraph G {\n")
                .append("fontsize=10;\n")
                .append("label=\"")
                .append(getMethodInstructionList(info.method).replace("\n", "\\l").replace("\"", "\'"))
                .append("\";\n");

        InstructionGraphNode returnNode = info.getReturnNode();
        for (InstructionGraphNode node : info.instructionGraphNodes) {
            // generate node
            sb.append("    ").append(node.instructionIndex)
                    .append(node.isAction ? "[penwidth=2.0,color=red]" : "")
                    .append(node.isContextSwitch ? "[penwidth=2.0,color=orange]" : "")
                    .append(node.isRuleCreation ? "[penwidth=2.0,color=brown]" : "")
                    .append(node == returnNode ? "[penwidth=2.0,color=blue]" : "")
                    .append(";\n");
            if (instructionSubSets != null) {
                for (int i = 0; i < instructionSubSets.size(); i++) {
                    InstructionSubSet subSet = instructionSubSets.get(i);
                    if (subSet.containsInstruction(node.instructionIndex) && subSet.isActionSet) {
                        sb.append("            ").append(node.instructionIndex)
                                .append(" [style=filled,fillcolor=\"/gnbu5/").append(i + 1).append("\"];\n");
                    }
                }
            }

            // generate edges
            for (Value pred : node.predecessors) {
                if (pred instanceof InstructionGraphNode) {
                    sb.append("    ").append(((InstructionGraphNode) pred).instructionIndex)
                            .append(" -> ").append(node.instructionIndex).append(";\n");
                }
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    public static void renderToGraphViz(String dotSource) throws Exception {
        String command = "C:\\Program Files\\Graphviz2.26\\bin\\dot.exe -Tpng";
        String output = "C:\\3\\graph.png";

        final Process process = Runtime.getRuntime().exec(command);
        copyAll(new ByteArrayInputStream(dotSource.getBytes("UTF-8")), process.getOutputStream());
        new Thread(new Runnable() {
            public void run() {
                copyAll(process.getErrorStream(), System.err);
            }
        }).start();
        copyAll(process.getInputStream(), new FileOutputStream(output));
        process.waitFor();
    }

}
