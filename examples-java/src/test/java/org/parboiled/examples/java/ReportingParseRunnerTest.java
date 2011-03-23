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

package org.parboiled.examples.java;

import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.testng.Assert.assertEquals;

public class ReportingParseRunnerTest {

    @Test
    public void testJavaError1() {
        String sourceWithErrors = "package org.parboiled.examples;\n" +
                "public class JavaTestSource {\n" +
                "    @SuppressWarnings({\"UnnecessaryLocalVariable\", \"UnusedDeclaration\"})\n" +
                "    public String method(int param) {\n" +
                "        String name = toString(;\n" +
                "        return name;\n" +
                "    }\n" +
                "}";

        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule rule = parser.CompilationUnit();
        ParsingResult result = new ReportingParseRunner(rule).run(sourceWithErrors);
        assertEquals(result.parseErrors.size(), 1);
        assertEquals(printParseErrors(result), "" +
                "Invalid input ';', expected Spacing, Expression or ')' (line 5, pos 32):\n" +
                "        String name = toString(;\n" +
                "                               ^\n");
    }

    @Test
    public void testJavaError2() {
        String sourceWithErrors = "package org.parboiled.examples;\n" +
                "public class JavaTestSource {\n" +
                "    @SuppressWarnings({\"UnnecessaryLocalVariable\", \"UnusedDeclaration\"})\n" +
                "    public String method(int param) {\n" +
                "        String name  toString();\n" +
                "        return name;\n" +
                "    }\n" +
                "}";

        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule rule = parser.CompilationUnit();
        ParsingResult result = new ReportingParseRunner(rule).run(sourceWithErrors);
        assertEquals(result.parseErrors.size(), 1);
        assertEquals(printParseErrors(result), "" +
                "Invalid input 't', expected Whitespace, \"/*\", \"//\", Dim, '=', ',' or ';' (line 5, pos 22):\n" +
                "        String name  toString();\n" +
                "                     ^\n");
    }
}