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

import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.support.Var;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class VarFramingTest extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    static class Parser extends BaseParser<Integer> {

        int count = 1;

        @SuppressWarnings( {"InfiniteRecursion"})
        public Rule Clause() {
            Var<Integer> a = new Var<Integer>(-1);
            return Sequence(
                    Digits(), a.set(peek()),
                    SomeRule(a),
                    Optional(
                            '+',
                            Clause(), push(a.get())
                    )
            );
        }

        @SuppressNode
        public Rule Digits() {
            return Sequence(
                    OneOrMore(CharRange('0', '9')),
                    push(Integer.parseInt(match()))
            );
        }

        public Rule SomeRule(Var<Integer> var) {
            return toRule(var.get() == count++);
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();

        ParserStatistics stats = ParserStatistics.generateFor(rule);
        assertEquals(stats.toString(), "" +
                "Parser statistics for rule 'Clause':\n" +
                "    Total rules       : 11\n" +
                "        Actions       : 4\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 1\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 1\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 0\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 1\n" +
                "        Optional      : 1\n" +
                "        Sequence      : 3\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 0\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 4\n" +
                "    ProxyMatchers     : 1\n" +
                "    VarFramingMatchers: 1\n" +
                "MemoMismatchesMatchers: 0\n");

        test(rule, "1+2+3")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Clause, {1}] '1+2+3'\n" +
                        "  [Optional, {1}] '+2+3'\n" +
                        "    [Sequence, {1}] '+2+3'\n" +
                        "      ['+', {1}] '+'\n" +
                        "      [Clause, {2}] '2+3'\n" +
                        "        [Optional, {2}] '+3'\n" +
                        "          [Sequence, {2}] '+3'\n" +
                        "            ['+', {2}] '+'\n" +
                        "            [Clause, {3}] '3'\n" +
                        "              [Optional, {3}]\n");
    }

}