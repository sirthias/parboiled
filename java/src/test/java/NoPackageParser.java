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

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class NoPackageParser extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    public static class Parser extends BaseParser<Integer> {

        public Rule A() {
            return Sequence('a', push(42));
        }
    }

    @Test
    public void testNoPackageParser() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.A(), "a")
                .hasNoErrors()
                .hasParseTree("" +
                        "[A, {42}] 'a'\n" +
                        "  ['a'] 'a'\n");
    }
}