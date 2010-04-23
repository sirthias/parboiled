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

import java.util.List;

/**
 * A {@link Matcher} that executes all of its sub matcher in sequence and only succeeds, if all sub matchers succeed.
 *
 * @param <V>
 */
public class SequenceMatcher<V> extends AbstractMatcher<V> {

    public SequenceMatcher(@NotNull Rule[] subRules) {
        super(subRules);
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        List<Matcher<V>> children = getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Matcher<V> matcher = children.get(i);

            // remember the current index in the context, so we can access it for building the current follower set
            context.setIntTag(i);

            if (!matcher.getSubContext(context).runMatcher()) {
                return false;
            }
        }
        context.createNode();
        return true;
    }

    public <R> R accept(@NotNull MatcherVisitor<V, R> visitor) {
        return visitor.visit(this);
    }

}
