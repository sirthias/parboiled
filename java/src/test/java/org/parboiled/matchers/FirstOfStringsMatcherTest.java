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

package org.parboiled.matchers;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.errors.GrammarException;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class FirstOfStringsMatcherTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    static class Parser extends BaseParser<Object> {

        public Rule Test1() {
            return FirstOf("Abc", "Ab", "Bcd");
        }

        public Rule Test2() {
            return FirstOf("Abc", "Abd", "Bcd");
        }

        public Rule Test3() {
            return FirstOf("Ab", "Abc", "Bcd");
        }
    }

    @Test
    public void testCreateRecords() {
        StringBuilder sb = new StringBuilder();
        printRecord(FirstOfStringsMatcher.createRecord(0, toArrayOfCharArray("" +
                "Alpha",
                "Beta",
                "Bertram",
                "Claudia",
                "ClaudiaVon",
                "Charlie",
                "Delta",
                "Delto",
                "x")), "", sb);
        assertEquals(sb.toString(), "\n" +
                "Alpha\n" +
                "Be\n" +
                "  rtram\n" +
                "  ta\n" +
                "C\n" +
                " harlie\n" +
                " laudia|Von\n" +
                "Delt\n" +
                "    a\n" +
                "    o\n" +
                "x\n");
    }

    @Test
    public void testFirstOfStringsMatcher() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.Test1(), "Abc").hasNoErrors().hasParseTree("[Test1] 'Abc'\n");
        test(parser.Test1(), "Ab").hasNoErrors().hasParseTree("[Test1] 'Ab'\n");
        test(parser.Test1(), "Bcd").hasNoErrors().hasParseTree("[Test1] 'Bcd'\n");

        test(parser.Test2(), "Abc").hasNoErrors().hasParseTree("[Test2] 'Abc'\n");
        test(parser.Test2(), "Abd").hasNoErrors().hasParseTree("[Test2] 'Abd'\n");
        test(parser.Test2(), "Bcd").hasNoErrors().hasParseTree("[Test2] 'Bcd'\n");
    }

    @Test(expectedExceptions = GrammarException.class)
    public void testFirstOfStringsFail() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.Test3(), "Abc").hasNoErrors().hasParseTree("");
    }

    private char[][] toArrayOfCharArray(String... strings) {
        char[][] chars = new char[strings.length][];
        for (int i = 0; i < strings.length; i++) {
            chars[i] = strings[i].toCharArray();
        }
        return chars;
    }

    private void printRecord(FirstOfStringsMatcher.Record rec, String indent, StringBuilder sb) {
        if (rec == null) {
            sb.append('\n');
            return;
        }
        if (rec.complete) sb.append('|');
        if (rec.chars.length == 1) {
            sb.append(rec.chars[0]);
            printRecord(rec.subs[0], indent + " ", sb);
        } else {
            sb.append('\n');
            for (int i = 0; i < rec.chars.length; i++) {
                sb.append(indent);
                sb.append(rec.chars[i]);
                printRecord(rec.subs[i], indent + " ", sb);
            }
        }
    }

}
