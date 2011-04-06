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

package org.parboiled.examples.abc;

import org.parboiled.Parboiled;
import org.parboiled.examples.TestNgParboiledTest;
import org.testng.annotations.Test;

public class AbcTest extends TestNgParboiledTest<Object> {

    @Test
    public void test() {
        AbcParser parser = Parboiled.createParser(AbcParser.class);
        test(parser.S(), "aabbcc")
                .hasNoErrors()
                .hasParseTree("" +
                        "[S] 'aabbcc'\n" +
                        "  [OneOrMore] 'aa'\n" +
                        "    ['a'] 'a'\n" +
                        "    ['a'] 'a'\n" +
                        "  [B] 'bbcc'\n" +
                        "    ['b'] 'b'\n" +
                        "    [Optional] 'bc'\n" +
                        "      [B] 'bc'\n" +
                        "        ['b'] 'b'\n" +
                        "        [Optional]\n" +
                        "        ['c'] 'c'\n" +
                        "    ['c'] 'c'\n");
    }

    @Test
    public void testFail1() {
        AbcParser parser = Parboiled.createParser(AbcParser.class);
        testWithRecovery(parser.S(), "aabbbcc")
                .hasErrors("" +
                        "Invalid input 'b', expected 'c' (line 1, pos 5):\n" +
                        "aabbbcc\n" +
                        "    ^\n")
                .hasParseTree("" +
                        "[S]E 'aabbcc'\n" +
                        "  [OneOrMore] 'aa'\n" +
                        "    ['a'] 'a'\n" +
                        "    ['a'] 'a'\n" +
                        "  [B]E 'bbcc'\n" +
                        "    ['b'] 'b'\n" +
                        "    [Optional]E 'bc'\n" +
                        "      [B]E 'bc'\n" +
                        "        ['b'] 'b'\n" +
                        "        [Optional]\n" +
                        "        ['c'] 'c'\n" +
                        "    ['c'] 'c'\n"
                );
    }

    @Test
    public void testFail2() {
        AbcParser parser = Parboiled.createParser(AbcParser.class);
        testWithRecovery(parser.S(), "aabcc")
                .hasErrors("" +
                        "Invalid input 'c', expected 'b' (line 1, pos 4):\n" +
                        "aabcc\n" +
                        "   ^\n")
                .hasParseTree("" +
                        "[S]E 'aabbcc'\n" +
                        "  [OneOrMore] 'aa'\n" +
                        "    ['a'] 'a'\n" +
                        "    ['a'] 'a'\n" +
                        "  [B]E 'bbcc'\n" +
                        "    ['b'] 'b'\n" +
                        "    [Optional]E 'bc'\n" +
                        "      [B]E 'bc'\n" +
                        "        ['b']E 'b'\n" +
                        "        [Optional]\n" +
                        "        ['c'] 'c'\n" +
                        "    ['c'] 'c'\n"
                );
    }

}