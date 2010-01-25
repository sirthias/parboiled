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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DisjointSet implementation for efficient management of union-partitions of n integers, running from 0 to n-1.
 */
public class DisjointIndexSet {

    // holds one value = data[i] for each integer i:
    // value <  0: i is representative of its set, -value is the size of its set
    // value >= 0: value is the parent of i in its set, following the parent chain will lead to the set representative
    private final int[] data;

    public DisjointIndexSet(int n) {
        data = new int[n];
        Arrays.fill(data, -1); // in the beginning we have n disjoint sets of size 1
    }

    public int size() {
        return data.length;
    }

    public int getRepresentative(int index) {
        Preconditions.checkElementIndex(index, data.length);
        int lookup = data[index];
        if (lookup < 0) return index; // index is representative of its set

        int representative = getRepresentative(lookup);
        if (representative != lookup) data[index] = representative; // path compression
        return representative;
    }

    public boolean inSameSet(int a, int b) {
        return getRepresentative(a) == getRepresentative(b);
    }

    public int merge(int a, int b) {
        int repA = getRepresentative(a);
        int repB = getRepresentative(b);

        if (repA == repB) return repA; // already in the same set

        // the ususal disjoint set implementation balances according to set size in order to achieve minimum,
        // inverse Ackerman, run time; however, we forego this tiny advantage for making sure that the
        // representative of a set is always its smallest member, which simplifies set treatment (like list building) 
        if (repA <= repB) {
            data[repA] += data[repB]; // increase set As size
            data[repB] = repA; // attach set B to set A
            return repA;
        } else {
            data[repB] += data[repA]; // increase set Bs size
            data[repA] = repB; // attach set A to set B
            return repB;
        }
    }

    public Map<Integer, int[]> getSubSets() {
        Map<Integer, int[]> sets = new LinkedHashMap<Integer, int[]>();
        int[] array, cursors = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            int rep, lookup = data[i];
            if (lookup < 0) {
                sets.put(rep = i, array = new int[-lookup]);
            } else {
                array = sets.get(rep = getRepresentative(lookup));
            }
            array[cursors[rep]++] = i;
        }
        return sets;
    }

}
