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

public class RecoveryErrorActionsTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    public static class Parser extends BaseParser<Object> {

        Rule Clause() {
            return Sequence(Seq(), EOI);
        }

        Rule Seq() {
            return Sequence(A(), B(), C(), D());
        }

        Rule A() {
            return Sequence('a', push(match()));
        }

        Rule B() {
            return Sequence('b', push(match()));
        }

        Rule C() {
            return Sequence('c', push(1));
        }

        Rule D() {
            return Sequence('d', push(2.0));
        }
    }

    @Test
    public void testRecoveryErrorActions1() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "abcd")
                .hasNoErrors()
                .hasResult("a", "b", 1, 2.0);
    }

    @Test
    public void testRecoveryErrorActions2() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "axcd")
                .hasErrors("" +
                        "Invalid input 'x', expected B (line 1, pos 2):\n" +
                        "axcd\n" +
                        " ^\n")
                .hasResult("a", "b", 1, 2.0);
    }
    
    @Test
    public void testRecoveryErrorActions3() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "axx")
                .hasErrors("" +
                        "Invalid input 'x...', expected B (line 1, pos 2):\n" +
                        "axx\n" +
                        " ^^\n")
                .hasResult("a", "", 1, 2.0);
    }
    
    @Test
    public void testRecoveryErrorActions4() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "abx")
                .hasErrors("" +
                        "Invalid input 'x', expected C (line 1, pos 3):\n" +
                        "abx\n" +
                        "  ^\n")
                .hasResult("a", "b", 1, 2.0);
    }
    
    @Test
    public void testRecoveryErrorActions5() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "abxyz")
                .hasErrors("" +
                        "Invalid input 'x...', expected C (line 1, pos 3):\n" +
                        "abxyz\n" +
                        "  ^^^\n")
                .hasResult("a", "b", 1, 2.0);
    }

    @Test
    public void testRecoveryOnEmptyBuffer() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "")
                .hasErrors("" +
                        "Invalid input 'EOI', expected Clause (line 1, pos 1):\n" +
                        "\n" +
                        "^\n")
                .hasParseTree("[Clause]E\n")
                .hasResult("", "", 1, 2.0);
    }
}