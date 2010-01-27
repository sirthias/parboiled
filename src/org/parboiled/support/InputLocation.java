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
import org.parboiled.common.BitField;

/**
 * Immutable value container identifying a certain position in an InputBuffer.
 */
public class InputLocation {
    public final int index;
    public final int row;
    public final int column;
    public final char currentChar;
    public final BitField failedRules;

    public InputLocation(@NotNull InputBuffer inputBuffer, BitField failedRules) {
        this(inputBuffer, 0, 0, 0, failedRules);
    }

    private InputLocation(@NotNull InputBuffer inputBuffer, int index, int row, int column, BitField failedRules) {
        this.index = index;
        this.row = row;
        this.column = column;
        this.failedRules = failedRules;
        this.currentChar = inputBuffer.charAt(index);
    }

    public InputLocation advance(@NotNull InputBuffer inputBuffer) {
        if (currentChar == Chars.EOI) return this;
        int newRow, newColumn;
        if (currentChar == '\n') {
            newRow = row + 1;
            newColumn = 0;
        } else {
            newRow = row;
            newColumn = column + 1;
        }
        return new InputLocation(inputBuffer, index + 1, newRow, newColumn,
                failedRules != null ? new BitField(failedRules.getLength()) : null);
    }

    public char lookAhead(@NotNull InputBuffer inputBuffer, int delta) {
        return inputBuffer.charAt(index + delta);
    }

}

