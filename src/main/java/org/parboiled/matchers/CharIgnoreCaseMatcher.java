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

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.matchervisitors.MatcherVisitor;

/**
 * A {@link Matcher} matching a single character case-independently.
 */
public class CharIgnoreCaseMatcher extends AbstractMatcher {

    public final char charLow;
    public final char charUp;

    public CharIgnoreCaseMatcher(char character) {
        this.charLow = Character.toLowerCase(character);
        this.charUp = Character.toUpperCase(character);
    }

    public boolean match(@NotNull MatcherContext context) {
        char c = context.getCurrentChar();
        if (c != charLow && c != charUp) return false;
        context.advanceIndex(1);
        context.createNode();
        return true;
    }

    public <R> R accept(@NotNull MatcherVisitor<R> visitor) {
        return visitor.visit(this);
    }

}