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

package org.parboiled.test;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.support.ParseTreeUtils.printParseErrors;
import org.parboiled.support.ParsingResult;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.fail;

public abstract class AbstractTest {

    public <V> void test(BaseParser<V> parser, Rule rule, String input, String expectedTree) {
        ParsingResult<V> parsingResult = parser.parse(rule, input);
        if (parsingResult.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult)
            );
        }

        String actualTree = printNodeTree(parsingResult);
        assertEqualsMultiline(actualTree, expectedTree);
    }

    public <V> void testFail(BaseParser<V> parser, Rule rule, String input, String expectedTree,
                             String expectedErrors) {
        ParsingResult<V> result = parser.parse(rule, input, true);
        String actualTree = printNodeTree(result);
        assertEqualsMultiline(printParseErrors(result.parseErrors, result.inputBuffer), expectedErrors);
        assertEqualsMultiline(actualTree, expectedTree);
    }

}