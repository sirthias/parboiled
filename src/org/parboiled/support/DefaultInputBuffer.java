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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Immutable default implementation of an InputBuffer.
 */
public class DefaultInputBuffer implements InputBuffer {

    private final int length;
    private final char[] buffer;
    private final int[] newlines;

    /**
     * The input text to create the InputBuffer from.
     *
     * @param inputText the text
     */
    public DefaultInputBuffer(@NotNull String inputText) {
        this(inputText.toCharArray());
    }

    /**
     * The input buffer to wrap.
     * CAUTION: For performance reasons the given char array is not defensively copied.
     *
     * @param buffer the chars
     */
    public DefaultInputBuffer(@NotNull char[] buffer) {
        this.buffer = buffer;
        this.length = buffer.length;
        this.newlines = extractNewlines(buffer);
    }

    private static int[] extractNewlines(char[] buffer) {
        int count = 0, length = buffer.length;
        for (int i = 0; i < length; i++) {
            if (buffer[i] == '\n') {
                count++;
            }
        }
        int[] newlines = new int[count];
        count = 0;
        for (int i = 0; i < length; i++) {
            if (buffer[i] == '\n') {
                newlines[count++] = i;
            }
        }
        return newlines;
    }

    public int getLength() {
        return length;
    }

    public char charAt(int index) {
        return 0 <= index && index < buffer.length ? buffer[index] : Characters.EOI;
    }

    public Position getPosition(int index) {
        Preconditions.checkArgument(0 <= index && index <= length); // also allow index "length" for EOI
        int j = Arrays.binarySearch(newlines, index);
        int line = j >= 0 ? j : -(j + 1);
        int column = index - (line > 0 ? newlines[line - 1] : -1);
        return new Position(line + 1, column);
    }

    public String extractLine(int lineNumber) {
        Preconditions.checkArgument(0 < lineNumber && lineNumber <= newlines.length + 1);
        int start = lineNumber > 1 ? newlines[lineNumber - 2] + 1 : 0;
        int end = lineNumber <= newlines.length ? newlines[lineNumber - 1] : length;
        if (charAt(end - 1) == '\r') end--;
        return extract(start, end);
    }

    @NotNull
    public String extract(int start, int end) {
        if (start < 0) start = 0;
        if (end >= buffer.length) end = buffer.length;
        if (end <= start) return "";
        return new String(buffer, start, end - start);
    }

}

