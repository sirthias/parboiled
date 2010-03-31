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

package org.parboiled.examples;

import org.parboiled.Parboiled;
import org.parboiled.ReportingParseRunner;
import org.parboiled.examples.calculators.*;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.test.AbstractTest;
import org.parboiled.trees.Filters;
import org.testng.annotations.Test;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class CalculatorsTest extends AbstractTest {

    @Test
    public void testCalculator0() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser0.class);
        test(parser.inputLine(), "1+5", "" +
                "[inputLine] '1+5'\n" +
                "    [expression] '1+5'\n" +
                "        [term] '1'\n" +
                "            [factor] '1'\n" +
                "                [number] '1'\n" +
                "                    [digit] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore] '+5'\n" +
                "            [sequence] '+5'\n" +
                "                [[+-]] '+'\n" +
                "                [term] '5'\n" +
                "                    [factor] '5'\n" +
                "                        [number] '5'\n" +
                "                            [digit] '5'\n" +
                "                    [zeroOrMore]\n" +
                "    [EOI]\n");
    }

    @Test
    public void testCalculator1() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser1.class);
        runBasicCalculationTests(parser, "");
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testCalculator2() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser2.class);

        assertEqualsMultiline(
                printTree(
                        (Matcher<Integer>) parser.inputLine(),
                        new ToStringFormatter<Matcher<Integer>>(),
                        Filters.<Integer>preventLoops()
                ), "" +
                        "inputLine\n" +
                        "    expression\n" +
                        "        term\n" +
                        "            factor\n" +
                        "                number\n" +
                        "                    digits\n" +
                        "                        digit\n" +
                        "                    number_Action1\n" +
                        "                parens\n" +
                        "                    '('\n" +
                        "                    expression\n" +
                        "                    ')'\n" +
                        "            term_Action1\n" +
                        "            zeroOrMore\n" +
                        "                firstOf\n" +
                        "                    sequence\n" +
                        "                        '*'\n" +
                        "                        factor\n" +
                        "                        term_Action2\n" +
                        "                    sequence\n" +
                        "                        '/'\n" +
                        "                        factor\n" +
                        "                        term_Action3\n" +
                        "        expression_Action1\n" +
                        "        zeroOrMore\n" +
                        "            firstOf\n" +
                        "                sequence\n" +
                        "                    '+'\n" +
                        "                    term\n" +
                        "                    expression_Action2\n" +
                        "                sequence\n" +
                        "                    '-'\n" +
                        "                    term\n" +
                        "                    expression_Action3\n" +
                        "    EOI\n");

        runBasicCalculationTests(parser, "");
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testCalculator3() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser3.class);

        assertEqualsMultiline(
                printTree(
                        (Matcher<Integer>) parser.inputLine(),
                        new ToStringFormatter<Matcher<Integer>>(),
                        Filters.<Integer>preventLoops()
                ), "" +
                        "inputLine\n" +
                        "    expression\n" +
                        "        term\n" +
                        "            factor\n" +
                        "                number\n" +
                        "                    digits\n" +
                        "                        digit\n" +
                        "                    number_Action1\n" +
                        "                parens\n" +
                        "                    '('\n" +
                        "                    expression\n" +
                        "                    ')'\n" +
                        "            term_Action1\n" +
                        "            zeroOrMore\n" +
                        "                sequence\n" +
                        "                    op\n" +
                        "                    factor\n" +
                        "                    term_Action2\n" +
                        "        expression_Action1\n" +
                        "        zeroOrMore\n" +
                        "            sequence\n" +
                        "                op\n" +
                        "                term\n" +
                        "                expression_Action2\n" +
                        "    EOI\n");

        runBasicCalculationTests(parser, "");
    }

    @Test
    public void testCalculator4() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser4.class);
        runBasicCalculationTests(parser, ".0");
        runExtendedCalculationTests(parser);
    }

    @Test
    public void testCalculator5() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser5.class);
        runBasicCalculationTests(parser, ".0");
        runExtendedCalculationTests(parser);
    }

    private void runBasicCalculationTests(CalculatorParser parser, String suffix) {
        test(parser, "1+2", "3" + suffix);
        test(parser, "1+2-3+4", "4" + suffix);
        test(parser, "1-2-3", "-4" + suffix);
        test(parser, "1-(2-3)", "2" + suffix);
        test(parser, "1*2+3", "5" + suffix);
        test(parser, "1+2*3", "7" + suffix);
        test(parser, "1*2*3", "6" + suffix);
        test(parser, "3*4/6", "2" + suffix);
        test(parser, "24/6/2", "2" + suffix);
        test(parser, "1-2*3-4", "-9" + suffix);
        test(parser, "1-2*3-4*5-6", "-31" + suffix);
        test(parser, "1-24/6/2-(5+7)", "-13" + suffix);
        test(parser, "((1+2)*3-(4-5))/5", "2" + suffix);
    }

    private void runExtendedCalculationTests(CalculatorParser parser) {
        test(parser, "1 + 2 +3", "6.0");
        test(parser, "1-2.0- -3.5", "2.5");
        test(parser, "13+SQRT( 2*50 )^2 ", "113.0");
        test(parser, "-0.10 * 10^(3+1)", "-1000.0");
        test(parser, "1-2*3^ 4 /5+6", "-25.4");
    }

    @SuppressWarnings({"unchecked"})
    private void test(CalculatorParser parser, String input, String value) {
        ParsingResult result = ReportingParseRunner.run(parser.inputLine(), input);
        if (result.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    printParseErrors(result.parseErrors, result.inputBuffer) +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(result)
            );
        }
        String str = result.parseTreeRoot.getValue().toString();
        int ix = str.indexOf('|');
        if (ix >= 0) str = str.substring(ix + 1);
        assertEquals(str, value);
    }

}