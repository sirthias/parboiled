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
import org.parboiled.support.Reference;

import java.util.List;

/**
 * A Matcher that delegates all Rule and Matcher interface methods to another Matcher.
 * It can also hold a label and lazily apply it to the underlying matcher once it is available.
 * @param <V>
 */
public class ProxyMatcher<V> implements Rule, Matcher<V> {

    private final Reference<Matcher<V>> ref = new Reference<Matcher<V>>();
    private String label;

    @NotNull
    public List<Matcher<V>> getChildren() {
        applyLabel();
        return ref.getTarget().getChildren();
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) throws Throwable {
        applyLabel();
        return ref.getTarget().match(context, enforced);
    }

    public String getExpectedString() {
        applyLabel();
        return ref.getTarget().getExpectedString();
    }

    public Characters getStarterChars() {
        applyLabel();
        return ref.getTarget().getStarterChars();
    }

    public String getLabel() {
        applyLabel();
        return ref.getTarget().getLabel();
    }

    @Override
    public String toString() {
        if (!ref.hasTarget()) return super.toString();
        applyLabel();
        return ref.getTarget().toString();
    }

    private void applyLabel() {
        Preconditions.checkState(ref.hasTarget());
        if (label != null) {
            label(label);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Rule label(String label) {
        if (!ref.hasTarget()) {
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
        Rule innerMost = (Rule) unwrap(ref.getTarget());
        Rule newTarget = innerMost.label(label);
        ref.setTarget((Matcher<V>) newTarget); // since relabelling might change the instance we have to update it
        this.label = null;
        return newTarget;
    }

    public ProxyMatcher<V> arm(Matcher<V> target) {
        ref.setTarget(target);
        return this;
    }

    @SuppressWarnings({"unchecked"})
    public static <V> Matcher<V> unwrap(Matcher<V> matcher) {
        return matcher instanceof ProxyMatcher ? unwrap(((ProxyMatcher<V>)matcher).ref.getTarget()) : matcher;
    }

}
