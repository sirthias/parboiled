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
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class SimpleTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    static class Parser extends BaseParser<Object> {

        public Rule Clause() {
            return Sequence(Digit(), Operator(), Digit(), AnyOf("abcd"), OneOrMore(NoneOf("abcd")), EOI);
        }

        public Rule Operator() {
            return FirstOf(Ch('+'), '-');
        }

        public Rule Digit() {
            return CharRange('0', '9');
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();
        test(rule, "1+5bx")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Clause] '1+5bx'\n" +
                        "  [Digit] '1'\n" +
                        "  [Operator] '+'\n" +
                        "    ['+'] '+'\n" +
                        "  [Digit] '5'\n" +
                        "  [[abcd]] 'b'\n" +
                        "  [OneOrMore] 'x'\n" +
                        "    [![abcdEOI]] 'x'\n" +
                        "  [EOI]\n");
    }

}
