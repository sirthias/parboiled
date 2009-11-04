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

/**
 * Immutable value container identifying a certain position in an InputBuffer.
 */
public class InputLocation {
    public final int index;
    public final int row;
    public final int column;
    public final char currentChar;

    public InputLocation(@NotNull InputBuffer inputBuffer) {
        this(inputBuffer, 0, 0, 0);
    }

    private InputLocation(@NotNull InputBuffer inputBuffer, int index, int row, int column) {
        this.index = index;
        this.row = row;
        this.column = column;
        this.currentChar = inputBuffer.charAt(index);
    }

    public InputLocation advance(@NotNull InputBuffer inputBuffer) {
        switch (currentChar) {
            case Chars.EOI:
                return this;
            case '\n':
                return new InputLocation(inputBuffer, index + 1, row + 1, 0);
            default:
                return new InputLocation(inputBuffer, index + 1, row, column + 1);
        }
    }

    public char lookAhead(@NotNull InputBuffer inputBuffer, int delta) {
        return inputBuffer.charAt(index + delta);
    }

}

