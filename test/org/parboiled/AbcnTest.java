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

import org.testng.annotations.Test;

@SuppressWarnings({"InfiniteRecursion"})
public class AbcnTest extends AbstractTest {

    /**
     * The classic non-context free language example { a^n b^n c^n : n >= 1 }
     * S ← &(A c) a+ B !(a/b/c)
     * A ← a A? b
     * B ← b B? c
     */
    public static class TestParser extends BaseParser<Actions> {

        public TestParser(Actions actions) {
            super(actions);
        }

        public Rule S() {
            return sequence(
                    test(sequence(A(), 'c')),
                    oneOrMore('a'),
                    B(),
                    testNot(firstOf('a', 'b', 'c'))
            );
        }

        public Rule A() {
            return sequence('a', optional(A()), 'b');
        }

        public Rule B() {
            return sequence('b', optional(B()), 'c');
        }

    }

    @Test
    public void test() {
        TestParser parser = Parser.create(TestParser.class, null);
        test(parser.S(), "aabbcc", "" +
                "[S] 'aabbcc'\n" +
                "    [oneOrMore] 'aa'\n" +
                "        ['a'] 'a'\n" +
                "        ['a'] 'a'\n" +
                "    [B] 'bbcc'\n" +
                "        ['b'] 'b'\n" +
                "        [optional] 'bc'\n" +
                "            [B] 'bc'\n" +
                "                ['b'] 'b'\n" +
                "                [optional]\n" +
                "                ['c'] 'c'\n" +
                "        ['c'] 'c'\n");
    }

    @Test
    public void testFail() {
        TestParser parser = Parser.create(TestParser.class, null);
        testFail(parser.S(), "aabbbcc", "" +
                "[S] 'aabbbcc'\n" +
                "    [oneOrMore] 'aa'\n" +
                "        ['a'] 'a'\n" +
                "        ['a'] 'a'\n" +
                "    [B] 'bbbcc'\n" +
                "        ['b'] 'b'\n" +
                "        [optional] 'bbcc'\n" +
                "            [B] 'bbcc'\n" +
                "                ['b'] 'b'\n" +
                "                [optional] 'bc'\n" +
                "                    [B] 'bc'\n" +
                "                        ['b'] 'b'\n" +
                "                        [optional]\n" +
                "                        ['c'] 'c'\n" +
                "                ['c'] 'c'\n" +
                "        ['c']\n", "" +
                "ParseError: Invalid input 'b', expected 'c' (line 1, pos 5):\n" +
                "aabbbcc\n" +
                "    ^\n" +
                "---\n" +
                "ParseError: Invalid input EOI, expected 'c' (line 1, pos 8):\n" +
                "aabbbcc\n" +
                "       ^\n"
        );
    }

}