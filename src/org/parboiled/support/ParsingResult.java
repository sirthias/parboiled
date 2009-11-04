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

import java.util.List;

/**
 * A simple container for encapsulating the result of a parsing run.
 */
public class ParsingResult<V> {

    /**
     * The root node of the parse tree created by the parsing run.
     */
    public final Node<V> parseTreeRoot;

    /**
     * The list of parse errors created during the parsing run.
     */
    public final List<ParseError> parseErrors;

    /**
     * The underlying input buffer.
     */
    public final InputBuffer inputBuffer;

    public ParsingResult(@NotNull Node<V> parseTreeRoot, @NotNull List<ParseError> parseErrors,
                         @NotNull InputBuffer inputBuffer) {
        this.parseTreeRoot = parseTreeRoot;
        this.parseErrors = parseErrors;
        this.inputBuffer = inputBuffer;
    }

    /**
     * @return true if the parsing run was completed without errors.
     */
    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }

}
