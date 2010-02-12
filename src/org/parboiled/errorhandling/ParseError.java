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

package org.parboiled.errorhandling;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

/**
 * Immutable class holding all values describing a certain error encountered during a parsing run.
 */
public class ParseError<V> {

    /**
     * The input location where the parse error happened.
     */
    private final InputLocation location;

    /**
     * The path to the matcher that matched the input immediately before the parse error location.
     * Can be null if no match ever succeeded, i.e. the very first is illegal.
     */
    private final MatcherPath<V> lastMatchPath;

    /**
     * The error message.
     */
    private final String errorMessage;

    public ParseError(@NotNull InputLocation location, MatcherPath<V> lastMatchPath, @NotNull String errorMessage) {
        this.location = location;
        this.lastMatchPath = lastMatchPath;
        this.errorMessage = errorMessage;
    }

    public InputLocation getLocation() {
        return location;
    }

    public MatcherPath<V> getLastMatchPath() {
        return lastMatchPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

