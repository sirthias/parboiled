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
import org.parboiled.common.StringUtils;

/**
 * (Almost) immutable descriptor for a certain position in an {@link InputBuffer}.
 * The InputLocations corresponding to one input text form a simple linked list.
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

    /**
     * @return the index of this location in the underlying {@link InputBuffer}
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the row number of this location, starting at 0
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column number of this location, starting at 0
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return the input character at this location
     */
    public char getChar() {
        return character;
    }

    /**
     * Returns the next input location if it has already been fetched, or null if this location is the last location
     * in the underlying {@link InputBuffer} that was looked at during the current parsing run.
     *
     * @return the next input location or null
     */
    public InputLocation getNext() {
        return next;
    }

    /**
     * Fetches and returns the input location after this one in the given {@link InputBuffer}.
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
            case Characters.EOI:
                return next = this;
            default:
                return next = new InputLocation(inputBuffer, index + 1, row, column + 1);
        }
    }

    /**
     * Returns the input character at a certain offset from the position of this InputLocation.
     * Note that this method does not honor virtual insertions/removals via {@link #removeNext()} or
     * {@link #insertNext(InputLocation)}/{@link #insertNext(char)} but rather works directly on the underlying
     * {@link InputBuffer}.
     * If the offset position is outside of the valid range of input positions the method return
     * {@link org.parboiled.support.Characters#EOI}.
     *
     * @param inputBuffer the underlying InputBuffer
     * @param delta       the position offset
     * @return the character at the offset position
     */
    public char lookAhead(@NotNull InputBuffer inputBuffer, int delta) {
        return inputBuffer.charAt(index + delta);
    }

    @Override
    public String toString() {
        return String.format("#%s(%s,%s)'%s'", index, row, column, StringUtils.escape(character));
    }

    /**
     * "Cuts out" and returns the next input location from the chain of InputLocations.
     * Neither this nor the next InputLocation must be the last InputLocation in the chain.
     * This operation does not change the underlying {@link InputBuffer}.
     *
     * @return the InputLocation that was cut out.
     */
    @NotNull
    public InputLocation removeNext() {
        Preconditions.checkState(next != null && next.next != null,
                "removeNext() should not be called on a fringe location or its immediate predecessor");
        InputLocation oldNext = next;
        next = next.next;
        return oldNext;
    }

    /**
     * Inserts the given InputLocation as the next input location into the chain.
     * Useful for reversing a previous {@link #removeNext()} operation.
     * This operation does not change the underlying {@link InputBuffer}.
     *
     * @param nextLocation the location to insert.
     */
    public void insertNext(@NotNull InputLocation nextLocation) {
        nextLocation.next = next;
        next = nextLocation;
    }

    /**
     * Inserts a virtual character into the input stream without changing the underlying {@link InputBuffer}.
     * This instance must not be the last InputLocation fetched so far.
     *
     * @param character the char to insert
     */
    public void insertNext(char character) {
        Preconditions.checkState(next != null, "insertNext(char) should not be called on a fringe location");
        InputLocation predecessor = new InputLocation(next.index, next.row, next.column, character);
        predecessor.next = next;
        insertNext(predecessor);
    }

}

