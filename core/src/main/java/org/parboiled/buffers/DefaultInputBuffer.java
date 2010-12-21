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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.support.Chars;

import java.util.Arrays;

/**
 * Immutable default implementation of an InputBuffer.
 */
public class DefaultInputBuffer implements InputBuffer {

    protected final int length;
    protected final char[] buffer;
    protected int[] newlines;

    /**
     * Constructs a new DefaultInputBuffer wrapping the given char array.
     * CAUTION: For performance reasons the given char array is not defensively copied.
     *
     * @param buffer the chars
     */
    public DefaultInputBuffer(char[] buffer) {
        checkArgNotNull(buffer, "buffer");
        this.buffer = buffer;
        this.length = buffer.length;
    }

    public char charAt(int index) {
        return 0 <= index && index < length ? buffer[index] : Chars.EOI;
    }

    public boolean test(int index, char[] characters) {
        int len = characters.length;
        if (index < 0 || index > length - len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buffer[index + i] != characters[i]) return false;
        }
        return true;
    }

    public String extract(int start, int end) {
        return extractInternal(start, end);
    }

    public Position getPosition(int index) {
        buildNewlines();
        int line = getLine0(newlines, index);
        int column = index - (line > 0 ? newlines[line - 1] : -1);
        return new Position(line + 1, column);
    }

    // returns the zero based input line number the character with the given index is found in
    protected static int getLine0(int[] newlines, int index) {
        int j = Arrays.binarySearch(newlines, index);
        return j >= 0 ? j : -(j + 1);
    }

    public String extractLine(int lineNumber) {
        buildNewlines();
        checkArgument(0 < lineNumber && lineNumber <= newlines.length + 1);
        int start = lineNumber > 1 ? newlines[lineNumber - 2] + 1 : 0;
        int end = lineNumber <= newlines.length ? newlines[lineNumber - 1] : length;
        if (charAt(end - 1) == '\r') end--;
        return extractInternal(start, end);
    }

    private String extractInternal(int start, int end) {
        if (start < 0) start = 0;
        if (end >= length) end = length;
        if (end <= start) return "";
        return new String(buffer, start, end - start);
    }

    public int getLineCount() {
        buildNewlines();
        return newlines.length + 1;
    }

    protected void buildNewlines() {
        if (newlines == null) {
            int count = 0, length = buffer.length;
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\n') {
                    count++;
                }
            }
            newlines = new int[count];
            count = 0;
            for (int i = 0; i < length; i++) {
                if (buffer[i] == '\n') {
                    newlines[count++] = i;
                }
            }
        }
    }

}

