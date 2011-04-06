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

package org.parboiled;

import org.parboiled.annotations.Label;
import org.parboiled.matchers.FirstOfMatcher;
import org.parboiled.matchers.Matcher;
import org.testng.annotations.Test;

import static org.parboiled.trees.GraphUtils.countAllDistinct;
import static org.testng.Assert.assertEquals;

public class CachingTest {

    public static class CachingParser extends BaseParser<Object> {

        public Rule Rule1() {
            return Sequence(
                    FirstOf('+', '-'),
                    Digit(),
                    FirstOf('+', '-'),
                    Digit()
            );
        }

        public Rule Rule2() {
            return Sequence(
                    FirstOf_uncached('+', '-'),
                    Digit(),
                    FirstOf_uncached('+', '-'),
                    Digit()
            );
        }

        public Rule Digit() {
            return CharRange('0', '9');
        }

        @Label("FirstOf")
        public Rule FirstOf_uncached(Object... rules) {
            return new FirstOfMatcher(toRules(rules));
        }

    }

    @Test
    public void testLabellingParser() {
        CachingParser parser = Parboiled.createParser(CachingParser.class);

        Matcher matcher1 = (Matcher) parser.Rule1();
        Matcher matcher2 = (Matcher) parser.Rule2();

        assertEquals(countAllDistinct(matcher1), 5);
        assertEquals(countAllDistinct(matcher2), 6);

        assertEquals(ParserStatistics.generateFor(parser.Rule1()).toString(), "" +
                "Parser statistics for rule 'Rule1':\n" +
                "    Total rules       : 5\n" +
                "        Actions       : 0\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 2\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 1\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 1\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 1\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 0\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 0\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 0\n");

        assertEquals(ParserStatistics.generateFor(parser.Rule2()).toString(), "" +
                "Parser statistics for rule 'Rule2':\n" +
                "    Total rules       : 6\n" +
                "        Actions       : 0\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 2\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 1\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 2\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 1\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 0\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 0\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 0\n");
    }

}