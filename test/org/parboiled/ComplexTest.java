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

package org.parboiled;

import org.testng.annotations.Test;

public class ComplexTest extends AbstractTest {

    public static class CalculatorParser extends BaseParser<CalculatorActions> {

        public CalculatorParser(CalculatorActions actions) {
            super(actions);
        }

        public Rule expression() {
            return enforcedSequence(
                    term(),
                    actions.setValue(value("term")),
                    eoi()
            );
        }

        public Rule term() {
            return sequence(
                    mult(),
                    zeroOrMore(
                            enforcedSequence(
                                    firstOf('+', '-'),
                                    mult()
                            )
                    ),
                    actions.compute(
                            value("mult", Integer.class),
                            chars("z/e/firstOf"),
                            values("z/e/mult", Integer.class)
                    )
            );
        }

        public Rule mult() {
            return sequence(
                    atom(),
                    zeroOrMore(
                            enforcedSequence(
                                    firstOf('*', '/'),
                                    atom()
                            )
                    ),
                    actions.compute(
                            value("atom", Integer.class),
                            chars("z/e/firstOf"),
                            values("z/e/atom", Integer.class)
                    )
            );
        }

        public Rule atom() {
            return sequence(
                    firstOf(
                            number(),
                            enforcedSequence('(', term(), ')').label("parens")
                    ),
                    actions.setValue(firstNonNull(value("f/number"), value("f/p/term")))
            );
        }

        public Rule number() {
            return sequence(
                    oneOrMore(digit()),
                    actions.setValue(convertToInteger(text("oneOrMore")))
            );
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    public static abstract class CalculatorActions extends BaseActions {

        public ActionResult compute(Integer firstValue, Character[] operators, Integer[] values) {
            int value = firstValue != null ? firstValue : 0;
            for (int i = 0; i < operators.length; i++) {
                if (operators[i] != null && values[i] != null) {
                    value = performOperation(value, operators[i], values[i]);
                }
            }
            return setValue(value);
        }

        private int performOperation(int value1, Character operator, Integer value2) {
            switch (operator) {
                case '+':
                    return value1 + value2;
                case '-':
                    return value1 - value2;
                case '*':
                    return value1 * value2;
                case '/':
                    return value1 / value2;
            }
            throw new IllegalStateException();
        }

    }

    @Test
    public void test() {
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.createActions(CalculatorActions.class)
        );

        test(parser.expression(), "1+5", "" +
                "[expression, {6}] '1+5'\n" +
                "    [term, {6}] '1+5'\n" +
                "        [mult, {1}] '1'\n" +
                "            [atom, {1}] '1'\n" +
                "                [firstOf] '1'\n" +
                "                    [number, {1}] '1'\n" +
                "                        [oneOrMore] '1'\n" +
                "                            [digit] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore] '+5'\n" +
                "            [enforcedSequence] '+5'\n" +
                "                [firstOf] '+'\n" +
                "                    ['+'] '+'\n" +
                "                [mult, {5}] '5'\n" +
                "                    [atom, {5}] '5'\n" +
                "                        [firstOf] '5'\n" +
                "                            [number, {5}] '5'\n" +
                "                                [oneOrMore] '5'\n" +
                "                                    [digit] '5'\n" +
                "                    [zeroOrMore]\n" +
                "    [eoi]\n");
    }

    @Test
    public void testSingleSymbolDeletion() {
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.createActions(CalculatorActions.class)
        );
        testFail(parser.expression(), "(8-6))", "" +
                "[expression, {2}] '(8-6))'\n" +
                "    [term, {2}] '(8-6)'\n" +
                "        [mult, {2}] '(8-6)'\n" +
                "            [atom, {2}] '(8-6)'\n" +
                "                [firstOf] '(8-6)'\n" +
                "                    [parens] '(8-6)'\n" +
                "                        ['('] '('\n" +
                "                        [term, {2}] '8-6'\n" +
                "                            [mult, {8}] '8'\n" +
                "                                [atom, {8}] '8'\n" +
                "                                    [firstOf] '8'\n" +
                "                                        [number, {8}] '8'\n" +
                "                                            [oneOrMore] '8'\n" +
                "                                                [digit] '8'\n" +
                "                                [zeroOrMore]\n" +
                "                            [zeroOrMore] '-6'\n" +
                "                                [enforcedSequence] '-6'\n" +
                "                                    [firstOf] '-'\n" +
                "                                        ['-'] '-'\n" +
                "                                    [mult, {6}] '6'\n" +
                "                                        [atom, {6}] '6'\n" +
                "                                            [firstOf] '6'\n" +
                "                                                [number, {6}] '6'\n" +
                "                                                    [oneOrMore] '6'\n" +
                "                                                        [digit] '6'\n" +
                "                                        [zeroOrMore]\n" +
                "                        [')'] ')'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore]\n" +
                "    [!ILLEGAL!] ')'\n" +
                "    [eoi]\n",
                "ParseError: Invalid input ')', expected eoi (line 1, pos 6):\n" +
                        "(8-6))\n" +
                        "     ^\n"
        );
    }

