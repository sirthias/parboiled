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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.Arrays;

public class DisjointIndexSetTest {

    @Test
    public void test() {
        DisjointIndexSet set = new DisjointIndexSet(10);
        for (int i = 0; i < set.size(); i++) {
            assertEquals(set.getRepresentative(i), i);
        }

        set.merge(2, 8);
        assertEquals(set.getRepresentative(8), 2);

        set.merge(8, 4);
        assertEquals(set.getRepresentative(4), 2);

        set.merge(1, 4);
        assertEquals(set.getRepresentative(8), 1);
        assertEquals(set.getRepresentative(4), 1);
        assertEquals(set.getRepresentative(2), 1);

        set.merge(0, 9);
        set.merge(0, 5);

        Map<Integer, int[]> map = set.getSubSets();
        assertTrue(Arrays.equals(map.get(0), new int[] {0, 5, 9}));
        assertTrue(Arrays.equals(map.get(1), new int[] {1, 2, 4, 8}));
        assertTrue(Arrays.equals(map.get(3), new int[] {3}));
        assertTrue(Arrays.equals(map.get(6), new int[] {6}));
        assertTrue(Arrays.equals(map.get(7), new int[] {7}));
        assertEquals(map.size(), 5);
    }

}
