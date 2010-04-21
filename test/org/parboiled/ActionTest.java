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

package org.parboiled;

import org.parboiled.annotations.Label;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ActionTest extends AbstractTest {

    public static class Actions extends BaseActions<Object> {

        public boolean addOne() {
            Integer i = (Integer) getContext().getNodeValue();
            getContext().setNodeValue(i + 1);
            return true;
        }
    }

    public static class Parser extends BaseParser<Integer> {

        final Actions actions = new Actions();

        public Rule A() {
            return Sequence(
                    'a',
                    set(42),
                    B(18),
                    stringAction("lastText:"+ lastText())
            );
        }

        @Label
        public Rule B(int i) {
            int j = i + 1;
            return Sequence(
                    'b',
                    set(timesTwo(i + j)),
                    C(),
                    set(value()) // no effect
            );
        }

        public Rule C() {
            return Sequence(
                    'c',
                    set(value()), // no effect
                    new Action() {
                        public boolean run(Context context) {
                            return getContext() == context;
                        }
                    },
                    D(1)
            );
        }

        @Label
        public Rule D(int i) {
            return Sequence(
                    'd', set(UP3(value())),
                    UP(set(i)),
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
        test(parser.A(), "abcd", "" +
                "[A, {42}] 'abcd'\n" +
                "    ['a'] 'a'\n" +
                "    [B, {74}] 'bcd'\n" +
                "        ['b'] 'b'\n" +
                "        [C, {1}] 'cd'\n" +
                "            ['c'] 'c'\n" +
                "            [D, {43}] 'd'\n" +
                "                ['d'] 'd'\n");

        ParserStatistics<Object> stats = ParserStatistics.generateFor(parser.A());
        assertEquals(stats.toString(), "" +
                "Parser statistics for rule 'A':\n" +
                "    Total rules       : 17\n" +
                "        Actions       : 9\n" +
                "        Any           : 0\n" +
                "        CharIgnoreCase: 0\n" +
                "        Char          : 4\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 0\n" +
                "        CharSet       : 0\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 0\n" +
                "        OneOrMore     : 0\n" +
                "        Optional      : 0\n" +
                "        Sequence      : 4\n" +
                "        Test          : 0\n" +
                "        TestNot       : 0\n" +
                "        ZeroOrMore    : 0\n" +
                "\n" +
                "    Action Classes    : 8\n" +
                "    Proxy Matchers    : 0\n");

        assertEquals(stats.printActionClassInstances(), "" +
                "Action classes and their instances for rule 'A':\n" +
                "    Action$26v2zgu0mt39t9Oä : D_Action1\n" +
                "    Action$4XIP43NSAsV9yk3d : B_Action1\n" +
                "    Action$7MnwChxVHD33TpLn : B_Action2, C_Action1\n" +
                "    Action$7jKZbcöfQRThImBV : D_Action3\n" +
                "    Action$AiIc8P8G0N8qeyX5 : A_Action2\n" +
                "    Action$Zvt3p5cXPY79KxMJ : D_Action2\n" +
                "    Action$tARpxBFUGHOSörOj : A_Action1\n" +
                "    and 1 anonymous instance(s)\n");
    }

}