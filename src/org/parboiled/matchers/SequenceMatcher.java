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
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.Rule;
import org.parboiled.MatcherContext;

import java.util.List;

/**
 * A Matcher that executes all of its sub matcher in sequence and only succeeds, if all sub matchers succeed.
 * @param <V>
 */
public class SequenceMatcher<V> extends AbstractMatcher<V> implements FollowMatcher<V> {

    private final boolean enforced;

    public SequenceMatcher(@NotNull Rule[] subRules, boolean enforced) {
        super(subRules);
        this.enforced = enforced;
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        List<Matcher<V>> children = getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Matcher<V> matcher = children.get(i);

            // remember the current index in the context, so we can access it for building the current follower set
            context.setTag(i);

            boolean matched = context.runMatcher(matcher, enforced || (this.enforced && i > 0));
            if (!matched) return false;
        }
        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        return getStarterChars(0);
    }

    private Characters getStarterChars(int startIndex) {
        Characters chars = Characters.ONLY_EMPTY;
        for (int i = startIndex; i < getChildren().size(); i++) {
            Characters matcherStarters = getChildren().get(i).getStarterChars();
            chars = chars.add(matcherStarters);
            if (!matcherStarters.contains(Chars.EMPTY)) return chars.remove(Chars.EMPTY);
        }
        return chars;
    }

    public Characters getFollowerChars(MatcherContext<V> context) {
        int currentIndex = (Integer) context.getTag();
        return getStarterChars(currentIndex + 1);
    }

}
