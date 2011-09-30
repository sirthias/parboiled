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

package org.parboiled.transform;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.MemoMismatches;
import org.parboiled.parserunners.ReportingParseRunner;
import org.testng.annotations.Test;

import static org.parboiled.transform.AsmTestUtils.getMethodInstructionList;
import static org.parboiled.transform.AsmTestUtils.verifyIntegrity;
import static org.testng.Assert.assertEquals;

public class BugIn101Test {

    static class Parser extends BaseParser<Object> {
        public Rule A() {
            Object a = new Object();
            return Sequence("a", push(a));
        }
        protected Rule B() {
            String b = "b";
            return Sequence("b", push(b));
        }
    }

    @Test
    public void verifyTestParserHierarchyExtension() throws Exception {
        // threw "java.lang.NoSuchFieldError: field$1" in 1.0.1
        new ReportingParseRunner<Object>(Parboiled.createParser(Parser.class).B()).run("b");
    }
}