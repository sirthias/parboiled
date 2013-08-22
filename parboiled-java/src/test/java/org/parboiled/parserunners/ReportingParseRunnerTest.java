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

package org.parboiled.parserunners;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.testng.Assert.assertEquals;

public class ReportingParseRunnerTest {

    public static class Parser extends BaseParser<Object> {
        Rule Line1() {
            return Sequence("Text;", OneOrMore(TestNot(';'), ANY), ';', EOI);
        }

        Rule Line2() {
            return Sequence('a', TestNot('b'), 'c', EOI);
        }
    }

    @Test
    public void testErrorLocation() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Line1();
        ParsingResult result = new ReportingParseRunner(rule).run("Text;;Something");
        assertEquals(result.parseErrors.size(), 1);
        assertEquals(printParseErrors(result), "" +
                "Invalid input ';' (line 1, pos 6):\n" +
                "Text;;Something\n" +
                "     ^\n");
    }

    @Test
    public void testErrorAtEOI() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Line1();
        ParsingResult result = new ReportingParseRunner(rule).run("Text;");
        assertEquals(result.parseErrors.size(), 1);
        assertEquals(printParseErrors(result), "" +
                "Unexpected end of input, expected ANY (line 1, pos 6):\n" +
                "Text;\n" +
                "     ^\n");
    }

    @Test
    public void testDoesntExpectTestNotMatcher() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Line2();
        ParsingResult result = new ReportingParseRunner(rule).run("ad");
        assertEquals(result.parseErrors.size(), 1);
        assertEquals(printParseErrors(result), "" +
                "Invalid input 'd', expected 'c' (line 1, pos 2):\n" +
                "ad\n" +
                " ^\n");
    }
}
