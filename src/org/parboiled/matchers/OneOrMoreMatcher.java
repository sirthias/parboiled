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
import org.parboiled.Rule;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;

/**
 * A Matcher that repeatedly tries its sub matcher against the input. Succeeds if its sub matcher succeeds at least once.
 *
 * @param <V>
 */
public class OneOrMoreMatcher<V> extends AbstractMatcher<V> implements FollowMatcher<V> {

    public OneOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        Matcher<V> matcher = getChildren().get(0);

        boolean matched = context.runMatcher(matcher, enforced);
        if (!matched) return false;

        // collect all further matches as well
        InputLocation lastLocation = context.getCurrentLocation();
        while (context.runMatcher(matcher, false)) {
            InputLocation currentLocation = context.getCurrentLocation();
            if (currentLocation == lastLocation) {
                Checks.fail("The inner rule of OneOrMore rule '%s' must not allow empty matches", context.getPath());
            }
            lastLocation = currentLocation;
        }

        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Matcher<V> matcher = getChildren().get(0);
        Characters chars = matcher.getStarterChars();
        Checks.ensure(!chars.contains(Chars.EMPTY),
                "Rule '%s' must not allow empty matches as sub-rule of an OneOrMore-rule", matcher);
        return chars;
    }

    public Characters getFollowerChars(MatcherContext<V> context) {
        // since this call is only legal when we are currently within a match of our sub matcher,
        // i.e. the submatcher can either match once more or the repetition can legally terminate which means
        // our follower set addition is incomplete -> add EMPTY
        return getStarterChars().add(Chars.EMPTY);
    }
}
