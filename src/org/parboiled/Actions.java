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

import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.EmptyMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.Characters;

public class Actions {

    public static Action testContextAlreadyMatchedSomething(final Context testContext) {
        return new NamedAction("testContextAlreadyMatchedSomething") {
            public boolean run(Context context) {
                return testContext.getCurrentLocation() != testContext.getStartLocation();
            }
        };
    }

    public static Action match(final Matcher matcher) {
        return new NamedAction("match") {
            @SuppressWarnings({"unchecked"})
            public boolean run(Context context) {
                MatcherContext matcherContext = (MatcherContext) context;
                return matcherContext.getSubContext(matcher).runMatcher();
            }
        };
    }

    public static Action isNextCharIn(final Characters characters) {
        return new NamedAction("isNextCharIn") {
            public boolean run(Context context) {
                return characters.contains(context.getCurrentLocation().currentChar);
            }
        };
    }

    public static Action createEmptyNodeFor(final Matcher matcher) {
        return new NamedAction("createEmptyNodeFor") {
            @SuppressWarnings({"unchecked"})
            public boolean run(Context context) {
                AbstractMatcher emptyMatcher = new EmptyMatcher().label(matcher.getLabel());
                MatcherContext matcherContext = (MatcherContext) context;
                return matcherContext.getSubContext(emptyMatcher).runMatcher();
            }
        };
    }

}
