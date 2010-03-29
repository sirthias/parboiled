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
import org.parboiled.examples.calculators.CalculatorParser;
import org.parboiled.examples.calculators.CalculatorParser1;
import org.parboiled.examples.calculators.CalculatorParser2;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.test.AbstractTest;
import org.parboiled.trees.Filters;
import org.testng.annotations.Test;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class CalculatorsTest extends AbstractTest {

    @Test
    public void testCalculator1() {
        CalculatorParser1 parser = Parboiled.createParser(CalculatorParser1.class);
        test(parser.inputLine(), "1+5", "" +
                "[inputLine, {6}] '1+5'\n" +
                "    [expression, {6}] '1+5'\n" +
                "        [term, {1}] '1'\n" +
                "            [factor, {1}] '1'\n" +
                "                [number, {1}] '1'\n" +
                "                    [oneOrMore] '1'\n" +
                "                        [digit] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore, {5}] '+5'\n" +
                "            [sequence, {5}] '+5'\n" +
                "                [[+-]] '+'\n" +
                "                [term, {5}] '5'\n" +
                "                    [factor, {5}] '5'\n" +
                "                        [number, {5}] '5'\n" +
                "                            [oneOrMore] '5'\n" +
                "                                [digit] '5'\n" +
                "                    [zeroOrMore]\n" +
                "    [EOI]\n");

        assertEquals(run(parser, "5*(2+6/3)").intValue(), 20);
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void testCalculator2() {
        CalculatorParser2 parser = Parboiled.createParser(CalculatorParser2.class);

        assertEqualsMultiline(
                printTree(
                        (Matcher<Integer>) parser.inputLine(),
                        new ToStringFormatter<Matcher<Integer>>(),
                        Filters.<Integer>preventLoops()
                ),
                "inputLine\n" +
                        "    expression\n" +
                        "        term\n" +
                        "            factor\n" +
                        "                number\n" +
                        "                    oneOrMore\n" +
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

        assertEquals(run(parser, "5*(2+6/3)").intValue(), 20);
        assertEquals(run(parser, "1+(2*3-4/5)").intValue(), 7);
    }

    private <V> V run(CalculatorParser<V> parser, String input) {
        ParsingResult<V> result = ReportingParseRunner.run(parser.inputLine(), input);
        assertFalse(result.hasErrors());
        return result.parseTreeRoot.getValue();
    }

}