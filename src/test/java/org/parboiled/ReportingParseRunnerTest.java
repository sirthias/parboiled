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

package org.parboiled;

import org.parboiled.examples.calculators.CalculatorParser1;
import org.parboiled.examples.java.JavaParser;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.assertEquals;

public class ReportingParseRunnerTest extends AbstractTest {

    @Test
    public void testSimpleReporting() {
        CalculatorParser1 parser = Parboiled.createParser(CalculatorParser1.class);
        test(parser, "X1+2", "Invalid input 'X', expected InputLine (line 1, pos 1):\nX1+2\n^\n");
        test(parser, "1X+2", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 2):\n1X+2\n ^\n");
        test(parser, "1+X2", "Invalid input 'X', expected Term (line 1, pos 3):\n1+X2\n  ^\n");
        test(parser, "1+2X", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 4):\n1+2X\n   ^\n");
        test(parser, "1+2X*(3-4)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 4):\n1+2X*(3-4)-5\n   ^\n");
        test(parser, "1+2*X(3-4)-5", "Invalid input 'X', expected Factor (line 1, pos 5):\n1+2*X(3-4)-5\n    ^\n");
        test(parser, "1+2*(X3-4)-5", "Invalid input 'X', expected Expression (line 1, pos 6):\n1+2*(X3-4)-5\n     ^\n");
        test(parser, "1+2*(3X-4)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 7):\n1+2*(3X-4)-5\n      ^\n");
        test(parser, "1+2*(3-X4)-5", "Invalid input 'X', expected Term (line 1, pos 8):\n1+2*(3-X4)-5\n       ^\n");
        test(parser, "1+2*(3-4X)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 9):\n1+2*(3-4X)-5\n        ^\n");
        test(parser, "1+2*(3-4)X-5", "Invalid input 'X', expected '*', '/', '+', '-' or EOI (line 1, pos 10):\n1+2*(3-4)X-5\n         ^\n");
        test(parser, "1+2*(3-4)-X5", "Invalid input 'X', expected Term (line 1, pos 11):\n1+2*(3-4)-X5\n          ^\n");
        test(parser, "1+2*(3-4)-5X", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 12):\n1+2*(3-4)-5X\n           ^\n");
        test(parser, "1+2*(3-4-5", "Invalid input 'EOI', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 11):\n1+2*(3-4-5\n          ^\n");
        test(parser, "1+2*3-4)-5", "Invalid input ')', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 8):\n1+2*3-4)-5\n       ^\n");
    }

    private void test(CalculatorParser1 parser, String input, String expectedErrorMessage) {
        Rule rule = parser.InputLine();
        ParsingResult result = ReportingParseRunner.run(rule, input);
        assertEquals(result.parseErrors.size(), 1);
        assertEqualsMultiline(printParseErrors(result), expectedErrorMessage);
    }

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
        ParsingResult result = ReportingParseRunner.run(rule, sourceWithErrors);
        assertEquals(result.parseErrors.size(), 1);
        assertEqualsMultiline(printParseErrors(result), "" +
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
        ParsingResult result = ReportingParseRunner.run(rule, sourceWithErrors);
        assertEquals(result.parseErrors.size(), 1);
        assertEqualsMultiline(printParseErrors(result), "" +
                "Invalid input 't', expected ' ', '\\t', '\\r', '\\n', '\\f', \"/*\", \"//\", Dim, '=', ',' or ';' (line 5, pos 22):\n" +
                "        String name  toString();\n" +
                "                     ^\n");
    }

    public static class Parser extends BaseParser<Object> {
        Rule Line() {
            return Sequence("Text;", OneOrMore(Sequence(TestNot(';'), Any())), ';', Eoi());
        }
    }

    @Test
    public void testErrorLocation() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Line();
        ParsingResult result = ReportingParseRunner.run(rule, "Text;;Something");
        assertEquals(result.parseErrors.size(), 1);
        assertEqualsMultiline(printParseErrors(result), "" +
                "Invalid input ';' (line 1, pos 6):\n" +
                "Text;;Something\n" +
                "     ^\n");
    }

}