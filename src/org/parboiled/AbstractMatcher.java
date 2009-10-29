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
import org.parboiled.utils.ImmutableList;
import org.parboiled.utils.Preconditions;

/**
 * Abstract base class of most regular Matchers.
 */
abstract class AbstractMatcher<V> extends AbstractRule<Matcher<V>> implements Matcher<V> {

    protected AbstractMatcher() {
        super(ImmutableList.<Matcher<V>>of());
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule subRule) {
        super(ImmutableList.of((Matcher<V>)subRule.toMatcher()));
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule[] subRules) {
        super(ImmutableList.<Matcher<V>>of(toMatchers(subRules)));
    }

    private static Matcher[] toMatchers(@NotNull Rule[] subRules) {
        Preconditions.checkArgument(subRules.length > 0);
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++) {
            matchers[i] = subRules[i].toMatcher();
        }
        return matchers;
    }

    // implementation of toMatcher() from the Rule interface
    public Matcher toMatcher() {
        return this;
    }

    public String getExpectedString() {
        return getLabel(); // default implementation
    }

    @Override
    public String toString() {
        String label = getLabel();
        if (label != null) return label;
        label = getClass().getSimpleName();
        return label.substring(0, label.length() - 7); // remove the "Matcher" ending
    }

}

