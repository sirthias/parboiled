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
import org.parboiled.common.Formatter;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.List;

public class InvalidInputError<V> implements ParseError {

    private final InputLocation errorLocation;
    private final MatcherPath<V> lastMatch;
    private final List<MatcherPath<V>> failedMatchers;
    private final Formatter<InvalidInputError<V>> formatter;
    private String errorMessage;

    public InvalidInputError(@NotNull InputLocation errorLocation, MatcherPath<V> lastMatch,
                             @NotNull List<MatcherPath<V>> failedMatchers,
                             @NotNull Formatter<InvalidInputError<V>> formatter) {
        this.errorLocation = errorLocation;
        this.lastMatch = lastMatch;
        this.failedMatchers = failedMatchers;
        this.formatter = formatter;
    }

    @NotNull
    public InputLocation getErrorLocation() {
        return errorLocation;
    }

    public MatcherPath<V> getLastMatch() {
        return lastMatch;
    }

    @NotNull
    public List<MatcherPath<V>> getFailedMatchers() {
        return failedMatchers;
    }

    public String getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = formatter.format(this);
        }
        return errorMessage;
    }

}

