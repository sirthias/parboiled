/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;

/**
 * A {@link SequenceMatcher} specialization for sequences of CharMatchers. Performs fast string matching if the
 * current context has it enabled.
 */
public class StringMatcher extends SequenceMatcher {

    private final char[] characters;

    public StringMatcher(@NotNull Rule[] charMatchers, char[] characters) {
        super(charMatchers);
        this.characters = characters;
    }

    @Override
    public boolean match(@NotNull MatcherContext context) {
        if (!context.fastStringMatching()) {
            return super.match(context);
        }

        if (!context.getInputBuffer().test(context.getCurrentIndex(), characters)) return false;
        context.advanceIndex(characters.length);
        context.createNode();
        return true;
    }

}