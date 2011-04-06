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

public class ParserInheritanceTest extends TestNgParboiledTest<Object> {

    public static class Actions extends BaseActions<Object> {
        public boolean dummyAction() {
            return true;
        }
    }

    @BuildParseTree
    public static class ParentParser extends BaseParser<Object> {
        public Actions actions = new Actions();

        public Rule Abcd() {
            return Sequence("ab", "cd", actions.dummyAction());
        }

    }

    public static class DerivedParser extends ParentParser {
        public Rule Abcds() {
            return Sequence(OneOrMore(Abcd()), actions.dummyAction());
        }
    }

    @Test
    public void test() {
        ParentParser parentParser = Parboiled.createParser(ParentParser.class);
        test(parentParser.Abcd(), "abcd")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Abcd] 'abcd'\n" +
                        "  [\"ab\"] 'ab'\n" +
                        "  [\"cd\"] 'cd'\n");

        DerivedParser derivedParser = Parboiled.createParser(DerivedParser.class);
        Rule rule = derivedParser.Abcds();
        test(rule, "abcdabcd")
                .hasNoErrors()
                .hasParseTree("" +
                        "[Abcds] 'abcdabcd'\n" +
                        "  [OneOrMore] 'abcdabcd'\n" +
                        "    [Abcd] 'abcd'\n" +
                        "      [\"ab\"] 'ab'\n" +
                        "      [\"cd\"] 'cd'\n" +
                        "    [Abcd] 'abcd'\n" +
                        "      [\"ab\"] 'ab'\n" +
                        "      [\"cd\"] 'cd'\n");
    }

}