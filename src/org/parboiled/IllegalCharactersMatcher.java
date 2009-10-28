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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.utils.ImmutableList;

import java.util.List;

class IllegalCharactersMatcher implements Matcher {
    private final String expected;
    private final Characters stopMatchChars;

    /**
     * Creates an IllegalCharactersMatcher that will match all characters not in the given follower set.
     *
     * @param expected       a string describing the expected content
     * @param stopMatchChars the set of characters up to which illegal characters will be matched
     */
    public IllegalCharactersMatcher(@NotNull String expected, @NotNull Characters stopMatchChars) {
        this.expected = expected;
        this.stopMatchChars = stopMatchChars;
    }

    public String getLabel() {
        return "!ILLEGAL!";
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        context.addUnexpectedInputError(context.getCurrentLocation().currentChar, expected);
        do {
            context.advanceInputLocation();
        } while (!stopMatchChars.contains(context.getCurrentLocation().currentChar));
        context.createNode();
        return true;
    }

    @NotNull
    public List<Matcher> getChildren() {
        return ImmutableList.of();
    }

    public Characters getStarterChars() {
        throw new IllegalStateException(); // should never be called
    }

    public String getExpectedString() {
        throw new UnsupportedOperationException();
    }
}
