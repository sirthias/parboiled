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
import org.parboiled.common.Reference;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class ActionVar2Test extends TestNgParboiledTest<Object> {

    @BuildParseTree
    static class Parser extends BaseParser<Object> {

        Rule Clause() {
            Reference<Integer> count = new Reference<Integer>();
            return Sequence(CharCount(count), Chars(count), '\n');
        }

        Rule CharCount(Reference<Integer> count) {
            return Sequence('{', OneOrMore(CharRange('0', '9')), count.set(Integer.parseInt(match())), '}');
        }

        Rule Chars(Reference<Integer> count) {
            return Sequence(
                    ZeroOrMore(count.get() > 0, ANY, count.set(count.get() - 1)),
                    count.get() == 0
            );
        }
    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.Clause(), "{12}abcdefghijkl\n")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Clause] '{12}abcdefghijkl\\n'\n" +
                        "  [CharCount] '{12}'\n" +
                        "    ['{'] '{'\n" +
                        "    [OneOrMore] '12'\n" +
                        "      [0..9] '1'\n" +
                        "      [0..9] '2'\n" +
                        "    ['}'] '}'\n" +
                        "  [Chars] 'abcdefghijkl'\n" +
                        "    [ZeroOrMore] 'abcdefghijkl'\n" +
                        "      [Sequence] 'a'\n" +
                        "        [ANY] 'a'\n" +
                        "      [Sequence] 'b'\n" +
                        "        [ANY] 'b'\n" +
                        "      [Sequence] 'c'\n" +
                        "        [ANY] 'c'\n" +
                        "      [Sequence] 'd'\n" +
                        "        [ANY] 'd'\n" +
                        "      [Sequence] 'e'\n" +
                        "        [ANY] 'e'\n" +
                        "      [Sequence] 'f'\n" +
                        "        [ANY] 'f'\n" +
                        "      [Sequence] 'g'\n" +
                        "        [ANY] 'g'\n" +
                        "      [Sequence] 'h'\n" +
                        "        [ANY] 'h'\n" +
                        "      [Sequence] 'i'\n" +
                        "        [ANY] 'i'\n" +
                        "      [Sequence] 'j'\n" +
                        "        [ANY] 'j'\n" +
                        "      [Sequence] 'k'\n" +
                        "        [ANY] 'k'\n" +
                        "      [Sequence] 'l'\n" +
                        "        [ANY] 'l'\n" +
                        "  ['\\n'] '\\n'\n");
    }

}