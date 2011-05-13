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
import org.parboiled.annotations.Label;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class ActionTest extends TestNgParboiledTest<Integer> {

    public static class Actions extends BaseActions<Integer> {

        public boolean addOne() {
            Integer i = getContext().getValueStack().pop();
            getContext().getValueStack().push(i + 1);
            return true;
        }
    }

    @BuildParseTree
    public static class Parser extends BaseParser<Integer> {

        final Actions actions = new Actions();

        public Rule A() {
            return Sequence(
                    'a',
                    push(42),
                    B(18),
                    stringAction("lastText:" + match())
            );
        }

        public Rule B(int i) {
            int j = i + 1;
            return Sequence(
                    'b',
                    push(timesTwo(i + j)),
                    C(),
                    push(pop()) // no effect
            );
        }

        public Rule C() {
            return Sequence(
                    'c',
                    push(pop()), // no effect
                    new Action() {
                        public boolean run(Context context) {
                            return getContext() == context;
                        }
                    },
                    D(1)
            );
        }

        @Label("Last")
        public Rule D(int i) {
            return Sequence(
                    'd', dup(),
                    push(i),
                    actions.addOne()
            );
        }

        public boolean stringAction(String string) {
            return "lastText:bcd".equals(string);
        }

        // ************* ACTIONS **************

        public int timesTwo(int i) {
            return i * 2;
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.A(), "abcd")
                .hasNoErrors()
                .hasParseTree("" +
                        "[A, {2}] 'abcd'\n" +
                        "  ['a'] 'a'\n" +
                        "  [B, {2}] 'bcd'\n" +
                        "    ['b', {42}] 'b'\n" +
                        "    [C, {2}] 'cd'\n" +
                        "      ['c', {74}] 'c'\n" +
                        "      [Last, {2}] 'd'\n" +
                        "        ['d', {74}] 'd'\n");

        ParserStatistics stats = ParserStatistics.generateFor(parser.A());
        assertEquals(stats.toString(), "" +
                "Parser statistics for rule 'A':\n" +
                "    Total rules       : 17\n" +
                "        Actions       : 9\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 4\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 0\n" +
                "        AnyOf         : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 0\n" +
                "        FirstOfStrings: 0\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 4\n" +
                "        String        : 0\n" +
                "        Test          : 0\n" +
                "        TestNot       : 0\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 8\n" +
                "    ProxyMatchers     : 0\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 0\n");

        assertEquals(stats.printActionClassInstances(), "" +
                "Action classes and their instances for rule 'A':\n" +
                "    Action$6cLpVbWbQq5ipg4L : A_Action2\n" +
                "    Action$C7Cct03iSioF6Y0I : D_Action1\n" +
                "    Action$CkakYAwr0N00UnJ1 : D_Action2\n" +
                "    Action$I0TRs0StgVDw2pzp : B_Action2, C_Action1\n" +
                "    Action$UUSxqpbGyJy9hEöE : A_Action1\n" +
                "    Action$j9fDPvQwCYftylRy : D_Action3\n" +
                "    Action$yNYbmROXF8hUKwhq : B_Action1\n" +
                "    and 1 anonymous instance(s)\n");
    }

}