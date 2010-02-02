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

package org.parboiled.examples;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import org.parboiled.examples.java.JavaParser;
import org.parboiled.support.Filters;
import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.FileUtils;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class JavaTest {

    @SuppressWarnings({"UnusedDeclaration"})
    @Test
    public void test() {
        String testSource = FileUtils.readAllText("test/org/parboiled/examples/JavaTestSource.java");
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule compilationUnit = parser.compilationUnit();
        String tree = test(parser, compilationUnit, testSource);
        // System.out.println(tree);
    }

    public <V> String test(BaseParser<V> parser, Rule rule, String input) {
        ParsingResult<V> parsingResult = parser.parse(rule, input);
        if (parsingResult.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult, Filters.SkipEmptyOptionalsAndZeroOrMores)
            );
        }

        return printNodeTree(parsingResult, Filters.SkipEmptyOptionalsAndZeroOrMores);
    }

}