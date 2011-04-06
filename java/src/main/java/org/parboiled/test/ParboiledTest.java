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

package org.parboiled.test;

import org.parboiled.Node;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Predicate;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.util.*;

import static org.parboiled.errors.ErrorUtils.printParseErrors;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;

public abstract class ParboiledTest<V> {

    public class TestResult<V> {
        public final ParsingResult<V> result;

        public TestResult(ParsingResult<V> result) {
            this.result = result;
        }

        public TestResult<V> hasNoErrors() {
            if (result.hasErrors()) {
                fail("\n--- ParseErrors ---\n" +
                        printParseErrors(result) +
                        "\n--- ParseTree ---\n" +
                        printNodeTree(result)
                );
            }
            return this;
        }

        public TestResult<V> hasErrors(String expectedErrors) {
            assertEquals(printParseErrors(result), expectedErrors);
            return this;
        }

        public TestResult<V> hasParseTree(String expectedTree) {
            assertEquals(printNodeTree(result), expectedTree);
            return this;
        }

        public TestResult<V> hasParseTree(Predicate<Node<V>> nodeFilter, Predicate<Node<V>> subTreeFilter,
                                          String expectedTree) {
            assertEquals(printNodeTree(result, nodeFilter, subTreeFilter), expectedTree);
            return this;
        }

        public TestResult<V> hasResult(V... expectedResults) {
            assertEquals(toListReversed(result.valueStack), Arrays.asList(expectedResults));
            return this;
        }
        
        private <T> List<T> toListReversed(Iterable<T> iterable) {
            List<T> list = new ArrayList<T>();
            for (T t : iterable) list.add(t);
            Collections.reverse(list);
            return list;
        }
    }

    public TestResult<V> test(Rule rule, String input) {
        return new TestResult<V>(new ReportingParseRunner<V>(rule).run(input));
    }
    
    public TestResult<V> test(Rule rule, InputBuffer inputBuffer) {
        return new TestResult<V>(new ReportingParseRunner<V>(rule).run(inputBuffer));
    }

    public TestResult<V> testWithRecovery(Rule rule, String input) {
        return new TestResult<V>(new RecoveringParseRunner<V>(rule).run(input));
    }
    
    public TestResult<V> testWithRecovery(Rule rule, InputBuffer inputBuffer) {
        return new TestResult<V>(new RecoveringParseRunner<V>(rule).run(inputBuffer));
    }

    protected abstract void fail(String message);

    protected abstract void assertEquals(Object actual, Object expected);
}