/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

import org.parboiled.common.StringUtils;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import org.parboiled.support.ParsingResult;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class MemoizationTest {

    static class Parser extends BaseParser<Object> {

        public Rule clause() {
            return sequence(digit(), operator(), digit(), eoi());
        }

        public Rule operator() {
            return firstOf('+', '-');
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.clause();
        ParsingResult<?> parsingResult = parser.parse(rule, "1+5", Parboiled.MemoizeMismatches);
        if (parsingResult.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult)
            );
        }

        assertEqualsMultiline(printNodeTree(parsingResult), "" +
                "[clause] '1+5'\n" +
                "    [digit] '1'\n" +
                "    [operator] '+'\n" +
                "        ['+'] '+'\n" +
                "    [digit] '5'\n" +
                "    [eoi]\n");
    }

}