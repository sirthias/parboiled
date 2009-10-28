/*
 * Copyright (C) 2009 Mathias Doenitz
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

public class CharactersTest {

    @Test
    public void testSimpleCharactersOps() {
        assertEquals(Characters.NONE.toString(), "[]");
        assertEquals(Characters.ONLY_EMPTY.toString(), "[EMPTY]");
        assertEquals(Characters.ALL.toString(), "![]");

        assertEquals(Characters.NONE.add('A').toString(), "[A]");
        assertEquals(Characters.ONLY_EMPTY.add('A').toString(), "[A,EMPTY]");
        assertEquals(Characters.ALL.add('A'), Characters.ALL);
        assertEquals(Characters.NONE.add(Chars.ANY), Characters.ALL_EXCEPT_EMPTY);
        assertEquals(Characters.ONLY_EMPTY.add(Chars.ANY), Characters.ALL);
        assertEquals(Characters.ALL.add(Chars.ANY), Characters.ALL);

        assertEquals(Characters.NONE.remove('A'), Characters.NONE);
        assertEquals(Characters.ONLY_EMPTY.remove('A'), Characters.ONLY_EMPTY);
        assertEquals(Characters.ALL.remove('A').toString(), "![A]");
        assertEquals(Characters.ONLY_EMPTY.remove(Chars.EMPTY), Characters.NONE);
    }

    @Test
    public void testMultiCharactersOps() {
        assertEquals(Characters.NONE.add(Characters.ALL), Characters.ALL);
        assertEquals(Characters.ALL.add(Characters.NONE), Characters.ALL);
        assertEquals(Characters.NONE.add(Characters.ONLY_EMPTY), Characters.ONLY_EMPTY);
        assertEquals(Characters.ONLY_EMPTY.add(Characters.NONE), Characters.ONLY_EMPTY);
        assertEquals(Characters.ONLY_EMPTY.add(Characters.ALL), Characters.ALL);
        assertEquals(Characters.ALL.add(Characters.ONLY_EMPTY), Characters.ALL);

        assertEquals(Characters.NONE.remove(Characters.ALL), Characters.NONE);
        assertEquals(Characters.ALL.remove(Characters.NONE), Characters.ALL);
        assertEquals(Characters.NONE.remove(Characters.ONLY_EMPTY), Characters.NONE);
        assertEquals(Characters.ONLY_EMPTY.remove(Characters.NONE), Characters.ONLY_EMPTY);
        assertEquals(Characters.ONLY_EMPTY.remove(Characters.ALL), Characters.NONE);
        assertEquals(Characters.ALL.remove(Characters.ONLY_EMPTY), Characters.allBut(Chars.EMPTY));

        assertEquals(Characters.of('A','B').add(Characters.of('B','C')), Characters.of('A','B','C'));
        assertEquals(Characters.allBut('A','B').add(Characters.of('B','C')), Characters.allBut('A'));
        assertEquals(Characters.of('A','B').add(Characters.allBut('B','C')), Characters.allBut('C'));
        assertEquals(Characters.allBut('A','B').add(Characters.allBut('B','C')), Characters.allBut('B'));

        assertEquals(Characters.of('A','B').remove(Characters.of('B','C')), Characters.of('A'));
        assertEquals(Characters.allBut('A','B').remove(Characters.of('B','C')), Characters.allBut('A', 'B', 'C'));
        assertEquals(Characters.of('A','B').remove(Characters.allBut('B','C')), Characters.of('B'));
        assertEquals(Characters.allBut('A','B').remove(Characters.allBut('B','C')), Characters.of('C'));
    }

}
