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
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;

/**
 * A Matcher matching a single character.
 *
 * @param <V>
 */
public class CharMatcher<V> extends AbstractMatcher<V> {

    public final char character;

    public CharMatcher(char character) {
        this.character = character;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        if (label != null) return label;

        switch (character) {
            case Chars.EOI:
                return "EOI";
            case Chars.ANY:
                return "ANY";
            case Chars.EMPTY:
                return "EMPTY";
            default:
                return "\'" + character + '\'';
        }
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        if (matches(context)) {
            if (character != Chars.EMPTY) context.advanceInputLocation();
            context.createNode();
            return true;
        }
        return false;
    }

    public boolean matches(MatcherContext<V> context) {
        char current = context.getCurrentLocation().currentChar;
        return character == Chars.EMPTY ||
                (character == Chars.ANY && current != Chars.EOI) ||
                character == current;
    }

    public Characters getStarterChars() {
        return Characters.of(character);
    }

}