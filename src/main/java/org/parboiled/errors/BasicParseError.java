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

package org.parboiled.errors;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.buffers.InputBuffer;

/**
 * A basic {@link ParseError} implementation for a one-char parse error with an optional error message.
 */
public class BasicParseError implements ParseError {

    private final InputBuffer inputBuffer;
    private int startIndex;
    private int endIndex;
    private String errorMessage;

    public BasicParseError(@NotNull InputBuffer inputBuffer, int errorIndex, String errorMessage) {
        this.inputBuffer = inputBuffer;
        this.startIndex = errorIndex;
        this.endIndex = errorIndex + 1;
        this.errorMessage = errorMessage;
    }

    @NotNull
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets the end index of this error. Must be greater than the start index.
     *
     * @param startIndex the start index
     */
    public void setStartIndex(int startIndex) {
        Preconditions.checkArgument(startIndex >= 0);
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Sets the end index of this error. Must be greater than the start index.
     *
     * @param endIndex the end index
     */
    public void setEndIndex(int endIndex) {
        Preconditions.checkArgument(endIndex > getStartIndex());
        this.endIndex = endIndex;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
