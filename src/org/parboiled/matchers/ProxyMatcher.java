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
    private Boolean leaf;

    @NotNull
    public List<Matcher<V>> getChildren() {
        apply();
        return target.getChildren();
    }

    public boolean match(@NotNull MatcherContext<V> context) throws Throwable {
        apply();
        return target.match(context);
    }

    public String getExpectedString() {
        apply();
        return target.getExpectedString();
    }

    public Characters getStarterChars() {
        apply();
        return target.getStarterChars();
    }

    public String getLabel() {
        apply();
        return target.getLabel();
    }

    public boolean isLeaf() {
        apply();
        return target.isLeaf();
    }

    @Override
    public String toString() {
        if (target == null) return super.toString();
        apply();
        return target.toString();
    }

    private void apply() {
        Preconditions.checkState(target != null);
        if (label != null) {
            label(label);
        }
        if (leaf != null) {
            makeLeaf();
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
            anotherProxy.arm(this);
            return anotherProxy;
        }

        // we already have a target to which we can directly apply the label
        Rule inner = (Rule) unwrap(target);
        target = (Matcher<V>) inner.label(label); // since relabelling might change the instance we have to update it
        this.label = null;
        return (Rule) target;
    }

    @SuppressWarnings({"unchecked"})
    public Rule makeLeaf() {
        if (target == null) {
            // if we have no target yet we need to save the leaf marker and "apply" it later
            leaf = true;
            return this;
        }

        // we already have a target to which we can directly apply the leaf marker
        Rule inner = (Rule) unwrap(target);
        target = (Matcher<V>) inner.makeLeaf(); // since leaf marking might change the instance we have to update it
        leaf = null;
        return (Rule) target;
    }

    public void arm(Matcher<V> target) {
        this.target = target;
    }

    @SuppressWarnings({"unchecked"})
    public static <V> Matcher<V> unwrap(Matcher<V> matcher) {
        return matcher instanceof ProxyMatcher ? unwrap(((ProxyMatcher<V>) matcher).target) : matcher;
    }

}
