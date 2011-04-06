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
import org.parboiled.support.Var;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class ActionVarTest extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    static class Parser extends BaseParser<Integer> {

        @SuppressWarnings( {"InfiniteRecursion"})
        public Rule A() {
            Var<List<String>> list = new Var<List<String>>(new ArrayList<String>());
            return Sequence('a', Optional(A(), list.get().add("Text"), push(list.get().size())));
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.A();

        test(rule, "aaaa")
                .hasNoErrors()
                .hasParseTree("" +
                        "[A, {1}] 'aaaa'\n" +
                        "  ['a'] 'a'\n" +
                        "  [Optional, {1}] 'aaa'\n" +
                        "    [Sequence, {1}] 'aaa'\n" +
                        "      [A, {1}] 'aaa'\n" +
                        "        ['a'] 'a'\n" +
                        "        [Optional, {1}] 'aa'\n" +
                        "          [Sequence, {1}] 'aa'\n" +
                        "            [A, {1}] 'aa'\n" +
                        "              ['a'] 'a'\n" +
                        "              [Optional, {1}] 'a'\n" +
                        "                [Sequence, {1}] 'a'\n" +
                        "                  [A] 'a'\n" +
                        "                    ['a'] 'a'\n" +
                        "                    [Optional]\n");
    }

}