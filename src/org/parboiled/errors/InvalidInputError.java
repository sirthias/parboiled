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

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.MatcherPath;

import java.util.List;

/**
 * A {@link ParseError} describing one or more input characters that are illegal with regard to the underlying
 * language grammar.
 *
 * @param <V>
 */
public class InvalidInputError<V> extends BasicParseError {

    private final MatcherPath<V> lastMatch;
    private final List<MatcherPath<V>> failedMatchers;

    public InvalidInputError(@NotNull InputBuffer inputBuffer, int startIndex, MatcherPath<V> lastMatch,
                             @NotNull List<MatcherPath<V>> failedMatchers, String errorMessage) {
        super(inputBuffer, startIndex, errorMessage);
        this.lastMatch = lastMatch;
        this.failedMatchers = failedMatchers;
    }

    /**
     * Gets the path to the matcher that matched the character immediately before the parse error location.
     *
     * @return the MatcherPatch
     */
    public MatcherPath<V> getLastMatch() {
        return lastMatch;
    }

    /**
     * Gets the list of paths to the single character matchers that failed at the error location of this error.
     *
     * @return the list of paths to the single character matchers that failed at the error location of this error
     */
    @NotNull
    public List<MatcherPath<V>> getFailedMatchers() {
        return failedMatchers;
    }

}

