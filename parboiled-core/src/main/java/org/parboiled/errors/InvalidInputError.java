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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.support.MatcherPath;

import java.util.List;

/**
 * A {@link ParseError} describing one or more input characters that are illegal with regard to the underlying
 * language grammar.
 */
public class InvalidInputError extends BasicParseError {
    private final List<MatcherPath> failedMatchers;

    public InvalidInputError(InputBuffer inputBuffer, int startIndex,
                             List<MatcherPath> failedMatchers, String errorMessage) {
        super(checkArgNotNull(inputBuffer, "inputBuffer"), startIndex, errorMessage);
        this.failedMatchers = checkArgNotNull(failedMatchers, "failedMatchers");
    }

    /**
     * Gets the list of paths to the single character matchers that failed at the error location of this error.
     *
     * @return the list of paths to the single character matchers that failed at the error location of this error
     */
    public List<MatcherPath> getFailedMatchers() {
        return failedMatchers;
    }
}

