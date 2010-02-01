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
 * A Matcher matching any character except for EOI.
 *
 * @param <V>
 */
public class AnyCharMatcher<V> extends AbstractMatcher<V> {

    public AnyCharMatcher(int index) {
        super(index);
    }

    @Override
    public String getLabel() {
        return hasLabel() ? super.getLabel() : "ANY";
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        if (context.getCurrentLocation().currentChar == Chars.EOI) return false;
        context.advanceInputLocation();
        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        return Characters.of(Chars.ANY);
    }

}