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
import org.parboiled.common.StringUtils;
import org.parboiled.Parboiled;

/**
 * (Almost) immutable value container identifying a certain position in an InputBuffer.
 */
public class InputLocation {
    public final int index;
    public final int row;
    public final int column;
    public final char currentChar;
    public InputLocation next;

    public InputLocation(@NotNull InputBuffer inputBuffer) {
        this(inputBuffer, 0, 0, 0);
    }

    private InputLocation(@NotNull InputBuffer inputBuffer, int index, int row, int column) {
        this(index, row, column, inputBuffer.charAt(index));
    }

    private InputLocation(int index, int row, int column, char currentChar) {
        this.index = index;
        this.row = row;
        this.column = column;
        this.currentChar = currentChar;
    }

    public InputLocation advance(@NotNull InputBuffer inputBuffer) {
        if (next != null) {
            return next;
        }
        switch (currentChar) {
            case '\n':
                return next = new InputLocation(inputBuffer, index + 1, row + 1, 0);
            case Parboiled.EOI:
                return next = this;
            default:
                return next = new InputLocation(inputBuffer, index + 1, row, column + 1);
        }
    }

    public char lookAhead(@NotNull InputBuffer inputBuffer, int delta) {
        return inputBuffer.charAt(index + delta);
    }

    @Override
    public String toString() {
        return String.format("#%s(%s,%s)'%s'", index, row, column, currentChar);
    }

    public InputLocation insertVirtualInput(char virtualChar) {
        InputLocation virtualLocation = new InputLocation(index, row, column, virtualChar);
        virtualLocation.next = this;
        return virtualLocation;
    }

    public InputLocation insertVirtualInput(String virtualText) {
        InputLocation virtualLocation = this;
        if (!StringUtils.isEmpty(virtualText)) {
            for (int i = virtualText.length() - 1; i >= 0; i--) {
                virtualLocation = virtualLocation.insertVirtualInput(virtualText.charAt(i));
            }
        }
        return virtualLocation;
    }

}

