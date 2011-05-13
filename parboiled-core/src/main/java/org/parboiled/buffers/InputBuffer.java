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

/**
 * Abstraction of a simple char[] buffer holding the input text to be parsed.
 */
public interface InputBuffer {

    /**
     * Returns the character at the given index. If the index is invalid the method returns
     * {@link org.parboiled.support.Chars#EOI}.
     *
     * @param index the index
     * @return the character at the given index or Chars.EOI.
     */
    char charAt(int index);

    /**
     * Determines whether the characters starting at the given index match the ones from the given array (in order).
     *
     * @param index      the index into the input buffer where to start the comparison
     * @param characters the characters to test against the input buffer
     * @return true if matched
     */
    boolean test(int index, char[] characters);

    /**
     * Constructs a new {@link String} from all character between the given indices.
     * Invalid indices are automatically adjusted to their respective boundary.
     *
     * @param start the start index (inclusively)
     * @param end   the end index (exclusively)
     * @return a new String (non-interned)
     */
    String extract(int start, int end);
    
    /**
     * Constructs a new {@link String} from all character covered by the given IndexRange.
     *
     * @param range the IndexRange
     * @return a new String (non-interned)
     */
    String extract(IndexRange range);

    /**
     * Returns the line and column number of the character with the given index encapsulated in a
     * {@link org.parboiled.support.Position}
     * object. The very first character has the line number 1 and the column number 1.
     *
     * @param index the index of the character to get the line number of
     * @return the line number
     */
    Position getPosition(int index);

    /**
     * Translates the given index from the scope of this InputBuffer to the scope of the original, underlying char
     * array. The {@link DefaultInputBuffer} implementation simply returns the given index, but other implementations
     * like the {@link IndentDedentInputBuffer} or the {@link MutableInputBuffer} need to "undo" all compressions and
     * index shiftings performed internally in order to return the underlying index. 
     * 
     * @param index the index relative to this InputBuffer
     * @return the index relative to the underlying string or char array
     */
    int getOriginalIndex(int index);

    /**
     * Constructs a new {@link String} containing all characters with the given line number except for the trailing
     * newline.
     *
     * @param lineNumber the line number to get
     * @return the string
     */
    String extractLine(int lineNumber);

    /**
     * Returns the number of lines in the input buffer.
     *
     * @return number of lines in the input buffer.
     */
    int getLineCount();
}
