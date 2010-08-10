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

package org.parboiled;

import org.parboiled.annotations.BuildParseTree;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class SimpleErrorRecoveryTest extends AbstractTest {

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
            return FirstOf(" has ", " have ", " digs ", " dig ", " loves ", " love ", " hates ", " hates ");
        }

        Rule Object() {
            return FirstOf("cats", "dogs", "animals", "cars", "building");
        }

    }

    @Test
    public void testSingleCharRecovery() {
        Parser parser = Parboiled.createParser(Parser.class);
        testFail(parser.Clause(), "AaA", "" +
                "Invalid input 'a...', expected 'l' (line 1, pos 2):\n" +
                "AaA\n" +
                " ^^\n" +
                "---\n" +
                "Invalid input 'EOI', expected \" and \", \" or \" or Verb (line 1, pos 4):\n" +
                "AaA\n" +
                "   ^\n",
                "");

        testFail(parser.Clause(), "Alice has anximals", "" +
                "Invalid input 'x', expected 'i' (line 1, pos 13):\n" +
                "Alice has anximals\n" +
                "            ^\n", "" +
                "[Clause]E 'Alice has animals'\n" +
                "  [Subject] 'Alice'\n" +
                "    [Name] 'Alice'\n" +
                "      [\"Alice\"] 'Alice'\n" +
                "    [ZeroOrMore]\n" +
                "  [Verb] ' has '\n" +
                "    [\" has \"] ' has '\n" +
                "  [Object]E 'animals'\n" +
                "    [\"animals\"]E 'animals'\n" +
                "      ['i'] 'i'\n" +
                "  [EOI]\n");

        testFail(parser.Clause().suppressSubnodes(), "Alice has anximals", "" +
                "Invalid input 'x', expected 'i' (line 1, pos 13):\n" +
                "Alice has anximals\n" +
                "            ^\n", "" +
                "[Clause]E 'Alice has animals'\n" +
                "  [Object]E 'animals'\n" +
                "    [\"animals\"]E 'animals'\n" +
                "      ['i'] 'i'\n");

        testFail(parser.Clause(), "Alice has anmals", "" +
                "Invalid input 'm', expected 'i' (line 1, pos 13):\n" +
                "Alice has anmals\n" +
                "            ^\n", "" +
                "[Clause]E 'Alice has animals'\n" +
                "  [Subject] 'Alice'\n" +
                "    [Name] 'Alice'\n" +
                "      [\"Alice\"] 'Alice'\n" +
                "    [ZeroOrMore]\n" +
                "  [Verb] ' has '\n" +
                "    [\" has \"] ' has '\n" +
                "  [Object]E 'animals'\n" +
                "    [\"animals\"]E 'animals'\n" +
                "      ['i']E 'i'\n" +
                "  [EOI]\n");

        testFail(parser.Clause(), "Alixyce has animals", "" +
                "Invalid input 'x...', expected 'c' (line 1, pos 4):\n" +
                "Alixyce has animals\n" +
                "   ^^^^\n", "" +
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

        testFail(parser.Clause(), "Alicexy has animals", "" +
                "Invalid input 'x...', expected \" and \", \" or \" or Verb (line 1, pos 6):\n" +
                "Alicexy has animals\n" +
                "     ^^^^^^^^^^^^^^\n", "" +
                "[Clause]E 'Alice'\n" +
                "  [Subject] 'Alice'\n" +
                "    [Name] 'Alice'\n" +
                "      [\"Alice\"] 'Alice'\n" +
                "    [ZeroOrMore]\n");

        testFail(parser.Clause(), "Alize has animals", "" +
                "Invalid input 'z', expected 'c' (line 1, pos 4):\n" +
                "Alize has animals\n" +
                "   ^\n", "" +
                "[Clause]E 'Alice has animals'\n" +
                "  [Subject]E 'Alice'\n" +
                "    [Name]E 'Alice'\n" +
                "      [\"Alice\"]E 'Alice'\n" +
                "        ['c']E 'c'\n" +
                "    [ZeroOrMore]\n" +
                "  [Verb] ' has '\n" +
                "    [\" has \"] ' has '\n" +
                "  [Object] 'animals'\n" +
                "    [\"animals\"] 'animals'\n" +
                "  [EOI]\n");

        testFail(parser.Clause(), "Alice lofes animals", "" +
                "Invalid input 'f', expected 'v' (line 1, pos 9):\n" +
                "Alice lofes animals\n" +
                "        ^\n", "" +
                "[Clause]E 'Alice loves animals'\n" +
                "  [Subject] 'Alice'\n" +
                "    [Name] 'Alice'\n" +
                "      [\"Alice\"] 'Alice'\n" +
                "    [ZeroOrMore]\n" +
                "  [Verb]E ' loves '\n" +
                "    [\" loves \"]E ' loves '\n" +
                "      ['v']E 'v'\n" +
                "  [Object] 'animals'\n" +
                "    [\"animals\"] 'animals'\n" +
                "  [EOI]\n");

        testFail(parser.Clause(), "Alixce and Emlio lofe animals", "" +
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
                "                   ^\n", "" +
                "[Clause]E 'Alice and Emilio love animals'\n" +
                "  [Subject]E 'Alice and Emilio'\n" +
                "    [Name]E 'Alice'\n" +
                "      [\"Alice\"]E 'Alice'\n" +
                "        ['c'] 'c'\n" +
                "    [ZeroOrMore]E ' and Emilio'\n" +
                "      [Sequence]E ' and Emilio'\n" +
                "        [FirstOf] ' and '\n" +
                "          [\" and \"] ' and '\n" +
                "        [Name]E 'Emilio'\n" +
                "          [\"Emilio\"]E 'Emilio'\n" +
                "            ['i']E 'i'\n" +
                "  [Verb]E ' love '\n" +
                "    [\" love \"]E ' love '\n" +
                "      ['v']E 'v'\n" +
                "  [Object] 'animals'\n" +
                "    [\"animals\"] 'animals'\n" +
                "  [EOI]\n");
    }

}