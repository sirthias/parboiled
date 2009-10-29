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

import static org.parboiled.TestUtils.assertEqualsMultiline;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import org.parboiled.utils.StringUtils;
import static org.testng.Assert.fail;

abstract class AbstractTest {

    public <V> void test(BaseParser<V, ?> parser, Rule rule, String input, String expectedTree) {
        ParsingResult<V> parsingResult = parser.parse(rule, input);
        if (!parsingResult.parseErrors.isEmpty()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult)
            );
        }

        String actualTree = printNodeTree(parsingResult);
        assertEqualsMultiline(actualTree, expectedTree);
    }

    public <V> void testFail(BaseParser<V, ?> parser, Rule rule, String input, String expectedTree,
                             String expectedErrors) {
        ParsingResult<V> parsingResult = parser.parse(rule, input);
        String actualTree = printNodeTree(parsingResult);
        assertEqualsMultiline(actualTree, expectedTree);
        assertEqualsMultiline(StringUtils.join(parsingResult.parseErrors, "---\n"), expectedErrors);
    }

}