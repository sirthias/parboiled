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

package org.parboiled.examples;

import org.parboiled.Parboiled;
import org.parboiled.examples.abc.AbcParser;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class AbcTest extends AbstractTest {

    @Test
    public void test() {
        AbcParser parser = Parboiled.createParser(AbcParser.class);
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
        AbcParser parser = Parboiled.createParser(AbcParser.class);
        testFail(parser.S(), "aabbbcc", "" +
                "Invalid input 'b', expected 'c' (line 1, pos 5):\n" +
                "aabbbcc\n" +
                "    ^\n" +
                "---\n" +
                "Invalid input 'EOI', expected 'c' (line 1, pos 8):\n" +
                "aabbbcc\n" +
                "       ^\n", "" +
                "[S] 'aabbbcc'\n" +
                "    [oneOrMore] 'aa'\n" +
                "        ['a'] 'a'\n" +
                "        ['a'] 'a'\n" +
                "    [B]\n" +
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
                "        [ILLEGAL]\n"
        );
    }

}