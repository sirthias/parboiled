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

package org.parboiled.examples.java;

import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.ParserStatistics;
import org.parboiled.Rule;
import org.parboiled.common.*;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.Filters;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.testng.annotations.Test;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class JavaTest {

    // the following line is actually being uncommented programmatically before running the test,
    // the reason being that the IntelliJ IDEA parser does not like "funny" identifies like these itself
    //private char \u0041_identifierWithNonAsciiCharacters_åäöÅÄÖ_\u0030_$ = '\u0061';

    @SuppressWarnings("unused")
    private char[] octalEscapes = new char[] {'\1', '\12', '\123'};
	
    @Test
    public void simpleJavaTest() {
        String testSource = FileUtils.readAllTextFromResource("SimpleJavaTest.test");
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule compilationUnit = parser.CompilationUnit();

        assertEquals(ParserStatistics.generateFor(compilationUnit).toString(), "" +
                "Parser statistics for rule 'CompilationUnit':\n" +
                "    Total rules       : 682\n" +
                "        Actions       : 0\n" +
                "        Any           : 1\n" +
                "        CharIgnoreCase: 1\n" +
                "        Char          : 83\n" +
                "        Custom        : 2\n" +
                "        CharRange     : 7\n" +
                "        AnyOf         : 16\n" +
                "        Empty         : 0\n" +
                "        FirstOf       : 65\n" +
                "        FirstOfStrings: 4\n" +
                "        Nothing       : 0\n" +
                "        OneOrMore     : 7\n" +
                "        Optional      : 40\n" +
                "        Sequence      : 310\n" +
                "        String        : 82\n" +
                "        Test          : 0\n" +
                "        TestNot       : 13\n" +
                "        ZeroOrMore    : 51\n" +
                "\n" +
                "    Action Classes    : 0\n" +
                "    ProxyMatchers     : 14\n" +
                "    VarFramingMatchers: 0\n" +
                "MemoMismatchesMatchers: 7\n");

        ParsingResult<Object> parsingResult = new RecoveringParseRunner<Object>(compilationUnit).run(testSource);
        if (parsingResult.hasErrors()) {
            fail("\n--- ParseErrors ---\n" +
                    StringUtils.join(parsingResult.parseErrors, "---\n") +
                    "\n--- ParseTree ---\n" +
                    printNodeTree(parsingResult, Filters.SKIP_EMPTY_OPTS_AND_ZOMS, Predicates.<Node<Object>>alwaysTrue())
            );
        }
        assertEquals(
                printTree(parsingResult.parseTreeRoot, new ToStringFormatter<Node<Object>>(),
                        Filters.SKIP_EMPTY_OPTS_AND_ZOMS, Predicates.<Node<Object>>alwaysTrue()),
                FileUtils.readAllTextFromResource("SimpleJavaTestParseTree.test")
        );
    }

}
