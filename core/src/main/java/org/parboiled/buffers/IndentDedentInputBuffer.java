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

import org.parboiled.common.IntArrayStack;
import org.parboiled.support.Chars;
import static org.parboiled.common.Preconditions.*;

/**
 * Special, immutable InputBuffer implementation for indentation based grammars.
 * This InputBuffer collapses all space and tab characters at the beginning of a text line into either nothing (if
 * the line has the same indentation level as the previous line), a special {@link Chars#INDENT} character (if the line
 * has a greater indentation level than the previous line) or one or more {@link Chars#DEDENT} characters (if the line
 * has a lower indentation level than the previous line).
 * This means that the highest index of this InputBuffer is probably smaller than that of the original input text
 * buffer, since all line indentation have been collapsed. However, the implementation will make sure that
 * {@link #getPosition(int)}, {@link #extract(int, int)}, etc. will work as expected and always return the "correct"
 * result from the underlying, original input buffer.
 */
public class IndentDedentInputBuffer extends DefaultInputBuffer {

    /* Implementation Note:
     * This implementation builds a second char[] with all line indentations collapsed into special INDENT / DEDENT
     * "characters", which is used as the primary parsing buffer. All text extraction methods etc. from the
     * DefaultInputBuffer that work on the original input text are reused by adding a small index translation layer.
     */

    protected final int length2;
    protected final char[] buffer2;
    protected int[] newlines2;
    protected final int tabStop;

    /**
     * Creates a new IndentDedentInputBuffer around the given char array. Note that for performance reasons the given
     * char array is not defensively copied.
     *
     * @param input   the input text.
     * @param tabStop the number of characters in a tab stop.
     */
    public IndentDedentInputBuffer(char[] input, int tabStop) {
        super(input);
        checkArgument(tabStop > 0, "tabStop must be > 0");
        this.tabStop = tabStop;
        buffer2 = buildBuffer2();
        length2 = buffer2.length;
    }

    @Override
    public char charAt(int index) {
        return 0 <= index && index < length2 ? buffer2[index] : Chars.EOI;
    }

    @Override
    public boolean test(int index, char[] characters) {
        int len = characters.length;
        if (index < 0 || index > length2 - len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (buffer2[index + i] != characters[i]) return false;
        }
        return true;
    }

    @Override
    public String extract(int start, int end) {
        return super.extract(translate(start), translate(end));
    }

    @Override
    public Position getPosition(int index) {
        return super.getPosition(translate(index));
    }

    // translate an index into buffer2 to the equivalent index into buffer

    protected int translate(int ix2) {
        ix2 = Math.min(Math.max(ix2, 0), length2); // also allow index "length" for EOI
        int line = getLine0(newlines2, ix2);
        int original = line >= newlines.length ? buffer.length : newlines[line];
        int adapted = newlines2[Math.min(line, newlines2.length - 1)];
        return ix2 + original - adapted;
    }

    protected char[] buildBuffer2() {
        StringBuilder sb = new StringBuilder();
        IntArrayStack previousLevels = new IntArrayStack();
        IntArrayStack newlines = new IntArrayStack();
        IntArrayStack newlines2 = new IntArrayStack();
        int length = buffer.length;
        int currentLevel = 0;
        int cursor = 0;
        previousLevels.push(0);

        // consume inital indent
        loop1:
        while (cursor < length) {
            switch (buffer[cursor]) {
                case ' ':
                    cursor++;
                    currentLevel++;
                    break;
                case '\t':
                    cursor++;
                    currentLevel = ((currentLevel / tabStop) + 1) * tabStop;
                    break;
                default:
                    break loop1;
            }
        }
        int indexDelta = currentLevel;

        // transform all other input
        while (cursor < length) {
            char c = buffer[cursor++];
            sb.append(c);
            if (c != '\n') continue;

            newlines.push(cursor - 1);
            newlines2.push(cursor - indexDelta - 1);

            // consume line indent
            int indent = 0;
            loop2:
            while (cursor < length) {
                switch (buffer[cursor]) {
                    case ' ':
                        cursor++;
                        indexDelta++;
                        indent++;
                        break;
                    case '\t':
                        cursor++;
                        indexDelta++;
                        indent = ((indent / tabStop) + 1) * tabStop;
                        break;
                    default:
                        break loop2;
                }
            }

            // generate INDENTS/DEDENTS
            if (indent > currentLevel) {
                previousLevels.push(currentLevel);
                currentLevel = indent;
                sb.append(Chars.INDENT);
                indexDelta--;
            } else {
                while (indent < currentLevel && indent <= previousLevels.peek()) {
                    currentLevel = previousLevels.pop();
                    sb.append(Chars.DEDENT);
                    indexDelta--;
                }
            }
        }

        // make sure to close all remaining indentation scopes
        if (previousLevels.size() > 1) {
            sb.append('\n');
            newlines.push(cursor);
            newlines2.push(cursor - indexDelta);
            while (previousLevels.size() > 1) {
                previousLevels.pop();
                sb.append(Chars.DEDENT);
            }
        }

        this.newlines = new int[newlines.size()];
        newlines.getElements(this.newlines, 0);

        this.newlines2 = new int[newlines2.size()];
        newlines2.getElements(this.newlines2, 0);

        char[] buffer = new char[sb.length()];
        sb.getChars(0, sb.length(), buffer, 0);

        return buffer;
    }

}

