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

public class SimpleErrorRecoveryTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    public static class Parser extends BaseParser<Object> {

        Rule Clause() {
            return Sequence(Subject(), Verb(), Object(), EOI);
        }

        Rule Subject() {
            return Sequence(
                    Name(),
                    ZeroOrMore(FirstOf(" and ", " or "), Name())
            );
        }

        Rule Name() {
            return FirstOf("Alice", "Bob", "Charlie", "Doreen", "Emilio", "Ferdinand");
        }

        Rule Verb() {
            return FirstOf(" has ", " have ", " digs ", " dig ", " loves ", " love ", " hates ");
        }

        Rule Object() {
            return FirstOf("cats", "dogs", "animals", "cars", "building");
        }
    }

    @Test
    public void testRecovery() {
        Parser parser = Parboiled.createParser(Parser.class);
        testWithRecovery(parser.Clause(), "AaA")
                .hasErrors("" +
                        "Invalid input 'a...', expected 'l' (line 1, pos 2):\n" +
                        "AaA\n" +
                        " ^^\n" +
                        "---\n" +
                        "Invalid input 'EOI', expected \" and \", \" or \" or Verb (line 1, pos 4):\n" +
                        "AaA\n" +
                        "   ^\n");

        testWithRecovery(parser.Clause(), "Alice has anximals")
                .hasErrors("" +
                        "Invalid input 'x', expected 'i' (line 1, pos 13):\n" +
                        "Alice has anximals\n" +
                        "            ^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice has animals'\n" +
                        "  [Subject] 'Alice'\n" +
                        "    [Name] 'Alice'\n" +
                        "      [\"Alice\"] 'Alice'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb] ' has '\n" +
                        "    [\" has \"] ' has '\n" +
                        "  [Object]E 'animals'\n" +
                        "    [\"animals\"]E 'animals'\n" +
                        "  [EOI]\n");

        testWithRecovery(parser.Clause().suppressSubnodes(), "Alice has anximals")
                .hasErrors("" +
                        "Invalid input 'x', expected 'i' (line 1, pos 13):\n" +
                        "Alice has anximals\n" +
                        "            ^\n")
                .hasParseTree("[Clause]E 'Alice has animals'\n");

        testWithRecovery(parser.Clause(), "Alice has anmals")
                .hasErrors("" +
                        "Invalid input 'm', expected 'i' (line 1, pos 13):\n" +
                        "Alice has anmals\n" +
                        "            ^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice has animals'\n" +
                        "  [Subject] 'Alice'\n" +
                        "    [Name] 'Alice'\n" +
                        "      [\"Alice\"] 'Alice'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb] ' has '\n" +
                        "    [\" has \"] ' has '\n" +
                        "  [Object]E 'animals'\n" +
                        "    [\"animals\"]E 'animals'\n" +
                        "  [EOI]\n");

        testWithRecovery(parser.Clause(), "Alixyce has animals")
                .hasErrors("" +
                        "Invalid input 'x...', expected 'c' (line 1, pos 4):\n" +
                        "Alixyce has animals\n" +
                        "   ^^^^\n")
                .hasParseTree("" +
                        "[Clause]E 'Ali has animals'\n" +
                        "  [Subject]E 'Ali'\n" +
                        "    [Name]E 'Ali'\n" +
                        "      [\"Alice\"]E 'Ali'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb] ' has '\n" +
                        "    [\" has \"] ' has '\n" +
                        "  [Object] 'animals'\n" +
                        "    [\"animals\"] 'animals'\n" +
                        "  [EOI]\n");

        testWithRecovery(parser.Clause(), "Alicexy has animals")
                .hasErrors("" +
                        "Invalid input 'x...', expected \" and \", \" or \" or Verb (line 1, pos 6):\n" +
                        "Alicexy has animals\n" +
                        "     ^^^^^^^^^^^^^^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice'\n" +
                        "  [Subject] 'Alice'\n" +
                        "    [Name] 'Alice'\n" +
                        "      [\"Alice\"] 'Alice'\n" +
                        "    [ZeroOrMore]\n");

        testWithRecovery(parser.Clause(), "Alize has animals")
                .hasErrors("" +
                        "Invalid input 'z', expected 'c' (line 1, pos 4):\n" +
                        "Alize has animals\n" +
                        "   ^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice has animals'\n" +
                        "  [Subject]E 'Alice'\n" +
                        "    [Name]E 'Alice'\n" +
                        "      [\"Alice\"]E 'Alice'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb] ' has '\n" +
                        "    [\" has \"] ' has '\n" +
                        "  [Object] 'animals'\n" +
                        "    [\"animals\"] 'animals'\n" +
                        "  [EOI]\n");

        testWithRecovery(parser.Clause(), "Alice lofes animals")
                .hasErrors("" +
                        "Invalid input 'f', expected 'v' (line 1, pos 9):\n" +
                        "Alice lofes animals\n" +
                        "        ^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice loves animals'\n" +
                        "  [Subject] 'Alice'\n" +
                        "    [Name] 'Alice'\n" +
                        "      [\"Alice\"] 'Alice'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb]E ' loves '\n" +
                        "    [\" loves \"]E ' loves '\n" +
                        "  [Object] 'animals'\n" +
                        "    [\"animals\"] 'animals'\n" +
                        "  [EOI]\n");

        testWithRecovery(parser.Clause(), "Alixce and Emlio lofe animals")
                .hasErrors("" +
                        "Invalid input 'x', expected 'c' (line 1, pos 4):\n" +
                        "Alixce and Emlio lofe animals\n" +
                        "   ^\n" +
                        "---\n" +
                        "Invalid input 'l', expected 'i' (line 1, pos 14):\n" +
                        "Alixce and Emlio lofe animals\n" +
                        "             ^\n" +
                        "---\n" +
                        "Invalid input 'f', expected 'v' (line 1, pos 20):\n" +
                        "Alixce and Emlio lofe animals\n" +
                        "                   ^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice and Emilio love animals'\n" +
                        "  [Subject]E 'Alice and Emilio'\n" +
                        "    [Name]E 'Alice'\n" +
                        "      [\"Alice\"]E 'Alice'\n" +
                        "    [ZeroOrMore]E ' and Emilio'\n" +
                        "      [Sequence]E ' and Emilio'\n" +
                        "        [FirstOf] ' and '\n" +
                        "          [\" and \"] ' and '\n" +
                        "        [Name]E 'Emilio'\n" +
                        "          [\"Emilio\"]E 'Emilio'\n" +
                        "  [Verb]E ' love '\n" +
                        "    [\" love \"]E ' love '\n" +
                        "  [Object] 'animals'\n" +
                        "    [\"animals\"] 'animals'\n" +
                        "  [EOI]\n");
        
        testWithRecovery(parser.Clause(), "Alice has cars!!")
                .hasErrors("" +
                        "Invalid input '!...', expected EOI (line 1, pos 15):\n" +
                        "Alice has cars!!\n" +
                        "              ^^\n")
                .hasParseTree("" +
                        "[Clause]E 'Alice has cars'\n" +
                        "  [Subject] 'Alice'\n" +
                        "    [Name] 'Alice'\n" +
                        "      [\"Alice\"] 'Alice'\n" +
                        "    [ZeroOrMore]\n" +
                        "  [Verb] ' has '\n" +
                        "    [\" has \"] ' has '\n" +
                        "  [Object] 'cars'\n" +
                        "    [\"cars\"] 'cars'\n");
    }
    
    @Test(expectedExceptions = RecoveringParseRunner.TimeoutException.class)
    public void testRecoveryTimeout() {
        Parser parser = Parboiled.createParser(Parser.class);
        new RecoveringParseRunner<Object>(parser.Clause(), 0).run("AaA");
    }
}