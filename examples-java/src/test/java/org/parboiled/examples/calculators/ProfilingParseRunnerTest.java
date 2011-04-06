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
import org.parboiled.examples.TestNgParboiledTest;
import org.parboiled.parserunners.ProfilingParseRunner;
import org.parboiled.test.ParboiledTest;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class ProfilingParseRunnerTest extends TestNgParboiledTest<Integer> {

    //@Test
    public void testProfiling() {
        CalculatorParser1 parser = Parboiled.createParser(CalculatorParser1.class);
        ProfilingParseRunner runner = new ProfilingParseRunner(parser.InputLine());
        assertFalse(runner.run("1+2*(3-4)").hasErrors());
        /*assertEquals(runner.getReport().printBasics(), "" +
                "Runs                     :               1\n" +
                "Active rules             :              28\n" +
                "Total net rule time      :           0.002 s\n" +
                "Total rule invocations   :              89\n" +
                "Total rule matches       :              50\n" +
                "Total rule mismatches    :              39\n" +
                "Total match share        :           56.18 %\n" +
                "Rule re-invocations      :               0\n" +
                "Rule re-matches          :               0\n" +
                "Rule re-mismatches       :               0\n" +
                "Rule re-invocation share :            0.00 %\n");*/
        assertEquals(runner.getReport().sortByInvocations().printTopRules(20, ProfilingParseRunner.Report.namedRules), "" +
                "Rule                           | Net-Time  |   Invocations   |     Matches     |   Mismatches    |   Time/Invoc.   | Match % |    Re-Invocs    |   Re-Matches    |   Re-Mismatch   |     Re-Invoc %    \n" +
                "-------------------------------|-----------|-----------------|-----------------|-----------------|-----------------|---------|-----------------|-----------------|-----------------|-------------------\n" +
                "Digit: CharRange               |      0 ms |     9  /     0  |     4  /     0  |     5  /     0  |          889 ns |  44.44% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "'*': Char                      |      0 ms |     5  /     0  |     1  /     0  |     4  /     0  |        1,000 ns |  20.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Number: Sequence               |      2 ms |     5  /    18  |     4  /    16  |     1  /     2  |      414,000 ns |  80.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "Factor: FirstOf                |      2 ms |     5  /    55  |     5  /    55  |     0  /     0  |      458,800 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "Digits: OneOrMore              |      1 ms |     5  /     9  |     4  /     8  |     1  /     1  |      175,200 ns |  80.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "'+': Char                      |      0 ms |     4  /     0  |     1  /     0  |     3  /     0  |        1,500 ns |  25.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Number_Action1: Action         |      1 ms |     4  /     0  |     4  /     0  |     0  /     0  |      286,250 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "'/': Char                      |      0 ms |     4  /     0  |     0  /     0  |     4  /     0  |          250 ns |   0.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Term: Sequence                 |      2 ms |     4  /    74  |     4  /    74  |     0  /     0  |      611,750 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "'-': Char                      |      0 ms |     3  /     0  |     1  /     0  |     2  /     0  |        1,000 ns |  33.33% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Expression: Sequence           |      3 ms |     2  /    86  |     2  /    86  |     0  /     0  |    1,286,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "Expression_Action1: Action     |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |       12,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "'(': Char                      |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |        4,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "')': Char                      |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |        4,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "InputLine: Sequence            |      2 ms |     1  /    88  |     1  /    88  |     0  /     0  |    2,435,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "Expression_Action2: Action     |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |       25,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Term_Action1: Action           |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |       13,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "EOI: Char                      |      0 ms |     1  /     0  |     1  /     0  |     0  /     0  |        4,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /    NaN%\n" +
                "Parens: Sequence               |      0 ms |     1  /    41  |     1  /    41  |     0  /     0  |      188,000 ns | 100.00% |     0  /     0  |     0  /     0  |     0  /     0  |   0.00% /   0.00%\n" +
                "Term_Action2: Action           |      0 ms |     0  /     0  |     0  /     0  |     0  /     0  |          NaN ns |    NaN% |     0  /     0  |     0  /     0  |     0  /     0  |    NaN% /    NaN%\n");
    }

}