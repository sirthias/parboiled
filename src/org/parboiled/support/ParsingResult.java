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
import org.parboiled.Node;
import org.parboiled.errorhandling.ParseError;

import java.util.List;

/**
 * A simple container for encapsulating the result of a parsing run.
 */
public class ParsingResult<V> {

    /**
     * Indicated whether the input was successfully parsed.
     */
    public final boolean matched;

    /**
     * The root node of the parse tree created by the parsing run.
     */
    public final Node<V> parseTreeRoot;

    /**
     * The list of parse errors created during the parsing run.
     */
    public final List<ParseError<V>> parseErrors;

    /**
     * The underlying input buffer.
     */
    public final InputBuffer inputBuffer;

    /**
     * The total number of rows in the parsed input text;
     */
    public final int totalRows;

    /**
     * Creates a new ParsingResult.
     *
     * @param matched       true if the rule matched the input
     * @param parseTreeRoot the parse tree root node
     * @param parseErrors   the list of parse errors
     * @param inputBuffer   the input buffer
     * @param totalRows     the number of total number of rows in the parsed input text
     */
    public ParsingResult(boolean matched, Node<V> parseTreeRoot, @NotNull List<ParseError<V>> parseErrors,
                         @NotNull InputBuffer inputBuffer, int totalRows) {
        this.matched = matched;
        this.parseTreeRoot = parseTreeRoot;
        this.parseErrors = parseErrors;
        this.inputBuffer = inputBuffer;
        this.totalRows = totalRows;
    }

    /**
     * @return true if this parsing result contains parsing errors.
     */
    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }

}
