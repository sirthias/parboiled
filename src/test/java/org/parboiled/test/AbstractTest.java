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

import org.parboiled.Node;
import org.parboiled.common.Predicate;
import org.parboiled.common.Predicates;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.Rule;
import org.parboiled.support.ParsingResult;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public abstract class AbstractTest {

    public  ParsingResult<?> test(Rule rule, String input, String expectedTree) {
        return test(RecoveringParseRunner.run(rule, input), expectedTree);
    }
    
    public  ParsingResult<?> testWithoutRecovery(Rule rule, String input, String expectedTree) {
        return test(ReportingParseRunner.run(rule, input), expectedTree);
    }
    
    private  ParsingResult<?> test(ParsingResult<?> result, String expectedTree) {
        if (result.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    printParseErrors(result) +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(result)
            );
        }

        assertEquals(printNodeTree(result), expectedTree);
        return result;
    }

    public <V> ParsingResult<V> testFail(Rule rule, String input, String expectedErrors,
                                         String expectedTree) {
        return testFail(rule, input, expectedErrors, expectedTree, Predicates.<Node<V>>alwaysTrue(),
                Predicates.<Node<V>>alwaysTrue());
    }

    public <V> ParsingResult<V> testFail(Rule rule, String input, String expectedErrors,
                                         String expectedTree, Predicate<Node<V>> nodeFilter,
                                         Predicate<Node<V>> subTreeFilter) {
        ParsingResult<V> result = testFail(rule, input, expectedErrors);
        assertEquals(printNodeTree(result, nodeFilter, subTreeFilter), expectedTree);
        return result;
    }

    public <V> ParsingResult<V> testFail(Rule rule, String input, String expectedErrors) {
        ParsingResult<V> result = RecoveringParseRunner.run(rule, input);
        assertEquals(printParseErrors(result), expectedErrors);
        return result;
    }

}