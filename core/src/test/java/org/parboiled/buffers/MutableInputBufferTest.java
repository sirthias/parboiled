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

package org.parboiled.buffers;

import org.parboiled.support.Position;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MutableInputBufferTest {

    @Test
    public void testMutableInputBuffer() {
        MutableInputBuffer buf = new MutableInputBuffer(new DefaultInputBuffer(("" +
                "abcd\n" +
                "ef\r\n" +
                "\n" +
                "gh\n" +
                "\n").toCharArray()
        ));
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(5), 'e');
        assertEquals(buf.charAt(8), '\n');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1,1));
        assertEquals(buf.getPosition(1), new Position(1,2));
        assertEquals(buf.getPosition(2), new Position(1,3));
        assertEquals(buf.getPosition(3), new Position(1,4));
        assertEquals(buf.getPosition(4), new Position(1,5));
        assertEquals(buf.getPosition(5), new Position(2,1));
        assertEquals(buf.getPosition(6), new Position(2,2));
        assertEquals(buf.getPosition(7), new Position(2,3));
        assertEquals(buf.getPosition(8), new Position(2,4));
        assertEquals(buf.getPosition(9), new Position(3,1));
        assertEquals(buf.getPosition(10), new Position(4,1));
        assertEquals(buf.getPosition(11), new Position(4,2));
        assertEquals(buf.getPosition(12), new Position(4,3));
        assertEquals(buf.getPosition(13), new Position(5,1));

        // ************* INSERT FIRST CHAR ************
        buf.insertChar(6, 'X');
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(5), 'e');
        assertEquals(buf.charAt(6), 'X');
        assertEquals(buf.charAt(7), 'f');
        assertEquals(buf.charAt(12), 'h');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1,1));
        assertEquals(buf.getPosition(1), new Position(1,2));
        assertEquals(buf.getPosition(2), new Position(1,3));
        assertEquals(buf.getPosition(3), new Position(1,4));
        assertEquals(buf.getPosition(4), new Position(1,5));
        assertEquals(buf.getPosition(5), new Position(2,1));
        assertEquals(buf.getPosition(6), new Position(2,2));
        assertEquals(buf.getPosition(7), new Position(2,2));
        assertEquals(buf.getPosition(8), new Position(2,3));
        assertEquals(buf.getPosition(9), new Position(2,4));
        assertEquals(buf.getPosition(10), new Position(3,1));
        assertEquals(buf.getPosition(11), new Position(4,1));
        assertEquals(buf.getPosition(12), new Position(4,2));
        assertEquals(buf.getPosition(13), new Position(4,3));
        assertEquals(buf.getPosition(14), new Position(5,1));

        // ************* INSERT SECOND CHAR ************
        buf.insertChar(13, 'Y');
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(5), 'e');
        assertEquals(buf.charAt(6), 'X');
        assertEquals(buf.charAt(7), 'f');
        assertEquals(buf.charAt(13), 'Y');
        assertEquals(buf.charAt(14), '\n');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1,1));
        assertEquals(buf.getPosition(1), new Position(1,2));
        assertEquals(buf.getPosition(2), new Position(1,3));
        assertEquals(buf.getPosition(3), new Position(1,4));
        assertEquals(buf.getPosition(4), new Position(1,5));
        assertEquals(buf.getPosition(5), new Position(2,1));
        assertEquals(buf.getPosition(6), new Position(2,2));
        assertEquals(buf.getPosition(7), new Position(2,2));
        assertEquals(buf.getPosition(8), new Position(2,3));
        assertEquals(buf.getPosition(9), new Position(2,4));
        assertEquals(buf.getPosition(10), new Position(3,1));
        assertEquals(buf.getPosition(11), new Position(4,1));
        assertEquals(buf.getPosition(12), new Position(4,2));
        assertEquals(buf.getPosition(13), new Position(4,3));
        assertEquals(buf.getPosition(14), new Position(4,3));
        assertEquals(buf.getPosition(15), new Position(5,1));

        // ************* INSERT THIRD CHAR AT SAME POSITION AS FIRST CHAR************
        buf.insertChar(6, 'Z');
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(5), 'e');
        assertEquals(buf.charAt(6), 'Z');
        assertEquals(buf.charAt(7), 'X');
        assertEquals(buf.charAt(8), 'f');
        assertEquals(buf.charAt(14), 'Y');
        assertEquals(buf.charAt(15), '\n');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1,1));
        assertEquals(buf.getPosition(1), new Position(1,2));
        assertEquals(buf.getPosition(2), new Position(1,3));
        assertEquals(buf.getPosition(3), new Position(1,4));
        assertEquals(buf.getPosition(4), new Position(1,5));
        assertEquals(buf.getPosition(5), new Position(2,1));
        assertEquals(buf.getPosition(6), new Position(2,2));
        assertEquals(buf.getPosition(7), new Position(2,2));
        assertEquals(buf.getPosition(8), new Position(2,2));
        assertEquals(buf.getPosition(9), new Position(2,3));
        assertEquals(buf.getPosition(10), new Position(2,4));
        assertEquals(buf.getPosition(11), new Position(3,1));
        assertEquals(buf.getPosition(12), new Position(4,1));
        assertEquals(buf.getPosition(13), new Position(4,2));
        assertEquals(buf.getPosition(14), new Position(4,3));
        assertEquals(buf.getPosition(15), new Position(4,3));
        assertEquals(buf.getPosition(16), new Position(5,1));

        // ************* UNDO INSERTION OF FIRST CHAR************
        assertEquals(buf.undoCharInsertion(7), 'X');
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(5), 'e');
        assertEquals(buf.charAt(6), 'Z');
        assertEquals(buf.charAt(7), 'f');
        assertEquals(buf.charAt(13), 'Y');
        assertEquals(buf.charAt(14), '\n');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1,1));
        assertEquals(buf.getPosition(1), new Position(1,2));
        assertEquals(buf.getPosition(2), new Position(1,3));
        assertEquals(buf.getPosition(3), new Position(1,4));
        assertEquals(buf.getPosition(4), new Position(1,5));
        assertEquals(buf.getPosition(5), new Position(2,1));
        assertEquals(buf.getPosition(6), new Position(2,2));
        assertEquals(buf.getPosition(7), new Position(2,2));
        assertEquals(buf.getPosition(8), new Position(2,3));
        assertEquals(buf.getPosition(9), new Position(2,4));
        assertEquals(buf.getPosition(10), new Position(3,1));
        assertEquals(buf.getPosition(11), new Position(4,1));
        assertEquals(buf.getPosition(12), new Position(4,2));
        assertEquals(buf.getPosition(13), new Position(4,3));
        assertEquals(buf.getPosition(14), new Position(4,3));
        assertEquals(buf.getPosition(15), new Position(5,1));
    }

}
