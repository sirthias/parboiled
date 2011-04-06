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
import org.parboiled.Rule;
import org.parboiled.examples.TestNgParboiledTest;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.ParboiledTest;
import org.parboiled.parserunners.ReportingParseRunner;
import org.testng.annotations.Test;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.testng.Assert.assertEquals;

public class ReportingParseRunnerTest extends TestNgParboiledTest<Integer> {

    private CalculatorParser1 parser = Parboiled.createParser(CalculatorParser1.class);

    @Test
    public void testSimpleReporting() {
        test("X1+2", "Invalid input 'X', expected InputLine (line 1, pos 1):\nX1+2\n^\n");
        test("1X+2", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 2):\n1X+2\n ^\n");
        test("1+X2", "Invalid input 'X', expected Term (line 1, pos 3):\n1+X2\n  ^\n");
        test("1+2X", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 4):\n1+2X\n   ^\n");
        test("1+2X*(3-4)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 4):\n1+2X*(3-4)-5\n   ^\n");
        test("1+2*X(3-4)-5", "Invalid input 'X', expected Factor (line 1, pos 5):\n1+2*X(3-4)-5\n    ^\n");
        test("1+2*(X3-4)-5", "Invalid input 'X', expected Expression (line 1, pos 6):\n1+2*(X3-4)-5\n     ^\n");
        test("1+2*(3X-4)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 7):\n1+2*(3X-4)-5\n      ^\n");
        test("1+2*(3-X4)-5", "Invalid input 'X', expected Term (line 1, pos 8):\n1+2*(3-X4)-5\n       ^\n");
        test("1+2*(3-4X)-5", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 9):\n1+2*(3-4X)-5\n        ^\n");
        test("1+2*(3-4)X-5", "Invalid input 'X', expected '*', '/', '+', '-' or EOI (line 1, pos 10):\n1+2*(3-4)X-5\n         ^\n");
        test("1+2*(3-4)-X5", "Invalid input 'X', expected Term (line 1, pos 11):\n1+2*(3-4)-X5\n          ^\n");
        test("1+2*(3-4)-5X", "Invalid input 'X', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 12):\n1+2*(3-4)-5X\n           ^\n");
        test("1+2*(3-4-5", "Invalid input 'EOI', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 11):\n1+2*(3-4-5\n          ^\n");
        test("1+2*3-4)-5", "Invalid input ')', expected Digit, '*', '/', '+', '-' or EOI (line 1, pos 8):\n1+2*3-4)-5\n       ^\n");
    }

    private void test(String input, String expectedErrorMessage) {
        test(parser.InputLine(), input).hasErrors(expectedErrorMessage);
    }
}