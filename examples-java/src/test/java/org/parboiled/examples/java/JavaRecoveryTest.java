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
import org.parboiled.common.FileUtils;
import org.parboiled.common.Predicates;
import org.parboiled.examples.TestNgParboiledTest;
import org.parboiled.support.Filters;
import org.testng.annotations.Test;

public class JavaRecoveryTest extends TestNgParboiledTest<Object> {

    @Test
    public void testJavaErrorRecovery() {
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        String[] tests = FileUtils.readAllTextFromResource("JavaErrorRecoveryTest.test")
                .split("###\r?\n");

        if (!runSingleTest(parser, tests)) {
            // no single, important test found, so run all tests
            for (String test : tests) {
                runTest(parser, test);
            }
        }
    }

    // if there is a test with its input starting with '>>>' only run that one
    private boolean runSingleTest(JavaParser parser, String[] tests) {
        for (String test : tests) {
            if (test.startsWith(">>>")) {
                runTest(parser, test.substring(3));
                return true;
            }
        }
        return false;
    }

    private void runTest(JavaParser parser, String test) {
        String[] s = test.split("===\r?\n");
        if (!s[0].startsWith("//")) {
            testWithRecovery(parser.CompilationUnit(), s[0])
                    .hasErrors(s[1])
                    .hasParseTree(Filters.SKIP_EMPTY_OPTS_AND_ZOMS, Predicates.<Node<Object>>alwaysTrue(), s[2]);
        }
    }

}