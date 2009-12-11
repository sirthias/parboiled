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

import org.parboiled.AbstractTest;
import org.parboiled.Parboiled;
import org.parboiled.examples.calculator.CalculatorParser;
import org.testng.annotations.Test;

public class CalculatorTest extends AbstractTest {

    @Test
    public void test() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        test(parser, parser.inputLine(), "1+5", "" +
                "[inputLine, {6}] '1+5'\n" +
                "    [expression, {6}] '1+5'\n" +
                "        [term, {1}] '1'\n" +
                "            [factor, {1}] '1'\n" +
                "                [number, {1}] '1'\n" +
                "                    [oneOrMore] '1'\n" +
                "                        [digit] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore, {5}] '+5'\n" +
                "            [enforcedSequence, {5}] '+5'\n" +
                "                [firstOf] '+'\n" +
                "                    ['+'] '+'\n" +
                "                [term, {5}] '5'\n" +
                "                    [factor, {5}] '5'\n" +
                "                        [number, {5}] '5'\n" +
                "                            [oneOrMore] '5'\n" +
                "                                [digit] '5'\n" +
                "                    [zeroOrMore]\n" +
                "    [eoi]\n");
    }

    @Test
    public void testSingleSymbolDeletion() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        testFail(parser, parser.inputLine(), "(8-6))", "" +
                "[inputLine, {2}] '(8-6))'\n" +
                "    [expression, {2}] '(8-6)'\n" +
                "        [term, {2}] '(8-6)'\n" +
                "            [factor, {2}] '(8-6)'\n" +
                "                [parens, {2}] '(8-6)'\n" +
                "                    ['('] '('\n" +
                "                    [expression, {2}] '8-6'\n" +
                "                        [term, {8}] '8'\n" +
                "                            [factor, {8}] '8'\n" +
                "                                [number, {8}] '8'\n" +
                "                                    [oneOrMore] '8'\n" +
                "                                        [digit] '8'\n" +
                "                            [zeroOrMore]\n" +
                "                        [zeroOrMore, {6}] '-6'\n" +
                "                            [enforcedSequence, {6}] '-6'\n" +
                "                                [firstOf] '-'\n" +
                "                                    ['-'] '-'\n" +
                "                                [term, {6}] '6'\n" +
                "                                    [factor, {6}] '6'\n" +
                "                                        [number, {6}] '6'\n" +
                "                                            [oneOrMore] '6'\n" +
                "                                                [digit] '6'\n" +
                "                                    [zeroOrMore]\n" +
                "                    [')'] ')'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore]\n" +
                "    [ILLEGAL] ')'\n" +
                "    [eoi]\n",
                "ParseError: Invalid input ')', expected eoi (line 1, pos 6):\n" +
                        "(8-6))\n" +
                        "     ^\n"
        );
    }

    @Test
    public void testSingleSymbolInsertion() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        testFail(parser, parser.inputLine(), "(8-6", "" +
                "[inputLine, {2}] '(8-6'\n" +
                "    [expression, {2}] '(8-6'\n" +
                "        [term, {2}] '(8-6'\n" +
                "            [factor, {2}] '(8-6'\n" +
                "                [parens, {2}] '(8-6'\n" +
                "                    ['('] '('\n" +
                "                    [expression, {2}] '8-6'\n" +
                "                        [term, {8}] '8'\n" +
                "                            [factor, {8}] '8'\n" +
                "                                [number, {8}] '8'\n" +
                "                                    [oneOrMore] '8'\n" +
                "                                        [digit] '8'\n" +
                "                            [zeroOrMore]\n" +
                "                        [zeroOrMore, {6}] '-6'\n" +
                "                            [enforcedSequence, {6}] '-6'\n" +
                "                                [firstOf] '-'\n" +
                "                                    ['-'] '-'\n" +
                "                                [term, {6}] '6'\n" +
                "                                    [factor, {6}] '6'\n" +
                "                                        [number, {6}] '6'\n" +
                "                                            [oneOrMore] '6'\n" +
                "                                                [digit] '6'\n" +
                "                                    [zeroOrMore]\n" +
                "                    [')']\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore]\n" +
                "    [eoi]\n",
                "ParseError: Invalid input EOI, expected ')' (line 1, pos 5):\n" +
                        "(8-6\n" +
                        "    ^\n"
        );
    }

    @Test
    public void testResynchronization1() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        testFail(parser, parser.inputLine(), "2*xxx(4-1)", "" +
                "[inputLine, {1}] '2*xxx(4-1)'\n" +
                "    [expression, {1}] '2*xxx(4-1'\n" +
                "        [term, {2}] '2*xxx(4'\n" +
                "            [factor, {2}] '2'\n" +
                "                [number, {2}] '2'\n" +
                "                    [oneOrMore] '2'\n" +
                "                        [digit] '2'\n" +
                "            [zeroOrMore] '*xxx(4'\n" +
                "                [enforcedSequence] '*xxx(4'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [factor]\n" +
                "                    [ILLEGAL] 'xxx(4'\n" +
                "        [zeroOrMore, {1}] '-1'\n" +
                "            [enforcedSequence, {1}] '-1'\n" +
                "                [firstOf] '-'\n" +
                "                    ['-'] '-'\n" +
                "                [term, {1}] '1'\n" +
                "                    [factor, {1}] '1'\n" +
                "                        [number, {1}] '1'\n" +
                "                            [oneOrMore] '1'\n" +
                "                                [digit] '1'\n" +
                "                    [zeroOrMore]\n" +
                "    [ILLEGAL] ')'\n" +
                "    [eoi]\n",
                "ParseError: Invalid input 'x', expected factor (line 1, pos 3):\n" +
                        "2*xxx(4-1)\n" +
                        "  ^\n" +
                        "---\n" +
                        "ParseError: Invalid input ')', expected eoi (line 1, pos 10):\n" +
                        "2*xxx(4-1)\n" +
                        "         ^\n"
        );
    }

    @Test
    public void testResynchronization2() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        testFail(parser, parser.inputLine(), "2*)(6-4)*3", "" +
                "[inputLine, {12}] '2*)(6-4)*3'\n" +
                "    [expression, {12}] '2*)(6-4)*3'\n" +
                "        [term, {12}] '2*)(6-4)*3'\n" +
                "            [factor, {2}] '2'\n" +
                "                [number, {2}] '2'\n" +
                "                    [oneOrMore] '2'\n" +
                "                        [digit] '2'\n" +
                "            [zeroOrMore, {3}] '*)(6-4)*3'\n" +
                "                [enforcedSequence, {2}] '*)(6-4)'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [ILLEGAL] ')'\n" +
                "                    [factor, {2}] '(6-4)'\n" +
                "                        [parens, {2}] '(6-4)'\n" +
                "                            ['('] '('\n" +
                "                            [expression, {2}] '6-4'\n" +
                "                                [term, {6}] '6'\n" +
                "                                    [factor, {6}] '6'\n" +
                "                                        [number, {6}] '6'\n" +
                "                                            [oneOrMore] '6'\n" +
                "                                                [digit] '6'\n" +
                "                                    [zeroOrMore]\n" +
                "                                [zeroOrMore, {4}] '-4'\n" +
                "                                    [enforcedSequence, {4}] '-4'\n" +
                "                                        [firstOf] '-'\n" +
                "                                            ['-'] '-'\n" +
                "                                        [term, {4}] '4'\n" +
                "                                            [factor, {4}] '4'\n" +
                "                                                [number, {4}] '4'\n" +
                "                                                    [oneOrMore] '4'\n" +
                "                                                        [digit] '4'\n" +
                "                                            [zeroOrMore]\n" +
                "                            [')'] ')'\n" +
                "                [enforcedSequence, {3}] '*3'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [factor, {3}] '3'\n" +
                "                        [number, {3}] '3'\n" +
                "                            [oneOrMore] '3'\n" +
                "                                [digit] '3'\n" +
                "        [zeroOrMore]\n" +
                "    [eoi]\n",
                "ParseError: Invalid input ')', expected factor (line 1, pos 3):\n" +
                        "2*)(6-4)*3\n" +
                        "  ^\n"
        );
    }
}