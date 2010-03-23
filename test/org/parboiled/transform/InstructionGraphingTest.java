/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.parboiled.test.FileUtils;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.List;

import static org.parboiled.test.TestUtils.computeCRC;
import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;

public class InstructionGraphingTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new ImplicitActionsConverter(),
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new InstructionGrouper()
    );

    @SuppressWarnings({"FieldCanBeLocal"})
    private String dotSource;

    @Test
    public void testInstructionGraphing() throws Exception {
        testMethodAnalysis("RuleWithDirectImplicitAction", 3866468079L);
        // renderToGraphViz(dotSource);

        //testMethodAnalysis("RuleWithIndirectExplicit2ParamAction", 2056891149L);
        renderToGraphViz(dotSource);

        testMethodAnalysis("upSetActionRule", 383151074L);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("booleanExpressionActionRule", 3547608743L);
        // renderToGraphViz(dotSource);

        testMethodAnalysis("complexActionsRule", 3194993502L);
        // renderToGraphViz(dotSource);
    }

    private void testMethodAnalysis(String methodName, long dotSourceCRC) throws Exception {
        RuleMethod method = processMethod(methodName, processors);

        dotSource = generateDotSource(method);
        long crc = computeCRC(dotSource);
        if (crc != dotSourceCRC) {
            System.err.println("Invalid dotSource CRC for method '" + methodName + "': " + crc + 'L');
            //assertEqualsMultiline(dotSource, "");
        }
    }

    public String generateDotSource(@NotNull RuleMethod method) {
        // generate graph attributes
        StringBuilder sb = new StringBuilder()
                .append("digraph G {\n")
                .append("fontsize=10;\n")
                .append("label=\"")
                .append(getMethodInstructionList(method).replace("\n", "\\l").replace("\"", "\'"))
                .append("\";\n");

        // legend
        sb.append(" Action [penwidth=2.0,color=magenta];\n");
        sb.append(" Capture [penwidth=2.0,color=blue];\n");
        sb.append(" ContextSwitch [penwidth=2.0,color=green];\n");
        sb.append(" CallOnContextAware [penwidth=2.0];\n");
        sb.append(" Action -> Capture -> ContextSwitch -> CallOnContextAware;\n");

        for (InstructionGraphNode node : method.getGraphNodes()) {
            // generate node
            boolean isSpecial = node.isActionRoot() || node.isCaptureRoot() || node.isContextSwitch() ||
                    node.isCallOnContextAware();
            sb.append(" ").append(node.getInstructionIndex())
                    .append(" [")
                    .append(isSpecial ? "penwidth=2.0," : "penwidth=1.0,")
                    .append(node.isActionRoot() ? "color=magenta," : "")
                    .append(node.isCaptureRoot() ? "color=blue," : "")
                    .append(node.isContextSwitch() ? "color=green," : "")
                    .append(node.getGroups().isEmpty() ? "fontcolor=red" :
                            new StringBuilder("label=\"\\N ")
                                    .append(belongsToGroup(node, InstructionGroup.RETURN) ? "R" : "")
                                    .append(belongsToGroup(node, InstructionGroup.ACTION) ? "A" : "")
                                    .append(belongsToGroup(node, InstructionGroup.CAPTURE) ? "C" : "")
                                    .append('\"')
                                    .toString())
                    .append(node.getGroups().size() > 1 ? ",style=filled,fillcolor=red" :
                            new StringBuilder()
                                    .append(node.getGroups().size() == 1 ? ",style=filled,fillcolor=" : "")
                                    .append(belongsToGroup(node, InstructionGroup.RETURN) ? "lightgrey" : "")
                                    .append(belongsToGroup(node, InstructionGroup.ACTION) ? "lightblue" : "")
                                    .append(belongsToGroup(node, InstructionGroup.CAPTURE) ? "orange" : "")
                                    .toString())
                    .append("];\n");

            // generate edges
            for (InstructionGraphNode pred : node.getPredecessors()) {
                sb.append(" ").append(pred.getInstructionIndex()).append(" -> ").append(node.getInstructionIndex())
                        .append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    public boolean belongsToGroup(InstructionGraphNode node, int groupTypes) {
        for (InstructionGroup group : node.getGroups()) {
            if ((group.getType() & groupTypes) > 0) return true;
        }
        return false;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static void renderToGraphViz(String dotSource) throws Exception {
        String command = "C:\\Program Files\\Graphviz2.26\\bin\\dot.exe -Tpng";
        String output = "C:\\3\\graph.png";

        final Process process = Runtime.getRuntime().exec(command);
        FileUtils.copyAll(new ByteArrayInputStream(dotSource.getBytes("UTF-8")), process.getOutputStream());
        new Thread(new Runnable() {
            public void run() {
                FileUtils.copyAll(process.getErrorStream(), System.err);
            }
        }).start();
        FileUtils.copyAll(process.getInputStream(), new FileOutputStream(output));
        process.waitFor();
    }

}
