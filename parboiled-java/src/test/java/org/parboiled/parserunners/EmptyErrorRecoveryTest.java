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

package org.parboiled.parserunners;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class EmptyErrorRecoveryTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    public static class Parser extends BaseParser<Object> {

        Rule Clause() {
            return Sequence(Shout(), " cool", EOI);
        }
        
        Rule Clause2() {
            return Sequence(Shout(), EOI);
        }

        Rule Shout() {
            return FirstOf("Yes", "Yup");
        }
    }

    @Test
    public void testSingleRecovery() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "Y cool")
                .hasErrors("" +
                        "Invalid input, expected 'e' or 'u' (line 1, pos 2):\n" +
                        "Y cool\n" +
                        " ^\n");
        
        testWithRecovery(parser.Clause(), "Y")
                .hasErrors("" +
                        "Invalid input 'EOI', expected 'e' or 'u' (line 1, pos 2):\n" +
                        "Y\n" +
                        " ^\n");
        
        testWithRecovery(parser.Clause2(), "Y cool")
                .hasErrors("" +
                        "Invalid input ' ...', expected 'e' or 'u' (line 1, pos 2):\n" +
                        "Y cool\n" +
                        " ^^^^^\n");
        
        testWithRecovery(parser.Clause2(), "Y")
                .hasErrors("" +
                        "Invalid input 'EOI', expected 'e' or 'u' (line 1, pos 2):\n" +
                        "Y\n" +
                        " ^\n");
    }
}