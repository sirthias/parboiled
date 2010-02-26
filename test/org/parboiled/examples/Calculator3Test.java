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

import org.parboiled.test.AbstractTest;
import org.parboiled.Parboiled;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.examples.calculator3.CalcNode;
import org.parboiled.examples.calculator3.CalculatorParser;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

public class Calculator3Test extends AbstractTest {

    private final CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);

    @Test
    public void test() {
        test("1 + 2 +3", 6);
        test("1-2.0- -3.5", 2.5);
        test("13+SQRT( 2*50 )^2 ", 113);
        test("-0.10 * 10^(3+1)", -1000);
        test("1-2*3^4/5+6", -25.4);
    }

    private void test(String input, double value) {
        ParsingResult<CalcNode> result = RecoveringParseRunner.run(parser.inputLine(), input);
        double resultValue = result.parseTreeRoot.getValue().getValue();
        assertFalse(result.hasErrors());
        assertEquals(resultValue, value);
    }

}