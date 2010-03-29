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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.test.TestUtils.computeCRC;
import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;

public class InstructionGroupCreatorTest extends TransformationTest {

    private final List<RuleMethodProcessor> processors = ImmutableList.of(
            new UnusedLabelsRemover(),
            new ReturnInstructionUnifier(),
            new InstructionGraphCreator(),
            new ImplicitActionsConverter(),
            new InstructionGroupCreator()
    );

    @SuppressWarnings({"FieldCanBeLocal"})
    private String dotSource;

    @Test
    public void testInstructionGraphing() throws Exception {
        setup(TestParser.class);

        testMethodAnalysis("RuleWithComplexActionSetup", 1823811521L);
        //renderToGraphViz(dotSource);

        testMethodAnalysis("RuleWithIndirectExplicitDownAction", 1228139512L);
        //renderToGraphViz(dotSource);
    }

    private void testMethodAnalysis(String methodName, long dotSourceCRC) throws Exception {
        RuleMethod method = processMethod(methodName, processors);

        dotSource = generateDotSource(method);
        long crc = computeCRC(dotSource);
        if (crc != dotSourceCRC) {
            System.err.println("Invalid dotSource CRC for method '" + methodName + "': " + crc + 'L');
            assertEqualsMultiline(dotSource, "");
        }
    }

    private String generateDotSource(@NotNull RuleMethod method) {
        // generate graph attributes
        StringBuilder sb = new StringBuilder()
                .append("digraph G {\n")
                .append("fontsize=10;\n")
                .append("label=\"")
                .append(getMethodInstructionList(method).replace("\n", "\\l").replace("\"", "\'"))
                .append("\";\n");

        // legend
        sb.append(" Action [penwidth=2.0,style=filled,fillcolor=skyblue];\n");
        sb.append(" Capture [penwidth=2.0,style=filled,fillcolor=pink];\n");
        sb.append(" ContextSwitch [penwidth=2.0,color=green];\n");
        sb.append(" XLoad [penwidth=2.0,color=orange];\n");
        sb.append(" XStore [penwidth=2.0,color=red];\n");
        sb.append(" CallOnContextAware [penwidth=2.0];\n");
        sb.append(" Action -> Capture -> ContextSwitch -> XLoad -> XStore -> CallOnContextAware;\n");

        for (int i = 0; i < method.getGraphNodes().size(); i++) {
            InstructionGraphNode node = method.getGraphNodes().get(i);
            // generate node
            boolean isSpecial = node.isActionRoot() || node.isCaptureRoot() || node.isContextSwitch() ||
                    node.isXLoad() || node.isXStore() || node.isCallOnContextAware();
            sb.append(" ").append(i)
                    .append(" [")
                    .append(isSpecial ? "penwidth=2.0," : "penwidth=1.0,")
                    .append(node.isActionRoot() ? "color=skyblue," : "")
                    .append(node.isCaptureRoot() ? "color=pink," : "")
                    .append(node.isContextSwitch() ? "color=green," : "")
                    .append(node.isXLoad() ? "color=orange," : "")
                    .append(node.isXStore() ? "color=red," : "")
                    .append(node.getGroup() != null && node.getGroup().getRoot().isActionRoot() ?
                            "style=filled,fillcolor=skyblue," : "")
                    .append(node.getGroup() != null && node.getGroup().getRoot().isCaptureRoot() ?
                            "style=filled,fillcolor=pink," : "")
                    .append("fontcolor=black];\n");

            // generate edges
            for (InstructionGraphNode pred : node.getPredecessors()) {
                sb.append(" ").append(method.getGraphNodes().indexOf(pred)).append(" -> ").append(i)
                        .append(";\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private static void renderToGraphViz(String dotSource) throws Exception {
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
