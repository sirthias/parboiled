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
import org.parboiled.matchers.Matcher;

/**
 * Immutable class holding all values describing a certain error encountered during a parsing run.
 */
public class ParseError {

    private final InputLocation location;
    private final Matcher matcher;
    private final String matcherPatch;
    private final String errorMessage;

    public ParseError(@NotNull InputLocation location, @NotNull Matcher matcher, String matcherPatch,
                      @NotNull String errorMessage) {
        this.location = location;
        this.matcher = matcher;
        this.matcherPatch = matcherPatch;
        this.errorMessage = errorMessage;
    }

    public InputLocation getLocation() {
        return location;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public String getMatcherPatch() {
        return matcherPatch;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}