    @Test
    public void testSingleSymbolInsertion() {
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.createActions(CalculatorActions.class)
        );
        testFail(parser.expression(), "(8-6", "" +
                "[expression, {2}] '(8-6'\n" +
                "    [term, {2}] '(8-6'\n" +
                "        [mult, {2}] '(8-6'\n" +
                "            [atom, {2}] '(8-6'\n" +
                "                [firstOf] '(8-6'\n" +
                "                    [parens] '(8-6'\n" +
                "                        ['('] '('\n" +
                "                        [term, {2}] '8-6'\n" +
                "                            [mult, {8}] '8'\n" +
                "                                [atom, {8}] '8'\n" +
                "                                    [firstOf] '8'\n" +
                "                                        [number, {8}] '8'\n" +
                "                                            [oneOrMore] '8'\n" +
                "                                                [digit] '8'\n" +
                "                                [zeroOrMore]\n" +
                "                            [zeroOrMore] '-6'\n" +
                "                                [enforcedSequence] '-6'\n" +
                "                                    [firstOf] '-'\n" +
                "                                        ['-'] '-'\n" +
                "                                    [mult, {6}] '6'\n" +
                "                                        [atom, {6}] '6'\n" +
                "                                            [firstOf] '6'\n" +
                "                                                [number, {6}] '6'\n" +
                "                                                    [oneOrMore] '6'\n" +
                "                                                        [digit] '6'\n" +
                "                                        [zeroOrMore]\n" +
                "                        [')']\n" +
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
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.createActions(CalculatorActions.class)
        );
        testFail(parser.expression(), "2*xxx(4-1)", "" +
                "[expression, {1}] '2*xxx(4-1)'\n" +
                "    [term, {1}] '2*xxx(4-1'\n" +
                "        [mult, {2}] '2*xxx(4'\n" +
                "            [atom, {2}] '2'\n" +
                "                [firstOf] '2'\n" +
                "                    [number, {2}] '2'\n" +
                "                        [oneOrMore] '2'\n" +
                "                            [digit] '2'\n" +
                "            [zeroOrMore] '*xxx(4'\n" +
                "                [enforcedSequence] '*xxx(4'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [atom] 'xxx(4'\n" +
                "                        [firstOf]\n" +
                "                        [!ILLEGAL!] 'xxx(4'\n" +
                "        [zeroOrMore] '-1'\n" +
                "            [enforcedSequence] '-1'\n" +
                "                [firstOf] '-'\n" +
                "                    ['-'] '-'\n" +
                "                [mult, {1}] '1'\n" +
                "                    [atom, {1}] '1'\n" +
                "                        [firstOf] '1'\n" +
                "                            [number, {1}] '1'\n" +
                "                                [oneOrMore] '1'\n" +
                "                                    [digit] '1'\n" +
                "                    [zeroOrMore]\n" +
                "    [!ILLEGAL!] ')'\n" +
                "    [eoi]\n",
                "ParseError: Invalid input 'x', expected number or parens (line 1, pos 3):\n" +
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
        CalculatorParser parser = Parser.create(
                CalculatorParser.class,
                Parser.createActions(CalculatorActions.class)
        );
        testFail(parser.expression(), "2*)(6-4)*3", "" +
                "[expression, {12}] '2*)(6-4)*3'\n" +
                "    [term, {12}] '2*)(6-4)*3'\n" +
                "        [mult, {12}] '2*)(6-4)*3'\n" +
                "            [atom, {2}] '2'\n" +
                "                [firstOf] '2'\n" +
                "                    [number, {2}] '2'\n" +
                "                        [oneOrMore] '2'\n" +
                "                            [digit] '2'\n" +
                "            [zeroOrMore] '*)(6-4)*3'\n" +
                "                [enforcedSequence] '*)(6-4)'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [atom, {2}] ')(6-4)'\n" +
                "                        [!ILLEGAL!] ')'\n" +
                "                        [firstOf] '(6-4)'\n" +
                "                            [parens] '(6-4)'\n" +
                "                                ['('] '('\n" +
                "                                [term, {2}] '6-4'\n" +
                "                                    [mult, {6}] '6'\n" +
                "                                        [atom, {6}] '6'\n" +
                "                                            [firstOf] '6'\n" +
                "                                                [number, {6}] '6'\n" +
                "                                                    [oneOrMore] '6'\n" +
                "                                                        [digit] '6'\n" +
                "                                        [zeroOrMore]\n" +
                "                                    [zeroOrMore] '-4'\n" +
                "                                        [enforcedSequence] '-4'\n" +
                "                                            [firstOf] '-'\n" +
                "                                                ['-'] '-'\n" +
                "                                            [mult, {4}] '4'\n" +
                "                                                [atom, {4}] '4'\n" +
                "                                                    [firstOf] '4'\n" +
                "                                                        [number, {4}] '4'\n" +
                "                                                            [oneOrMore] '4'\n" +
                "                                                                [digit] '4'\n" +
                "                                                [zeroOrMore]\n" +
                "                                [')'] ')'\n" +
                "                [enforcedSequence] '*3'\n" +
                "                    [firstOf] '*'\n" +
                "                        ['*'] '*'\n" +
                "                    [atom, {3}] '3'\n" +
                "                        [firstOf] '3'\n" +
                "                            [number, {3}] '3'\n" +
                "                                [oneOrMore] '3'\n" +
                "                                    [digit] '3'\n" +
                "        [zeroOrMore]\n" +
                "    [eoi]\n",
                "ParseError: Invalid input ')', expected number or parens (line 1, pos 3):\n" +
                        "2*)(6-4)*3\n" +
                        "  ^\n"
        );
    }
}