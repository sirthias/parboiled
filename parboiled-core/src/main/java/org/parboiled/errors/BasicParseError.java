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

package org.parboiled.errors;

import static org.parboiled.common.Preconditions.*;
import org.parboiled.buffers.InputBuffer;

/**
 * A basic {@link ParseError} implementation for a one-char parse error with an optional error message.
 */
public class BasicParseError implements ParseError {
    private final InputBuffer inputBuffer;
    private final int startIndex;
    private final String errorMessage;
    private int endIndex;
    private int indexDelta;

    public BasicParseError(InputBuffer inputBuffer, int errorIndex, String errorMessage) {
        this.inputBuffer = checkArgNotNull(inputBuffer, "inputBuffer");
        this.startIndex = errorIndex;
        this.endIndex = errorIndex + 1;
        this.errorMessage = errorMessage;
    }

    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public int getStartIndex() {
        return startIndex + indexDelta;
    }

    public int getEndIndex() {
        return endIndex + indexDelta;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex - indexDelta;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getIndexDelta() {
        return indexDelta;
    }

    public void shiftIndexDeltaBy(int delta) {
        this.indexDelta += delta;
    }
}
