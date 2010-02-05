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

import org.parboiled.examples.calculator.CalculatorParser;
import org.parboiled.support.Filters;
import static org.parboiled.support.ParseTreeUtils.printParseErrors;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.AbstractTest;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class ErrorTest extends AbstractTest {

    //@Test
    public void testReporting() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        Rule rule = parser.inputLine();
        ParsingResult<Integer> result = parser.parse(rule, "1+X2*(3-)-4/abc2+1");
        assertTrue(result.hasErrors());
        assertEqualsMultiline(printParseErrors(result.parseErrors, result.inputBuffer), "" +
                "Invalid input 'X', expected digit (line 1, pos 3):\n" +
                "1+X2*(3-)-4/abc2+1\n" +
                "  ^\n");
    }

    @Test
    public void testRecovery() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        Rule rule = parser.inputLine();
        testFail(parser, rule, "1+X2*(3-)-4/abc2+1", "" +
                "Invalid input 'X', expected term (line 1, pos 3):\n" +
                "1+X2*(3-)-4/abc2+1\n" +
                "  ^\n" +
                "---\n" +
                "Invalid input ')', expected term (line 1, pos 9):\n" +
                "1+X2*(3-)-4/abc2+1\n" +
                "        ^\n" +
                "---\n" +
                "Invalid input 'a', expected factor (line 1, pos 13):\n" +
                "1+X2*(3-)-4/abc2+1\n" +
                "            ^\n" +
                "---\n" +
                "Invalid input 'a', expected eoi (line 1, pos 13):\n" +
                "1+X2*(3-)-4/abc2+1\n" +
                "            ^\n", "" +
                "\n", Filters.SkipEmptyOptionalsAndZeroOrMores);
    }

}