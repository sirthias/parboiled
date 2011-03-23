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

package org.parboiled.examples.calculators;

import org.parboiled.Parboiled;
import org.parboiled.common.Predicates;
import org.parboiled.examples.TestNgParboiledTest;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.Filters;
import org.parboiled.support.ToStringFormatter;
import org.testng.annotations.Test;

import static org.parboiled.trees.GraphUtils.printTree;

public class CalculatorsTest extends TestNgParboiledTest<Object> {

    @Test
    public void testCalculator0() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser0.class);
        test(parser.InputLine(), "1+5")
                .hasNoErrors()
                .hasParseTree("" +
                        "[InputLine] '1+5'\n" +
                        "  [Expression] '1+5'\n" +
                        "    [Term] '1'\n" +
                        "      [Factor] '1'\n" +
                        "        [Number] '1'\n" +
                        "          [Digit] '1'\n" +
                        "      [ZeroOrMore]\n" +
                        "    [ZeroOrMore] '+5'\n" +
                        "      [Sequence] '+5'\n" +
                        "        [[+-]] '+'\n" +
                        "        [Term] '5'\n" +
                        "          [Factor] '5'\n" +
                        "            [Number] '5'\n" +
                        "              [Digit] '5'\n" +
                        "          [ZeroOrMore]\n" +
                        "  [EOI]\n");
    }

    @Test
    public void testCalculator1() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser1.class);
        runBasicCalculationTests(parser, "");
    }

    @Test
    public void testCalculator2() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser2.class);
        assertEquals(
                printTree(
                        (Matcher) parser.InputLine(),
                        new ToStringFormatter<Matcher>(),
                        Predicates.<Matcher>alwaysTrue(),
                        Filters.preventLoops()
                ), "" +
                        "InputLine\n" +
                        "  Expression\n" +
                        "    Term\n" +
                        "      Factor\n" +
                        "        Number\n" +
                        "          Digits\n" +
                        "            Digit\n" +
                        "          Number_Action1\n" +
                        "        Parens\n" +
                        "          '('\n" +
                        "          Expression\n" +
                        "          ')'\n" +
                        "      ZeroOrMore\n" +
                        "        Sequence\n" +
                        "          [*/]\n" +
                        "          Term_Action1\n" +
                        "          Factor\n" +
                        "          Term_Action2\n" +
                        "    ZeroOrMore\n" +
                        "      Sequence\n" +
                        "        [+-]\n" +
                        "        Expression_Action1\n" +
                        "        Term\n" +
                        "        Expression_Action2\n" +
                        "  EOI\n");

        runBasicCalculationTests(parser, "");
    }

    @Test
    public void testCalculator3() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser3.class);
        runBasicCalculationTests(parser, ".0");
        runExtendedCalculationTests(parser);
    }

    @Test
    public void testCalculator4() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser4.class);
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

    private void test(CalculatorParser parser, String input, String value) {
        String str = test(parser.InputLine(), input).hasNoErrors().result.parseTreeRoot.getValue().toString();
        int ix = str.indexOf('|');
        if (ix >= 0) str = str.substring(ix + 2);
        assertEquals(str, value);
    }

}