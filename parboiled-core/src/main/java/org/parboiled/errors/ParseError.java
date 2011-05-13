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

import org.parboiled.buffers.InputBuffer;

/**
 * Common interface of all parboiled parse error implementations.
 */
public interface ParseError {

    /**
     * Gets the inputbuffer this error occurred in.
     *
     * @return the inputbuffer
     */
    InputBuffer getInputBuffer();

    /**
     * Gets the start index of the parse error in the underlying input buffer.
     *
     * @return the input index of the first character covered by this error
     */
    int getStartIndex();

    /**
     * Gets the end index of the parse error in the underlying input buffer.
     *
     * @return the end index of this error, i.e. the index of the character immediately following the last character
     *         covered by this error
     */
    int getEndIndex();

    /**
     * An optional error message.
     *
     * @return an optional error message.
     */
    String getErrorMessage();

}
