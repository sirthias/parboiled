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

import org.parboiled.annotations.MemoMismatches;
import org.parboiled.parserunners.ProfilingParseRunner;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class MemoMismatchesTest {

    static class Parser extends BaseParser<Integer> {

        Rule Clause() {
            return Sequence(FirstOf(Zero(), One(), Two()), EOI);
        }

        Rule Zero() {
            return Sequence(TestNot(SevenOrNine()), Ch('0'));
        }

        Rule One() {
            return Sequence(TestNot(SevenOrNine()), Ch('1'));
        }

        Rule Two() {
            return Sequence(TestNot(SevenOrNine()), Ch('2'));
        }

        Rule SevenOrNine() {
            return FirstOf('7', '9');
        }

    }

    static class MemoParser extends Parser {
        @Override
        @MemoMismatches
        Rule SevenOrNine() {
            return super.SevenOrNine();
        }
    }

    @Test
    public void test1() {
        Parser parser = Parboiled.createParser(Parser.class);

        ParserStatistics stats = ParserStatistics.generateFor(parser.Clause());
        assertEquals(stats.toString(), "" +
                "Parser statistics for rule 'Clause':\n" +
                "    Total rules       : 13\n" +
                "        Actions       : 0\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 6\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 0\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 2\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 4\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 1\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 0\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 0\n");

        ProfilingParseRunner runner = new ProfilingParseRunner(parser.Clause());
        assertFalse(runner.run("2").hasErrors());
        assertEquals(runner.getReport().printBasics().replaceFirst("\\d\\.\\d\\d\\d s", "X.XXX s"), "" +
                "Runs                     :               1\n" +
                "Active rules             :              13\n" +
                "Total net rule time      :           X.XXX s\n" +
                "Total rule invocations   :              21\n" +
                "Total rule matches       :               8\n" +
                "Total rule mismatches    :              13\n" +
                "Total match share        :           38.10 %\n" +
                "Rule re-invocations      :               8\n" +
                "Rule re-matches          :               2\n" +
                "Rule re-mismatches       :               6\n" +
                "Rule re-invocation share :           38.10 %\n");
    }

    @Test
    public void test2() {
        MemoParser parser = Parboiled.createParser(MemoParser.class);

        ParserStatistics stats = ParserStatistics.generateFor(parser.Clause());
        assertEquals(stats.toString(), "" +
                "Parser statistics for rule 'Clause':\n" +
                "    Total rules       : 13\n" +
                "        Actions       : 0\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 6\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 0\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 2\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 4\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 1\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 0\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 1\n");

        ProfilingParseRunner runner = new ProfilingParseRunner(parser.Clause());
        assertFalse(runner.run("2").hasErrors());
        assertEquals(runner.getReport().printBasics().replaceFirst("\\d\\.\\d\\d\\d s", "X.XXX s"), "" +
                "Runs                     :               1\n" +
                "Active rules             :              13\n" +
                "Total net rule time      :           X.XXX s\n" +
                "Total rule invocations   :              17\n" +
                "Total rule matches       :               8\n" +
                "Total rule mismatches    :               9\n" +
                "Total match share        :           47.06 %\n" +
                "Rule re-invocations      :               4\n" +
                "Rule re-matches          :               2\n" +
                "Rule re-mismatches       :               2\n" +
                "Rule re-invocation share :           23.53 %\n");
    }

}