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

public class RecursionTest extends AbstractTest {

    public static class RecursionParser extends BaseParser<Object, Actions<Object>> {

        @SuppressWarnings({"InfiniteRecursion"})
        public Rule lotsOfAs() {
            return sequence('A', optional(lotsOfAs()));
        }

    }

    @Test
    public void testRecursion() {
        RecursionParser parser = Parboiled.createParser(RecursionParser.class);
        test(parser, parser.lotsOfAs(), "AAA", "" +
                "[lotsOfAs] 'AAA'\n" +
                "    ['A'] 'A'\n" +
                "    [optional] 'AA'\n" +
                "        [lotsOfAs] 'AA'\n" +
                "            ['A'] 'A'\n" +
                "            [optional] 'A'\n" +
                "                [lotsOfAs] 'A'\n" +
                "                    ['A'] 'A'\n" +
                "                    [optional]\n");
    }

}