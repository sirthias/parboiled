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

package org.parboiled.errors;

import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.common.StringUtils;

/**
 * Exception thrown by the IndentDedentInputbuffer upon detection of an illegal indentation.
 */
public class IllegalIndentationException extends RuntimeException {
    public final char[] input;
    public final int lineNumber;
    public final int indent;

    public IllegalIndentationException(char[] input, int lineNumber, int indent) {
        this.input = input;
        this.lineNumber = lineNumber;
        this.indent = indent;
    }

    @Override
    public String getMessage() {
        return "Illegal indentation in line " + lineNumber + ":\n" +
                new DefaultInputBuffer(input).extractLine(lineNumber) + '\n' +
                StringUtils.repeat('^', indent) + '\n';
    }
}