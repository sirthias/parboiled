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
import org.parboiled.Parboiled;
import org.parboiled.common.StringUtils;

/**
 * Value container identifying a certain position in an InputBuffer.
 */
public class InputLocation {
    private final int index;
    private final int row;
    private final int column;
    private final char character;
    private InputLocation next;

    public InputLocation(@NotNull InputBuffer inputBuffer) {
        this(inputBuffer, 0, 0, 0);
    }

    private InputLocation(@NotNull InputBuffer inputBuffer, int index, int row, int column) {
        this(index, row, column, inputBuffer.charAt(index));
    }

    private InputLocation(int index, int row, int column, char character) {
        this.index = index;
        this.row = row;
        this.column = column;
        this.character = character;
    }

    public int getIndex() {
        return index;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public char getChar() {
        return character;
    }

    public InputLocation getNext() {
        return next;
    }

    /**
     * Returns the input location after this one in the given InputBuffer.
     * If this is already the input location at EOI the method return this instance.
     *
     * @param inputBuffer the input buffer
     * @return the following input location or this if already at EOI
     */
    public InputLocation advance(@NotNull InputBuffer inputBuffer) {
        if (next != null) {
            return next;
        }
        switch (character) {
            case '\n':
                return next = new InputLocation(inputBuffer, index + 1, row + 1, 0);
            case Parboiled.EOI:
                return next = this;
            default:
                return next = new InputLocation(inputBuffer, index + 1, row, column + 1);
        }
    }

    @Override
    public String toString() {
        return String.format("#%s(%s,%s)'%s'", index, row, column, StringUtils.escape(character));
    }

    @NotNull
    public InputLocation removeAfter() {
        Preconditions.checkState(next != null, "removeAfter() should not be called on a fringe location");
        InputLocation oldNext = next;
        next = next.next;
        return oldNext;
    }

    public void insertAfter(@NotNull InputLocation nextLocation) {
        nextLocation.next = next;
        next = nextLocation;
    }

    /**
     * Inserts a virtual character into the input stream without changing the underlying InputBuffer.
     *
     * @param character the char to insert
     */
    public void insertAfter(char character) {
        Preconditions.checkState(next != null, "insertAfter(char) should not be called on a fringe location");
        insertAfter(next.insertBefore(character));
    }

    public InputLocation insertBefore(char character) {
        InputLocation predecessor = new InputLocation(index, row, column, character);
        predecessor.next = this;
        return predecessor;
    }

}

