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

package org.parboiled.support;

import static org.parboiled.common.Preconditions.*;
import org.parboiled.Node;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;

import java.util.List;

/**
 * A simple container encapsulating the result of a parsing run.
 */
public class ParsingResult<V> {

    /**
     * Indicates whether the input was successfully parsed.
     */
    public final boolean matched;

    /**
     * The root node of the parse tree created by the parsing run. This field will only be non-null when
     * parse-tree-building has been enabled.
     */
    public final Node<V> parseTreeRoot;

    /**
     * The top value of the value stack at the end of the parsing run or null, if the value stack is empty.
     */
    public final V resultValue;

    /**
     * The ValueStack used during the parsing run containing all values not popped of the stack by the parser.
     */
    public final ValueStack<V> valueStack;

    /**
     * The list of parse errors created during the parsing run.
     */
    public final List<ParseError> parseErrors;

    /**
     * The underlying input buffer.
     */
    public final InputBuffer inputBuffer;

    /**
     * Creates a new ParsingResult.
     *
     * @param matched       true if the rule matched the input
     * @param parseTreeRoot the parse tree root node
     * @param valueStack    the value stack of the parsing run
     * @param parseErrors   the list of parse errors
     * @param inputBuffer   the input buffer
     */
    public ParsingResult(boolean matched, Node<V> parseTreeRoot, ValueStack<V> valueStack, List<ParseError> parseErrors,
                         InputBuffer inputBuffer) {
        this.matched = matched;
        this.parseTreeRoot = parseTreeRoot;
        this.valueStack = checkArgNotNull(valueStack, "valueStack");
        this.resultValue = valueStack.isEmpty() ? null : valueStack.peek();
        this.parseErrors = checkArgNotNull(parseErrors, "parseErrors");
        this.inputBuffer = checkArgNotNull(inputBuffer, "inputBuffer");
    }

    /**
     * @return true if this parsing result contains parsing errors.
     */
    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }
}
