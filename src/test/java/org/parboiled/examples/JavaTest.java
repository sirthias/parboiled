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

import org.parboiled.Parboiled;
import org.parboiled.ParserStatistics;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import org.parboiled.examples.java.JavaParser;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.FileUtils;
import org.parboiled.trees.Filters;
import org.testng.annotations.Test;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class JavaTest {

    @Test
    public void simpleJavaTest() {
        String testSource = FileUtils.readAllText("src/test/java/org/parboiled/examples/JavaTest.java");
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule compilationUnit = parser.CompilationUnit();

        assertEquals(ParserStatistics.generateFor(compilationUnit).toString(), "" +
                "Parser statistics for rule 'CompilationUnit':\n" +
                "    Total rules       : 681\n" +
                "        Actions       : 0\n" +
                "        Any           : 1\n" +
                "        CharIgnoreCase: 1\n" +
                "        Char          : 86\n" +
                "        Custom        : 0\n" +
                "        CharRange     : 9\n" +
                "        CharSet       : 16\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 64\n" +
                "        FirstOfStrings: 4\n" +
                "        OneOrMore     : 7\n" +
                "        Optional      : 40\n" +
                "        Sequence      : 309\n" +
                "        String        : 80\n" +
                "        Test          : 0\n" +
                "        TestNot       : 13\n" +
                "        ZeroOrMore    : 51\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 14\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 7\n");

        ParsingResult<Object> parsingResult = RecoveringParseRunner.run(compilationUnit, testSource);
        if (parsingResult.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult, Filters.skipEmptyOptionalsAndZeroOrMores())
            );
        }
        assertEqualsMultiline(
                printNodeTree(parsingResult, Filters.skipEmptyOptionalsAndZeroOrMores()),
                FileUtils.readAllTextFromResource("SimpleJavaTestParseTree.test")
        );
    }

}