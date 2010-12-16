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

package org.parboiled.buffers;

import org.testng.annotations.Test;

import static org.parboiled.support.Chars.*;
import static org.testng.Assert.assertEquals;

public class IndentDedentInputBufferTest {

    @Test
    public void testIndentDedentInputBuffer() {
        InputBuffer buf = new IndentDedentInputBuffer(("" +
                "level 1\n" +
                "  \tlevel 2\n" +
                "    still level 2\n" +
                "\t    level 3\n" +
                "     also 3\n" +
                "    2 again\n" +
                "        another 3\n" +
                "and back to 1\n" +
                "  another level 2 again").toCharArray(), 2);
        String bufContent = getContent(buf);
        assertEquals(bufContent, "" +
                "level 1\n" +
                ">level 2\n" +
                "still level 2\n" +
                ">level 3\n" +
                "also 3\n" +
                "<2 again\n" +
                ">another 3\n" +
                "<<and back to 1\n" +
                ">another level 2 again\n" +
                "<");

        assertEquals(buf.charAt(13), 'l');
        assertEquals(buf.extract(9, 16), "level 2");
        assertEquals(buf.extract(69, 105), "and back to 1\n  another level 2 again");
        assertEquals(buf.getPosition(12), new InputBuffer.Position(2, 7));
        assertEquals(buf.extractLine(2), "  \tlevel 2");
        assertEquals(buf.getPosition(bufContent.length() - 1), new InputBuffer.Position(10, 1));
    }

    @Test
    public void testIndentDedentInputBuffer2() {
        InputBuffer buf = new IndentDedentInputBuffer(("" +
                "level 1\n" +
                "   \tlevel 2\n" +
                "back to 1\n" +
                "and one more").toCharArray(), 2);
        assertEquals(getContent(buf), "" +
                "level 1\n" +
                ">level 2\n" +
                "<back to 1\n" +
                "and one more");

        assertEquals(buf.extract(0, 30), "" +
                "level 1\n" +
                "   \tlevel 2\n" +
                "back to 1\n" +
                "and one more");
        assertEquals(buf.extract(18, 30), "back to 1\nand one more");
    }

    private String getContent(InputBuffer buf) {
        StringBuilder sb = new StringBuilder();
        int ix = 0;
        while (true) {
            char c = buf.charAt(ix++);
            if (c == EOI) break;
            sb.append(c);
        }

        return sb.toString().replace(INDENT, '>').replace(DEDENT, '<');
    }

}
