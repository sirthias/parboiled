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
import org.parboiled.errorhandling.RecoveringMatchHandler;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.support.ParseTreeUtils.printParseErrors;
import org.parboiled.support.ParsingResult;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import org.parboiled.trees.Filter;
import static org.testng.Assert.fail;

public abstract class AbstractTest {

    public <V> ParsingResult<V> test(BaseParser<V> parser, Rule rule, String input, String expectedTree) {
        ParsingResult<V> result = parser.parse(rule, input);
        if (result.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    printParseErrors(result.parseErrors, result.inputBuffer) +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(result)
            );
        }

        assertEqualsMultiline(printNodeTree(result), expectedTree);
        return result;
    }

    public <V> ParsingResult<V> testFail(BaseParser<V> parser, Rule rule, String input, String expectedErrors,
                                         String expectedTree) {
        return testFail(parser, rule, input, expectedErrors, expectedTree, null);
    }

    public <V> ParsingResult<V> testFail(BaseParser<V> parser, Rule rule, String input, String expectedErrors,
                                         String expectedTree, Filter filter) {
        ParsingResult<V> result = testFail(parser, rule, input, expectedErrors);
        assertEqualsMultiline(printNodeTree(result, filter), expectedTree);
        return result;
    }

    public <V> ParsingResult<V> testFail(BaseParser<V> parser, Rule rule, String input, String expectedErrors) {
        ParsingResult<V> result = parser.parse(rule, input, new RecoveringMatchHandler<V>());
        assertEqualsMultiline(printParseErrors(result.parseErrors, result.inputBuffer), expectedErrors);
        return result;
    }

}