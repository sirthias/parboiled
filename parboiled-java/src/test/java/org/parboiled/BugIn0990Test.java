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

import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class BugIn0990Test extends TestNgParboiledTest<Integer> {

    public static class Parser extends BaseParser<Integer> {
        Rule ID() {
            return Sequence('a', WhiteSpaceChar(), 'b');
        }

        Rule WhiteSpaceChar() {
            return AnyOf(" \n\r\t\f");
        }
    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.ID(), "ab")
                .hasErrors("" +
                        "Invalid input 'b', expected WhiteSpaceChar (line 1, pos 2):\n" +
                        "ab\n" +
                        " ^\n");
    }
}
