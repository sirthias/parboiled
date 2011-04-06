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
import org.parboiled.common.StringBuilderSink;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;
import org.parboiled.parserunners.TracingParseRunner;

import static org.parboiled.common.Predicates.*;
import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.support.Filters.rules;
import static org.parboiled.support.Filters.rulesBelow;
import static org.testng.Assert.assertEquals;

public class TracingParseRunnerTest {

    @Test
    public void testTracingParseRunner() {
        CalculatorParser1 parser = Parboiled.createParser(CalculatorParser1.class);

        StringBuilderSink log = new StringBuilderSink();
        TracingParseRunner<Integer> runner = new TracingParseRunner<Integer>(parser.InputLine())
                .withFilter(and(rules(parser.Number(), parser.Parens()), not(rulesBelow(parser.Digits()))))
                .withLog(log);
        ParsingResult<Integer> result = runner.run("2*(4+5");

        assertEquals(printParseErrors(result), "" +
                "Invalid input 'EOI', expected Digit, '*', '/', '+', '-' or ')' (line 1, pos 7):\n" +
                "2*(4+5\n" +
                "      ^\n");

        assertEquals(log.toString(), "Starting new parsing run\n" +
                "InputLine/Expression/Term/Factor/Number/Digits, matched, cursor at 1:2 after \"2\"\n" +
                "..(4)../Number/Number_Action1, matched, cursor at 1:2 after \"2\"\n" +
                "..(4)../Number, matched, cursor at 1:2 after \"2\"\n" +
                "..(2)../Term/ZeroOrMore/FirstOf/Sequence/Factor/Number/Digits, failed, cursor at 1:3 after \"2*\"\n" +
                "..(7)../Number, failed, cursor at 1:3 after \"2*\"\n" +
                "..(6)../Factor/Parens/'(', matched, cursor at 1:4 after \"2*(\"\n" +
                "..(7)../Parens/Expression/Term/Factor/Number/Digits, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(11)../Number/Number_Action1, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(11)../Number, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(10)../Factor, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(9)../Term/ZeroOrMore/FirstOf/Sequence/'*', failed, cursor at 1:5 after \"2*(4\"\n" +
                "..(12)../Sequence, failed, cursor at 1:5 after \"2*(4\"\n" +
                "..(11)../FirstOf/Sequence/'/', failed, cursor at 1:5 after \"2*(4\"\n" +
                "..(12)../Sequence, failed, cursor at 1:5 after \"2*(4\"\n" +
                "..(11)../FirstOf, failed, cursor at 1:5 after \"2*(4\"\n" +
                "..(10)../ZeroOrMore, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(9)../Term, matched, cursor at 1:5 after \"2*(4\"\n" +
                "..(8)../Expression/ZeroOrMore/FirstOf/Sequence/'+', matched, cursor at 1:6 after \"2*(4+\"\n" +
                "..(11)../Sequence/Term/Factor/Number/Digits, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(14)../Number/Number_Action1, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(14)../Number, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(13)../Factor, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(12)../Term/ZeroOrMore/FirstOf/Sequence/'*', failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(15)../Sequence, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(14)../FirstOf/Sequence/'/', failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(15)../Sequence, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(14)../FirstOf, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(13)../ZeroOrMore, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(12)../Term, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(11)../Sequence/Expression_Action1, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(11)../Sequence, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(10)../FirstOf, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(9)../ZeroOrMore/FirstOf/Sequence/'+', failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(11)../Sequence, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(10)../FirstOf/Sequence/'-', failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(11)../Sequence, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(10)../FirstOf, failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(9)../ZeroOrMore, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(8)../Expression, matched, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(7)../Parens/')', failed, cursor at 1:7 after \"2*(4+5\"\n" +
                "..(7)../Parens, failed, cursor at 1:7 after \"2*(4+5\"\n");
    }
}