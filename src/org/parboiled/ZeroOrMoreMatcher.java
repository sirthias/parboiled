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
import org.parboiled.support.Chars;
import org.parboiled.support.Checks;
import org.parboiled.support.InputLocation;

class ZeroOrMoreMatcher extends AbstractMatcher implements FollowMatcher {

    public ZeroOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        Matcher matcher = getChildren().get(0);

        InputLocation lastLocation = context.getCurrentLocation();
        while (context.runMatcher(matcher, false)) {
            InputLocation currentLocation = context.getCurrentLocation();
            Checks.ensure(currentLocation.index > lastLocation.index,
                    "The inner rule of ZeroOrMore rule '%s' must not allow empty matches", context.getPath());
            lastLocation = currentLocation;
        }

        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Characters chars = getChildren().get(0).getStarterChars();
        Checks.ensure(!chars.contains(Chars.EMPTY), "Sub rule of an ZeroOrMore-rule must not allow empty matches");
        return chars;
    }

    public Characters getFollowerChars(MatcherContext context) {
        return getStarterChars().add(Chars.EMPTY);
    }

}