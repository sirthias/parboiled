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
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.common.Reference;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class PrevCallsTest extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    static class Parser extends BaseParser<Integer> {

        @SuppressSubnodes
        public Rule Clause() {
            Reference<Integer> a = new Reference<Integer>();
            Reference<Character> op = new Reference<Character>();
            Reference<Integer> b = new Reference<Integer>();
            return Sequence(
                    Digits(), a.set(pop()),
                    Operator(), op.set(matchedChar()),
                    Digits(), b.set(pop()),
                    EOI,
                    push(op.get() == '+' ? a.get() + b.get() : a.get() - b.get())
            );
        }

        public Rule Operator() {
            return FirstOf('+', '-');
        }

        public Rule Digits() {
            return Sequence(
                    Digits2(),
                    debug()
            );
        }

        boolean debug() {
            return true;
        }

        public Rule Digits2() {
            return Sequence(
                    OneOrMore(CharRange('0', '9')),
                    push(Integer.parseInt(match()))
            );
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();
        test(rule, "100+23")
                .hasNoErrors()
                .hasParseTree("[Clause, {123}] '100+23'\n");
    }

}