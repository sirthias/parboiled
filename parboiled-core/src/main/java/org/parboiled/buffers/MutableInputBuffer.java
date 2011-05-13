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

import org.parboiled.support.IndexRange;
import org.parboiled.support.Position;

import java.util.Arrays;
import static org.parboiled.common.Preconditions.*;

/**
 * An InputBuffer wrapping another InputBuffer and providing for the ability to insert (and undo) characters at
 * certain index positions. Inserted chars do not appear in extracted text and have the same positions as the
 * original chars at their indices.
 * Note that this implementation is optimized for a rather small number of insertions and will perform badly with
 * a large number of insertions.
 */
public class MutableInputBuffer implements InputBuffer {
    private final InputBuffer buffer;
    private int[] inserts = new int[0];
    private char[] chars = new char[0];

    public MutableInputBuffer(InputBuffer buffer) {
        this.buffer = buffer;
    }

    public char charAt(int index) {
        int j = Arrays.binarySearch(inserts, index);
        if (j >= 0) return chars[j];
        return buffer.charAt(index + (j + 1));
    }

    public boolean test(int index, char[] characters) {
        throw new UnsupportedOperationException();
    }

    public Position getPosition(int index) {
        return buffer.getPosition(map(index));
    }

    public int getOriginalIndex(int index) {
        return buffer.getOriginalIndex(map(index));
    }

    public String extractLine(int lineNumber) {
        return buffer.extractLine(lineNumber);
    }

    public String extract(int start, int end) {
        return buffer.extract(map(start), map(end));
    }

    public String extract(IndexRange range) {
        return buffer.extract(map(range.start), map(range.end));
    }

    public int getLineCount() {
        return buffer.getLineCount();
    }

    private int map(int index) {
        int j = Arrays.binarySearch(inserts, index);
        if (j < 0) j = -(j + 1);
        return index - j;
    }

    public void insertChar(int index, char c) {
        int j = Arrays.binarySearch(inserts, index);
        if (j < 0) j = -(j + 1);

        char[] newChars = new char[chars.length + 1];
        System.arraycopy(chars, 0, newChars, 0, j);
        newChars[j] = c;
        System.arraycopy(chars, j, newChars, j + 1, chars.length - j);
        chars = newChars;

        int[] newInserts = new int[inserts.length + 1];
        System.arraycopy(inserts, 0, newInserts, 0, j);
        newInserts[j] = index;
        for (int i = j; i < inserts.length; i++) {
            newInserts[i + 1] = inserts[i] + 1;
        }
        inserts = newInserts;
    }

    public char undoCharInsertion(int index) {
        int j = Arrays.binarySearch(inserts, index);
        checkArgument(j >= 0, "Cannot undo a non-existing insertion");
        char removedChar = chars[j];

        char[] newChars = new char[chars.length - 1];
        System.arraycopy(chars, 0, newChars, 0, j);
        System.arraycopy(chars, j + 1, newChars, j, newChars.length - j);
        chars = newChars;

        int[] newInserts = new int[inserts.length - 1];
        System.arraycopy(inserts, 0, newInserts, 0, j);
        for (int i = j + 1; i < inserts.length; i++) {
            newInserts[i - 1] = inserts[i] - 1;
        }
        inserts = newInserts;
        return removedChar;
    }
    
    public void replaceInsertedChar(int index, char c) {
        int j = Arrays.binarySearch(inserts, index);
        checkArgument(j >= 0, "Can only replace chars that were previously inserted");
        chars[j] = c;
    }
}
