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

package org.parboiled.support;

import org.parboiled.Parboiled;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class InputLocationTest {

    @Test
    public void testBasics() {
        InputBuffer buf = new InputBuffer("abcdefgh");
        InputLocation loc = new InputLocation(buf);

        assertEquals(readToEnd(buf, loc), "abcdefgh");
        assertEquals(readToEnd(buf, loc.advance(buf).advance(buf)), "cdefgh");

        loc.advance(buf).insertNext('X');
        assertEquals(readToEnd(buf, loc), "abXcdefgh");
        loc.advance(buf).advance(buf).advance(buf).advance(buf).insertNext('Y');
        assertEquals(readToEnd(buf, loc), "abXcdYefgh");
    }

    @Test
    public void testRemovalAndInsertion() {
        InputBuffer buf = new InputBuffer("abcdefgh");
        InputLocation loc = new InputLocation(buf);

        String original = "" +
                "#0(0,0)'a'\n" +
                "#1(0,1)'b'\n" +
                "#2(0,2)'c'\n" +
                "#3(0,3)'d'\n" +
                "#4(0,4)'e'\n" +
                "#5(0,5)'f'\n" +
                "#6(0,6)'g'\n" +
                "#7(0,7)'h'\n";

        assertEquals(getStream(buf, loc), original);

        InputLocation cursor = loc.advance(buf).advance(buf);
        InputLocation saved = cursor.removeNext();
        assertEquals(getStream(buf, loc), "" +
                "#0(0,0)'a'\n" +
                "#1(0,1)'b'\n" +
                "#2(0,2)'c'\n" +
                "#4(0,4)'e'\n" +
                "#5(0,5)'f'\n" +
                "#6(0,6)'g'\n" +
                "#7(0,7)'h'\n");

        cursor.insertNext(saved);
        assertEquals(getStream(buf, loc), original);
    }

    private String readToEnd(InputBuffer inputBuffer, InputLocation location) {
        StringBuilder sb = new StringBuilder();
        while (location.getChar() != Parboiled.EOI) {
            sb.append(location.getChar());
            location = location.advance(inputBuffer);
        }
        return sb.toString();
    }

    private String getStream(InputBuffer inputBuffer, InputLocation location) {
        StringBuilder sb = new StringBuilder();
        while (location.getChar() != Parboiled.EOI) {
            sb.append(location).append('\n');
            location = location.advance(inputBuffer);
        }
        return sb.toString();
    }

}
