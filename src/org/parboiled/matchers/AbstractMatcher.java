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
import org.parboiled.Rule;
import org.parboiled.common.ImmutableList;
import org.parboiled.trees.ImmutableGraphNode;

/**
 * Abstract base class of most regular Matchers.
 */
public abstract class AbstractMatcher<V> extends ImmutableGraphNode<Matcher<V>> implements Rule, Matcher<V>, Cloneable {

    private String label;
    private Matcher<V> recoveryMatcher;
    private boolean locked;
    private boolean leaf;
    private boolean withoutNode;

    protected AbstractMatcher() {
        this(new Rule[0]);
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule subRule) {
        this(new Rule[] {subRule});
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule[] subRules) {
        super(ImmutableList.<Matcher<V>>of(toMatchers(subRules)));
    }

    private static Matcher[] toMatchers(@NotNull Rule[] subRules) {
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++) {
            matchers[i] = (Matcher) subRules[i];
        }
        return matchers;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public boolean isWithoutNode() {
        return withoutNode;
    }

    public boolean hasLabel() {
        return label != null;
    }

    public String getExpectedString() {
        return getLabel(); // default implementation
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public Matcher<V> getRecoveryMatcher() {
        return recoveryMatcher;
    }

    @SuppressWarnings({"unchecked"})
    public AbstractMatcher<V> label(@NotNull String label) {
        if (label.equals(this.label)) return this;
        AbstractMatcher<V> matcher = isLocked() ? createClone() : this;
        matcher.label = label;
        return matcher;
    }

    @SuppressWarnings({"unchecked"})
    public Rule asLeaf() {
        if (isLeaf()) return this;
        AbstractMatcher<V> matcher = isLocked() ? createClone() : this;
        matcher.leaf = true;
        return matcher;
    }

    @SuppressWarnings({"unchecked"})
    public Rule recoveredBy(Rule recoveryRule) {
        if (recoveryRule == this.recoveryMatcher) return this;
        AbstractMatcher<V> matcher = isLocked() ? createClone() : this;
        matcher.recoveryMatcher = (Matcher<V>) recoveryRule;
        return matcher;
    }

    @SuppressWarnings({"unchecked"})
    public Rule withoutNode() {
        if (isWithoutNode()) return this;
        AbstractMatcher<V> matcher = isLocked() ? createClone() : this;
        matcher.withoutNode = true;
        return matcher;
    }

    // creates a shallow copy
    @SuppressWarnings({"unchecked"})
    private AbstractMatcher<V> createClone() {
        try {
            return (AbstractMatcher<V>) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

}

