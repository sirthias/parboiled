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

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import org.parboiled.Parboiled;

public class InputLocationTest {

    @Test
    public void testInputLocation() {
        InputBuffer buf = new InputBuffer("abcdefgh");

        InputLocation loc = new InputLocation(buf);

        assertEquals(readToEnd(buf, loc), "abcdefgh");
        assertEquals(readToEnd(buf, loc.advance(buf).advance(buf)), "cdefgh");
        assertEquals(loc.advance(buf).lookAhead(buf, 0), 'b');
        assertEquals(loc.advance(buf).lookAhead(buf, 1), 'c');
        assertEquals(loc.advance(buf).lookAhead(buf, 20), Parboiled.EOI);
        assertEquals(readToEnd(buf, loc.advance(buf).insertVirtualInput('X')), "Xbcdefgh");
        assertEquals(readToEnd(buf, loc.insertVirtualInput("XYZ")), "XYZabcdefgh");
    }

    private String readToEnd(InputBuffer inputBuffer, InputLocation location) {
        StringBuilder sb = new StringBuilder();
        while(location.currentChar != Parboiled.EOI) {
            sb.append(location.currentChar);
            location = location.advance(inputBuffer);
        }
        return sb.toString();
    }

}
