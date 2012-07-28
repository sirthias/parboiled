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

public class NTimesTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    static class Parser extends BaseParser<Object> {

        public Rule Clause() {
            return NTimes(3, FourDigits(), Operator());
        }

        public Rule Operator() {
            return FirstOf('+', '-');
        }

        public Rule FourDigits() {
            return NTimes(4, CharRange('0', '9'));
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();
        test(rule, "1234+2345-3456")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Clause] '1234+2345-3456'\n" +
                        "  [FourDigits] '1234'\n" +
                        "    [0..9] '1'\n" +
                        "    [0..9] '2'\n" +
                        "    [0..9] '3'\n" +
                        "    [0..9] '4'\n" +
                        "  [Operator] '+'\n" +
                        "    ['+'] '+'\n" +
                        "  [FourDigits] '2345'\n" +
                        "    [0..9] '2'\n" +
                        "    [0..9] '3'\n" +
                        "    [0..9] '4'\n" +
                        "    [0..9] '5'\n" +
                        "  [Operator] '-'\n" +
                        "    ['-'] '-'\n" +
                        "  [FourDigits] '3456'\n" +
                        "    [0..9] '3'\n" +
                        "    [0..9] '4'\n" +
                        "    [0..9] '5'\n" +
                        "    [0..9] '6'\n");
    }

}
