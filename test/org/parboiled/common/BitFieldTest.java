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

package org.parboiled.common;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class BitFieldTest {

    @Test
    public void testConstruction() {
        BitField bf = new BitField(103);
        assertEquals(bf.getLength(), 103);
    }

    @Test
    public void testBitSettingAndClearing() {
        BitField bf = new BitField(46);
        assertTrue(bf.isAllClear());
        assertEquals(bf.get(41), false);

        bf.set(41);
        assertEquals(bf.get(41), true);
        assertFalse(bf.isAllClear());

        bf.setAll(true);
        assertTrue(bf.isAllSet());
        bf.setAll(false);
        assertFalse(bf.isAllSet());
    }

    @Test
    public void testBitwiseOperations() {
        BitField a = new BitField(234);
        BitField b = new BitField(234);

        a.or(BitField.not(b));
        assertTrue(a.isAllSet());

        a.xor(b);
        assertTrue(a.isAllSet());

        b.not();
        a.xor(b);
        assertFalse(a.isAllSet());

        a.and(b);
        assertFalse(a.isAllSet());
    }

    @Test
    public void testSubstract() {
        BitField a = new BitField(77);
        BitField b = new BitField(77);

        a.set(32);
        a.set(56);
        a.set(59);
        b.set(56);

        assertTrue(a.get(56));
        BitField s = BitField.substract(a, b);
        assertFalse(s.get(56));
    }
    
}
