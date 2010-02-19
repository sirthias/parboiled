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

import org.jetbrains.annotations.NotNull;
import org.parboiled.Parboiled;
import org.parboiled.common.StringUtils;

/**
 * Value container identifying a certain position in an InputBuffer.
 */
public class InputLocation {
    private int index;
    private int row;
    private int column;
    private char character;
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

    private void copyContentFrom(InputLocation location) {
        this.index = location.index;
        this.row = location.row;
        this.column = location.column;
        this.character = location.character;
    }

    /**
     * Removes the character represented by this input location from the input stream without changing the underlying
     * input buffer. If this input location represents EOI the method does nothing.
     * The method return an InputLocation object that serves as a container for the removed data. It can be used with
     * {@link #insert(InputLocation)} to reverse the removal.
     *
     * @param inputBuffer the underlying InputBuffer
     * @return an InputLocation containing the removed data
     */
    @NotNull
    public InputLocation remove(@NotNull InputBuffer inputBuffer) {
        if (next == null) {
            advance(inputBuffer);
        }
        if (next == this) {
            return this;
        }
        InputLocation saved = new InputLocation(index, row, column, character);
        copyContentFrom(next);
        this.next = next.next;
        return saved;
    }

    /**
     * Reverses the effect of a previous call to {@link #remove(InputBuffer)}.
     *
     * @param savedLocation the result of a previous call to {@link #remove(InputBuffer)}
     */
    public void insert(InputLocation savedLocation) {
        InputLocation nextLocation = new InputLocation(index, row, column, character);
        nextLocation.next = next;
        this.next = nextLocation;
        copyContentFrom(savedLocation);
    }

    /**
     * Inserts a virtual character into the input stream without changing the underlying InputBuffer.
     * The insertion can be reversed with a call to {@link #remove(InputBuffer)}.
     *
     * @param character the char to insert
     */
    public void insert(char character) {
        InputLocation nextLocation = new InputLocation(index, row, column, this.character);
        nextLocation.next = this.next;
        this.next = nextLocation;
        this.character = character;
    }

}

