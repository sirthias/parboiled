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
import org.parboiled.common.FileUtils;
import org.parboiled.common.ImmutableList;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.CRC32;

import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;
import static org.testng.Assert.assertEquals;

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

        testMethodAnalysis("RuleWithComplexActionSetup", 724347041L);
        //renderToGraphViz(dotSource);
    }

    private void testMethodAnalysis(String methodName, long dotSourceCRC) throws Exception {
        RuleMethod method = processMethod(methodName, processors);

        dotSource = generateDotSource(method);
        long crc = computeCRC(dotSource);
        if (crc != dotSourceCRC) {
            System.err.println("Invalid dotSource CRC for method '" + methodName + "': " + crc + 'L');
            assertEquals(dotSource, "");
        }
    }

    private String generateDotSource(RuleMethod method) {
        checkArgNotNull(method, "method");

        // generate graph attributes
        StringBuilder sb = new StringBuilder()
                .append("digraph G {\n")
                .append("fontsize=10;\n")
                .append("label=\"")
                .append(getMethodInstructionList(method).replace("\n", "\\l").replace("\"", "\'"))
                .append("\";\n");

        // legend
        sb.append(" Action [penwidth=2.0,style=filled,fillcolor=skyblue];\n");
        sb.append(" VarInit [penwidth=2.0,style=filled,fillcolor=grey];\n");
        sb.append(" XLoad [penwidth=2.0,color=orange];\n");
        sb.append(" XStore [penwidth=2.0,color=red];\n");
        sb.append(" CallOnContextAware [penwidth=2.0];\n");
        sb.append(" Action -> Capture -> VarInit -> ContextSwitch -> XLoad -> XStore -> CallOnContextAware;\n");

        for (int i = 0; i < method.getGraphNodes().size(); i++) {
            InstructionGraphNode node = method.getGraphNodes().get(i);
            // generate node
            boolean isSpecial = node.isActionRoot() || node.isVarInitRoot() ||
                    node.isXLoad() || node.isXStore() || node.isCallOnContextAware();
            sb.append(" ").append(i)
                    .append(" [")
                    .append(isSpecial ? "penwidth=2.0," : "penwidth=1.0,")
                    .append(node.isActionRoot() ? "color=skyblue," : "")
                    .append(node.isVarInitRoot() ? "color=grey," : "")
                    .append(node.isXLoad() ? "color=orange," : "")
                    .append(node.isXStore() ? "color=red," : "")
                    .append(node.getGroup() != null && node.getGroup().getRoot().isActionRoot() ?
                            "style=filled,fillcolor=\"/pastel15/" + (method.getGroups()
                                    .indexOf(node.getGroup()) + 1) + "\"," : "")
                    .append(node.getGroup() != null && node.getGroup().getRoot().isVarInitRoot() ?
                            "style=filled,fillcolor=grey," : "")
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
        String command = "/usr/local/bin/dot -Tpng";
        String output = "/Users/mathias/Downloads/graph.png";

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

    private static long computeCRC(String text) throws Exception {
        CRC32 crc32 = new CRC32();
        byte[] buf = text.getBytes("UTF8");
        crc32.update(buf);
        return crc32.getValue();
    }

}
