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
import org.parboiled.common.Preconditions;
import org.parboiled.support.Characters;

import java.util.List;

/**
 * A Matcher that delegates all Rule and Matcher interface methods to another Matcher.
 * It can also hold a label and lazily apply it to the underlying matcher once it is available.
 *
 * @param <V>
 */
public class ProxyMatcher<V> implements Rule, Matcher<V> {

    private Matcher<V> target;
    private String label;

    @NotNull
    public List<Matcher<V>> getChildren() {
        applyLabel();
        return target.getChildren();
    }

    public boolean match(@NotNull MatcherContext<V> context) throws Throwable {
        applyLabel();
        return target.match(context);
    }

    public String getExpectedString() {
        applyLabel();
        return target.getExpectedString();
    }

    public Characters getStarterChars() {
        applyLabel();
        return target.getStarterChars();
    }

    public String getLabel() {
        applyLabel();
        return target.getLabel();
    }

    @Override
    public String toString() {
        if (target == null) return super.toString();
        applyLabel();
        return target.toString();
    }

    private void applyLabel() {
        Preconditions.checkState(target != null);
        if (label != null) {
            label(label);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Rule label(String label) {
        if (target == null) {
            // if we have no target yet we need to save the label and "apply" it later
            if (this.label == null) {
                this.label = label;
                return this;
            }

            // this proxy matcher is already waiting for its label application opportunity,
            // so we need to create another proxy level
            ProxyMatcher<V> anotherProxy = (ProxyMatcher<V>) new ProxyMatcher<V>().label(label);
            return anotherProxy.arm(this);
        }

        // we already have a target to which we can directly apply the label
        Rule innerMost = (Rule) unwrap(target);
        target = (Matcher<V>) innerMost
                .label(label); // since relabelling might change the instance we have to update it
        this.label = null;
        return (Rule) target;
    }

    public ProxyMatcher<V> arm(Matcher<V> target) {
        this.target = target;
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public static <V> Matcher<V> unwrap(Matcher<V> matcher) {
        return matcher instanceof ProxyMatcher ? unwrap(((ProxyMatcher<V>) matcher).target) : matcher;
    }

}
