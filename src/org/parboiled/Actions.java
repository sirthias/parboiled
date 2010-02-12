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
import org.parboiled.matchers.Matcher;
import org.parboiled.support.Characters;

public class Actions {

    public static <V> Action<V> match(@NotNull final Matcher<V> matcher) {
        return new NamedAction<V>("match") {
            @SuppressWarnings({"unchecked"})
            public boolean run(Context<V> context) {
                MatcherContext<V> matcherContext = (MatcherContext<V>) context;
                MatcherContext<V> actionMatcherContext = matcherContext.getSubContext();
                boolean matched = actionMatcherContext.getSubContext(matcher).runMatcher();
                if (matched && actionMatcherContext.getSubNodes() != null) {
                    matcherContext.addChildNodes(actionMatcherContext.getSubNodes());
                }
                return matched;
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

    public static <V> Action<V> isNextCharIn(@NotNull final Characters testChars) {
        return new NamedAction<V>("isNextCharIn") {
            public boolean run(Context<V> context) {
                return testChars.contains(context.getCurrentLocation().currentChar);
            }
        };
    }

}
