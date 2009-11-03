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
import org.parboiled.common.ImmutableList;
import org.parboiled.Rule;
import org.parboiled.MatcherContext;

public class WrapMatcher<V> extends AbstractRule<Matcher<V>> implements Matcher<V> {

    @SuppressWarnings({"unchecked"})
    public WrapMatcher(@NotNull Rule innerRule) {
        // we cast directly instead of calling toMatcher(), for not triggering the LazyLoader proxy too early
        super(ImmutableList.of((Matcher<V>) innerRule));
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        return label != null ? label : getInner().getLabel();
    }

    public Matcher toMatcher() {
        return this;
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        return getInner().match(context, enforced);
    }

    @Override
    public String toString() {
        return "wrapper:" + getLabel();
    }

    public Characters getStarterChars() {
        return getInner().getStarterChars();
    }

    public String getExpectedString() {
        return getInner().getExpectedString();
    }

    private Matcher<V> getInner() {
        return getChildren().get(0);
    }

}
