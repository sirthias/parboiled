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

package org.parboiled.matchers;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FirstOfStringsMatcherTest {

    @Test
    public void testCreateRecords() {
        StringBuilder sb = new StringBuilder();
        printRecord(FirstOfStringsMatcher.createRecord(0, toArrayOfCharArray("" +
                "Alpha",
                "Beta",
                "Bertram",
                "Claudia",
                "Charlie",
                "Delta",
                "Delto",
                "x")), "", sb);
        assertEquals(sb.toString(), "\n" +
                "Delt\n" +
                "    o\n" +
                "    a\n" +
                "Alpha\n" +
                "C\n" +
                " harlie\n" +
                " laudia\n" +
                "Be\n" +
                "  rtram\n" +
                "  ta\n" +
                "x\n");
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
