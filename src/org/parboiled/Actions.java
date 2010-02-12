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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.errorhandling.StarterCharsVisitor;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.EmptyMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.Characters;

public class Actions {

    public static <V> Action<V> match(@NotNull final Matcher<V> matcher) {
        return new NamedAction<V>("match") {
            @SuppressWarnings({"unchecked"})
            public boolean run(Context<V> context) {
                MatcherContext<V> matcherContext = (MatcherContext<V>) context;
                return matcherContext.getSubContext(matcher).runMatcher();
            }
        };
    }

    public static <V> Action<V> injectVirtualInput(final char character) {
        return new NamedAction<V>("injectVirtualInput") {
            public boolean run(Context<V> context) {
                context.injectVirtualInput(character);
                return true;
            }
        };
    }

    public static <V> Action<V> isNextCharLegalFollower(@NotNull Context<V> context) {
        final Characters followChars = Characters.NONE;
        StarterCharsVisitor<V> starterCharsVisitor = new StarterCharsVisitor<V>();
        for (Matcher<V> followMatcher : context.getCurrentFollowerMatchers()) {
            followChars.add(followMatcher.accept(starterCharsVisitor));
        }

        return new NamedAction<V>("isNextCharLegalFollower") {
            public boolean run(Context<V> context) {
                return followChars.contains(context.getCurrentLocation().currentChar);
            }
        };
    }

    public static <V> Action<V> createEmptyNodeFor(@NotNull Matcher<V> matcher) {
        final AbstractMatcher emptyMatcher = new EmptyMatcher().label(matcher.getLabel());
        return new NamedAction<V>("createEmptyNodeFor") {
            @SuppressWarnings({"unchecked"})
            public boolean run(Context<V> context) {
                MatcherContext<V> matcherContext = (MatcherContext<V>) context;
                return matcherContext.getSubContext(emptyMatcher).runMatcher();
            }
        };
    }

}
